#!/system/bin/sh
# ============================================================================
# ZORON-X Thermal Safety Daemon
# v4.0.0: Optimized — sysfs-only reads, no dumpsys, 30s interval
# ============================================================================

LOG_FILE="/data/local/tmp/zoron/log.txt"
PROFILE_FILE="/data/local/tmp/zoron/profile.txt"

log() {
    echo "[$(date)] [thermal_daemon] $1" >> "$LOG_FILE"
}

sysfs_write() {
    if [ -e "$1" ]; then
        local CURRENT
        CURRENT=$(cat "$1" 2>/dev/null)
        if [ "$CURRENT" != "$2" ]; then
            echo "$2" > "$1" 2>/dev/null
        fi
    fi
}

# Get battery temperature from sysfs (returns tenths of °C, e.g. 310 = 31.0°C)
get_batt_temp() {
    # Direct sysfs — no dumpsys
    if [ -f /sys/class/power_supply/battery/temp ]; then
        cat /sys/class/power_supply/battery/temp 2>/dev/null
        return
    fi
    # Fallback: batt_therm thermal zone (some devices)
    for tz in /sys/class/thermal/thermal_zone*; do
        [ -d "$tz" ] || continue
        local type
        type=$(cat "$tz/type" 2>/dev/null)
        case "$type" in
            *battery*|*batt*|*bms*)
                cat "$tz/temp" 2>/dev/null
                return
                ;;
        esac
    done
    echo 0
}

# Get charging state from sysfs
is_charging() {
    if [ -f /sys/class/power_supply/battery/status ]; then
        local status
        status=$(cat /sys/class/power_supply/battery/status 2>/dev/null)
        case "$status" in
            Charging|Full) echo 1; return ;;
        esac
    fi
    echo 0
}

while true; do
    BATT_TEMP_RAW=$(get_batt_temp)
    [ -z "$BATT_TEMP_RAW" ] && BATT_TEMP_RAW=0
    BATT_TEMP_C=$((BATT_TEMP_RAW / 10))

    CPU_TEMP=0
    if [ -f /sys/class/thermal/thermal_zone0/temp ]; then
        TEMP_RAW=$(cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null)
        if [ "$TEMP_RAW" -gt 1000 ] 2>/dev/null; then
            CPU_TEMP=$((TEMP_RAW / 1000))
        else
            CPU_TEMP=$TEMP_RAW
        fi
    fi

    CHARGING=$(is_charging)
    PROFILE=$(cat "$PROFILE_FILE" 2>/dev/null)

    # Policy 1: IF battery_temp >= 41C, deny BURST mode
    if [ "$BATT_TEMP_C" -ge 41 ]; then
        if [ "$PROFILE" = "burst" ]; then
            log "Battery temp critical (${BATT_TEMP_C}C), downgrading from BURST to BALANCED"
            /system/bin/zoron_fastpath.sh "set_mode" "balanced"
            /system/bin/zoron_engine "balanced" &
        fi
    fi

    # Policy 2: IF cpu_temp >= 70C, clamp big cluster
    if [ "$CPU_TEMP" -ge 70 ]; then
        log "CPU temp critical (${CPU_TEMP}C), clamping big cluster"
        sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq" "1401600"
    fi

    # Policy 3: IF charging and balanced, reduce max freq slightly
    if [ "$CHARGING" -eq 1 ] && [ "$PROFILE" = "balanced" ]; then
        sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq" "1804800"
    fi

    # v4.0.0: 30s interval (thermal inertia, was 15s)
    sleep 30
done
