#!/system/bin/sh
# ============================================================================
# ZORON-X Fastpath Engine
# Handles immediate responsiveness-sensitive changes (Microbursts, Gov switches)
# ============================================================================

ACTION="$1"
ARG="$2" 

log() {
    echo "[$(date)] [zoron_fastpath] $1" >> /data/local/tmp/zoron/log.txt
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

case "$ACTION" in
    microburst)
        BOOST_DURATION="${ARG:-1000}" # ms
        # Temporarily boost frequencies and relax cpusets
        log "Applying microburst for ${BOOST_DURATION}ms"
        
        if [ -d /sys/devices/system/cpu/cpu0/cpufreq ]; then
            sysfs_write "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" "performance"
        fi
        if [ -d /sys/devices/system/cpu/cpu4/cpufreq ]; then
            sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor" "performance"
        fi
        
        # We don't have floating point sleep in standard Android shell often, fallback to sleep 1
        sleep 1
        
        log "Microburst ended, returning to previous state via engine update"
        # We trigger the main engine to restore the current profile
        PROFILE=$(cat /data/local/tmp/zoron/profile.txt 2>/dev/null || echo "balanced")
        /system/bin/zoron_engine "$PROFILE" &
        ;;
        
    set_mode)
        MODE="$ARG"
        log "Fastpath mode switch to $MODE"
        # Fastpath instantly sets governor and cpusets based on mode
        # Then let the background engine do the rest
        if [ "$MODE" = "burst" ] || [ "$MODE" = "perform" ]; then
            sysfs_write "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" "performance"
            sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor" "performance"
        elif [ "$MODE" = "balanced" ]; then
            sysfs_write "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" "schedutil"
            sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor" "schedutil"
        elif [ "$MODE" = "deep" ] || [ "$MODE" = "hibernation" ] || [ "$MODE" = "nightwatch" ]; then
            sysfs_write "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" "powersave"
            sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor" "powersave"
        fi
        ;;
    *)
        echo "Usage: zoron_fastpath.sh <microburst|set_mode> [duration/mode]"
        ;;
esac
