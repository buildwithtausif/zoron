#!/system/bin/sh
MODDIR=${0%/*}
PROFILE_FILE="/data/local/tmp/zoron/profile.txt"
MARKER_FILE="/data/local/tmp/zoron/boot_marker"

mkdir -p /data/local/tmp/zoron
chmod 777 /data/local/tmp/zoron

# Copy scripts to globally accessible tmp to bypass SELinux and Magisk overlay bugs
cp $MODDIR/system/bin/whyred_opt /data/local/tmp/zoron/whyred_opt
cp $MODDIR/system/bin/zoron_tracker.sh /data/local/tmp/zoron/zoron_tracker.sh
chmod 777 /data/local/tmp/zoron/whyred_opt
chmod 777 /data/local/tmp/zoron/zoron_tracker.sh

# Wait until boot completes
while [ "$(getprop sys.boot_completed)" != "1" ]; do
    sleep 5
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

# Apply the profile natively
sh /data/local/tmp/zoron/whyred_opt $PROFILE
rm -f $MARKER_FILE

# Start the battery tracker daemon
nohup sh /data/local/tmp/zoron/zoron_tracker.sh >/dev/null 2>&1 &
