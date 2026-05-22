#!/system/bin/sh
# ============================================================================
# ZORON-X Thermal Safety Daemon
# Monitors thermals and battery, enforces safety policies
# ============================================================================

LOG_FILE="/data/local/tmp/zoron/log.txt"

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

while true; do
    BATT_TEMP=$(dumpsys battery | grep temperature | grep -o "[0-9]*" | head -n 1)
    [ -z "$BATT_TEMP" ] && BATT_TEMP=0
    # dumpsys battery returns temp in tenths of a degree (e.g., 410 = 41.0C)
    BATT_TEMP_C=$((BATT_TEMP / 10))
    
    CPU_TEMP=0
    if [ -f /sys/class/thermal/thermal_zone0/temp ]; then
        TEMP_RAW=$(cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null)
        if [ "$TEMP_RAW" -gt 1000 ] 2>/dev/null; then
            CPU_TEMP=$((TEMP_RAW / 1000))
        else
            CPU_TEMP=$TEMP_RAW
        fi
    fi
    
    IS_CHARGING=$(dumpsys battery | grep -E "AC powered|USB powered|Wireless powered" | grep true)
    PROFILE=$(cat /data/local/tmp/zoron/profile.txt 2>/dev/null)
    
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
        sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq" "1401600" # 1.4 GHz clamp
    fi
    
    # Policy 3: IF charging == true, reduce max frequencies slightly to lower heat
    if [ -n "$IS_CHARGING" ] && [ "$PROFILE" = "balanced" ]; then
        sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq" "1804800"
    fi

    sleep 15
done
