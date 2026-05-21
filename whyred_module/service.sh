#!/system/bin/sh
MODDIR=${0%/*}
PROFILE_FILE="/data/local/tmp/zoron/profile.txt"
MARKER_FILE="/data/local/tmp/zoron/boot_marker"
SCRIPT_DIR="/data/local/tmp/zoron"

mkdir -p $SCRIPT_DIR
chmod 777 $SCRIPT_DIR

# Wait until boot completes
while [ "$(getprop sys.boot_completed)" != "1" ]; do
    sleep 5
done

# Copy scripts from module directory to a reliable, executable location.
# This is necessary because PowerShell's Compress-Archive uses backslash paths
# in the zip, which can prevent Magisk's magic mount from working correctly.
# Also strip any Windows CRLF line endings.
for script in whyred_opt zoron_tracker.sh; do
    if [ -f "$MODDIR/system/bin/$script" ]; then
        sed 's/\r$//' "$MODDIR/system/bin/$script" > "$SCRIPT_DIR/$script"
        chmod 755 "$SCRIPT_DIR/$script"
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
fi

# Bootloop Security Check
if [ -f "$MARKER_FILE" ]; then
    echo "battery" > $PROFILE_FILE
fi
touch $MARKER_FILE

# Read saved profile or default to none
if [ -f "$PROFILE_FILE" ]; then
    PROFILE=$(cat $PROFILE_FILE)
else
    PROFILE="none"
    echo "none" > $PROFILE_FILE
fi
chmod 666 $PROFILE_FILE

# Apply the profile - try multiple paths
if [ -x "$SCRIPT_DIR/whyred_opt" ]; then
    sh $SCRIPT_DIR/whyred_opt $PROFILE
elif [ -f /system/bin/whyred_opt ]; then
    sed 's/\r$//' /system/bin/whyred_opt | sh -s $PROFILE
elif [ -f "$MODDIR/system/bin/whyred_opt" ]; then
    sed 's/\r$//' "$MODDIR/system/bin/whyred_opt" | sh -s $PROFILE
fi
rm -f $MARKER_FILE

# Start the battery tracker daemon
if [ -x "$SCRIPT_DIR/zoron_tracker.sh" ]; then
    nohup sh $SCRIPT_DIR/zoron_tracker.sh >/dev/null 2>&1 &
elif command -v zoron_tracker.sh >/dev/null 2>&1; then
    nohup zoron_tracker.sh >/dev/null 2>&1 &
fi
