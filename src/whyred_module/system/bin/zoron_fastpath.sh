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
        log "Applying microburst for ${BOOST_DURATION}ms"
        
        if [ -d /sys/devices/system/cpu/cpu0/cpufreq ]; then
            sysfs_write "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" "performance"
        fi
        if [ -d /sys/devices/system/cpu/cpu4/cpufreq ]; then
            sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor" "performance"
        fi
        
        sleep 1
        
        log "Microburst ended, returning to previous state via engine update"
        PROFILE=$(cat /data/local/tmp/zoron/profile.txt 2>/dev/null || echo "balanced")
        /system/bin/zoron_engine "$PROFILE" &
        ;;
        
    set_mode)
        MODE="$ARG"
        log "Fastpath mode switch to $MODE"
        if [ "$MODE" = "burst" ] || [ "$MODE" = "perform" ] || [ "$MODE" = "performance" ]; then
            sysfs_write "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" "performance"
            sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor" "performance"
        elif [ "$MODE" = "balanced" ] || [ "$MODE" = "deep" ] || [ "$MODE" = "hibernation" ] || [ "$MODE" = "battery" ] || [ "$MODE" = "video" ]; then
            sysfs_write "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" "schedutil"
            sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor" "schedutil"
        elif [ "$MODE" = "nightwatch" ]; then
            sysfs_write "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" "powersave"
            sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor" "powersave"
        fi
        ;;

    video_boost_on)
        log "Fastpath: Video Boost ON"
        # Online all big cores for dynamic video scaling
        for cpu in 4 5 6 7; do
            sysfs_write "/sys/devices/system/cpu/cpu${cpu}/online" 1
        done
        
        # Switch to schedutil for fluid scaling
        sysfs_write "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" "schedutil"
        sysfs_write "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor" "schedutil"
        
        # Maximize frequency bounds for zero lag
        if [ -f /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq ]; then
            cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq 2>/dev/null
        fi
        if [ -f /sys/devices/system/cpu/cpu4/cpufreq/cpuinfo_max_freq ]; then
            cat /sys/devices/system/cpu/cpu4/cpufreq/cpuinfo_max_freq > /sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq 2>/dev/null
        fi
        
        # Maximize GPU rendering clock bounds
        if [ -d /sys/class/kgsl/kgsl-3d0 ]; then
            sysfs_write "/sys/class/kgsl/kgsl-3d0/devfreq/governor" "msm-adreno-tz"
            if [ -f /sys/class/kgsl/kgsl-3d0/gpu_available_frequencies ]; then
                local max_gpu
                max_gpu=$(cat /sys/class/kgsl/kgsl-3d0/gpu_available_frequencies 2>/dev/null | tr ' ' '\n' | sort -rn | head -n 1)
                [ -n "$max_gpu" ] && sysfs_write "/sys/class/kgsl/kgsl-3d0/max_gpuclk" "$max_gpu"
            fi
        fi
        ;;

    video_boost_off)
        log "Fastpath: Video Boost OFF"
        # Re-apply active manual profile to restore background power savings
        PROFILE=$(cat /data/local/tmp/zoron/profile.txt 2>/dev/null || echo "balanced")
        sh /system/bin/zoron_engine "$PROFILE" &
        ;;
        
    *)
        echo "Usage: zoron_fastpath.sh <microburst|set_mode|video_boost_on|video_boost_off> [duration/mode]"
        ;;
esac
