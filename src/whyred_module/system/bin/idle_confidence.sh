#!/system/bin/sh
# ============================================================================
# ZORON-X Idle Confidence Engine
# Detects actual idleness to trigger deep/light idle appropriately
# ============================================================================

LOG_FILE="/data/local/tmp/zoron/log.txt"

log() {
    echo "[$(date)] [idle_confidence] $1" >> "$LOG_FILE"
}

IDLE_TIME=0

while true; do
    SCREEN_STATE=$(dumpsys window | grep "mScreenOn" | grep true)
    
    if [ -z "$SCREEN_STATE" ]; then
        # Screen is off
        IDLE_TIME=$((IDLE_TIME + 15))
        
        # Factors for confidence:
        IS_AUDIO=$(dumpsys audio | grep "player piid:" | grep "state:started")
        IS_CHARGING=$(dumpsys battery | grep -E "AC powered|USB powered|Wireless powered" | grep true)
        
        if [ -z "$IS_AUDIO" ] && [ -z "$IS_CHARGING" ]; then
            if [ "$IDLE_TIME" -eq 120 ]; then
                log "Idle confidence HIGH (120s screen off). Triggering DEEP_IDLE via Fastpath"
                /system/bin/zoron_fastpath.sh "set_mode" "deep"
                # Let main engine run heavy doze tuning in background
                /system/bin/zoron_engine "deep" &
            fi
        else
            # Audio playing or charging, keep light idle
            if [ "$IDLE_TIME" -eq 120 ]; then
                log "Idle confidence LOW (audio/charging). Remaining in LIGHT_IDLE"
            fi
        fi
    else
        # Screen is on
        if [ "$IDLE_TIME" -ge 120 ]; then
            log "Device woke up. Restoring original profile"
            PROFILE=$(cat /data/local/tmp/zoron/profile.txt 2>/dev/null || echo "balanced")
            /system/bin/zoron_fastpath.sh "set_mode" "$PROFILE"
            /system/bin/zoron_engine "$PROFILE" &
        fi
        IDLE_TIME=0
    fi
    
    sleep 15
done
