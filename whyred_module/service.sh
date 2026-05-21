#!/system/bin/sh
MODDIR=${0%/*}
PROFILE_FILE="/data/local/tmp/zoron/profile.txt"
MARKER_FILE="/data/local/tmp/zoron/boot_marker"

mkdir -p /data/local/tmp/zoron
chmod 777 /data/local/tmp/zoron

# Wait until boot completes
while [ "$(getprop sys.boot_completed)" != "1" ]; do
    sleep 5
done

# Install the Controller APK if it's not installed
if ! pm list packages | grep -q "com.zoron.whyred"; then
    pm install -g -r $MODDIR/Zoron.apk
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

# Apply the profile
/system/bin/whyred_opt $PROFILE
rm -f $MARKER_FILE

# Start the battery tracker daemon
nohup /system/bin/zoron_tracker.sh >/dev/null 2>&1 &
