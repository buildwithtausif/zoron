#!/system/bin/sh
MODDIR=${0%/*}
PROFILE_FILE="/data/local/tmp/zoron/profile.txt"
MARKER_FILE="/data/local/tmp/zoron/boot_marker"
SCRIPT_DIR="/data/local/tmp/zoron"
LOG_FILE="/data/local/tmp/zoron/log.txt"

mkdir -p $SCRIPT_DIR
chmod 777 $SCRIPT_DIR

# Logging helper
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') [service] $1" >> $LOG_FILE 2>/dev/null
}

log "========================================"
log "ZORON-X v4.5.12 service.sh starting"
log "========================================"

# Wait until boot completes
while [ "$(getprop sys.boot_completed)" != "1" ]; do
    sleep 5
done
log "Boot completed, proceeding with initialization"

# Copy scripts from module directory to a reliable, executable location.
# This is necessary because PowerShell's Compress-Archive uses backslash paths
# in the zip, which can prevent Magisk's magic mount from working correctly.
# Also strip any Windows CRLF line endings.
for script in whyred_opt zoron_tracker.sh zoron_engine zoron_intent_engine zoron_process_monitor zoron_fastpath.sh thermal_safety_daemon.sh idle_confidence.sh zoron_rule_engine; do
    if [ -f "$MODDIR/system/bin/$script" ]; then
        sed 's/\r$//' "$MODDIR/system/bin/$script" > "$SCRIPT_DIR/$script"
        chmod 755 "$SCRIPT_DIR/$script"
        log "Copied and fixed: $script"
    fi
done

# Install or upgrade the Controller APK safely via /data/local/tmp
MOD_VER=$(grep versionCode $MODDIR/module.prop | cut -d= -f2)
MARKER_APK="/data/local/tmp/zoron/apk_version"
if [ ! -f "$MARKER_APK" ] || [ "$(cat $MARKER_APK)" != "$MOD_VER" ]; then
    cp $MODDIR/Zoron.apk /data/local/tmp/zoron/Zoron.apk
    chmod 666 /data/local/tmp/zoron/Zoron.apk
    pm install -g -r /data/local/tmp/zoron/Zoron.apk
    rm -f /data/local/tmp/zoron/Zoron.apk
    echo "$MOD_VER" > "$MARKER_APK"
    log "APK installed/upgraded to versionCode $MOD_VER"
fi

# --- Transitional upgrade: disable old module ---
OLD_MODULE_DIR="/data/adb/modules/whyred_battery_optimizer"
UPGRADE_MARKER="/data/local/tmp/zoron/upgrade_from_legacy"
if [ -d "$OLD_MODULE_DIR" ]; then
    log "Legacy module detected at $OLD_MODULE_DIR, disabling it"
    touch "$OLD_MODULE_DIR/disable" 2>/dev/null
    echo "migrated_by_zoron_x_v3.5.0" > "$UPGRADE_MARKER" 2>/dev/null
    log "Legacy module disabled and upgrade marker created"
fi

# Bootloop Security Check
if [ -f "$MARKER_FILE" ]; then
    echo "battery" > $PROFILE_FILE
    log "Bootloop marker found! Falling back to battery profile"
fi
touch $MARKER_FILE

# Read saved profile or default to balanced (ZORON-X balanced)
if [ -f "$PROFILE_FILE" ]; then
    PROFILE=$(cat $PROFILE_FILE)
else
    PROFILE="balanced"
    echo "balanced" > $PROFILE_FILE
    log "No profile found, defaulting to ZORON-X balanced"
fi
chmod 666 $PROFILE_FILE
log "Active profile: $PROFILE"

# Determine which engine to use based on profile type
# ZORON-X modes: balanced, deep, hibernation, burst, nightwatch -> use zoron_engine
# Legacy modes:  none, battery, performance                     -> use whyred_opt
case "$PROFILE" in
    balanced|deep|hibernation|burst|nightwatch)
        ENGINE="zoron_engine"
        log "ZORON-X profile detected, using zoron_engine"
        ;;
    none|battery|performance)
        DEVICE=$(getprop ro.product.device 2>/dev/null)
        if [ "$DEVICE" = "whyred" ] || [ "$DEVICE" = "tulip" ]; then
            ENGINE="whyred_opt"
            log "Legacy profile detected on whyred/tulip, using whyred_opt"
        else
            ENGINE="zoron_engine"
            log "Legacy profile detected on generic device, using zoron_engine"
        fi
        ;;
    *)
        # Unknown profile, treat as ZORON-X balanced
        ENGINE="zoron_engine"
        PROFILE="balanced"
        echo "balanced" > $PROFILE_FILE
        log "Unknown profile '$PROFILE', defaulting to zoron_engine balanced"
        ;;
esac

# Apply the profile - try multiple paths
if [ "$ENGINE" = "zoron_engine" ]; then
    if [ -x "$SCRIPT_DIR/zoron_engine" ]; then
        log "Executing: $SCRIPT_DIR/zoron_engine $PROFILE"
        sh $SCRIPT_DIR/zoron_engine $PROFILE
    elif [ -f "$MODDIR/system/bin/zoron_engine" ]; then
        log "Executing zoron_engine from module dir"
        sed 's/\r$//' "$MODDIR/system/bin/zoron_engine" | sh -s $PROFILE
    fi
else
    if [ -x "$SCRIPT_DIR/whyred_opt" ]; then
        log "Executing: $SCRIPT_DIR/whyred_opt $PROFILE"
        sh $SCRIPT_DIR/whyred_opt $PROFILE
    elif [ -f /system/bin/whyred_opt ]; then
        sed 's/\r$//' /system/bin/whyred_opt | sh -s $PROFILE
    elif [ -f "$MODDIR/system/bin/whyred_opt" ]; then
        sed 's/\r$//' "$MODDIR/system/bin/whyred_opt" | sh -s $PROFILE
    fi
fi
rm -f $MARKER_FILE
log "Profile applied, boot marker removed"

# Start the battery tracker daemon
if [ -x "$SCRIPT_DIR/zoron_tracker.sh" ]; then
    nohup sh $SCRIPT_DIR/zoron_tracker.sh >/dev/null 2>&1 &
    log "Battery tracker daemon started (PID: $!)"
elif command -v zoron_tracker.sh >/dev/null 2>&1; then
    nohup zoron_tracker.sh >/dev/null 2>&1 &
    log "Battery tracker daemon started via PATH (PID: $!)"
fi

# v4.0.0: Stagger daemon starts to avoid burst resource usage
sleep 5

# Start the intent engine daemon (with PID file check)
INTENT_PID_FILE="$SCRIPT_DIR/zoron_intent_engine.pid"
if [ -f "$INTENT_PID_FILE" ] && kill -0 "$(cat $INTENT_PID_FILE)" 2>/dev/null; then
    log "Intent engine already running (PID: $(cat $INTENT_PID_FILE)), skipping"
else
    if [ -x "$SCRIPT_DIR/zoron_intent_engine" ]; then
        nohup sh $SCRIPT_DIR/zoron_intent_engine >/dev/null 2>&1 &
        echo $! > "$INTENT_PID_FILE"
        log "Intent engine daemon started (PID: $!)"
    fi
fi

# Stagger again before process monitor
sleep 5

# Start the process monitor daemon (with PID file check)
PROCMON_PID_FILE="$SCRIPT_DIR/zoron_process_monitor.pid"
if [ -f "$PROCMON_PID_FILE" ] && kill -0 "$(cat $PROCMON_PID_FILE)" 2>/dev/null; then
    log "Process monitor already running (PID: $(cat $PROCMON_PID_FILE)), skipping"
else
    if [ -x "$SCRIPT_DIR/zoron_process_monitor" ]; then
        nohup sh $SCRIPT_DIR/zoron_process_monitor >/dev/null 2>&1 &
        echo $! > "$PROCMON_PID_FILE"
        log "Process monitor daemon started (PID: $!)"
    fi
fi

# Stagger again before rule engine
sleep 5

# Start the rule engine daemon (with PID file check)
RULE_PID_FILE="$SCRIPT_DIR/zoron_rule_engine.pid"
if [ -f "$RULE_PID_FILE" ] && kill -0 "$(cat $RULE_PID_FILE)" 2>/dev/null; then
    log "Rule engine already running (PID: $(cat $RULE_PID_FILE)), skipping"
else
    if [ -x "$SCRIPT_DIR/zoron_rule_engine" ]; then
        nohup sh $SCRIPT_DIR/zoron_rule_engine >/dev/null 2>&1 &
        echo $! > "$RULE_PID_FILE"
        log "Rule engine daemon started (PID: $!)"
    fi
fi

log "ZORON-X v4.5.12 service.sh initialization complete"
