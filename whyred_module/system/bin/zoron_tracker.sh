#!/system/bin/sh
LOG_DIR="/data/adb/modules/whyred_battery_optimizer/logs"
CSV_FILE="$LOG_DIR/battery.csv"

mkdir -p $LOG_DIR
chmod 777 $LOG_DIR
if [ ! -f "$CSV_FILE" ]; then
    touch $CSV_FILE
    chmod 666 $CSV_FILE
fi

while true; do
    BATTERY=$(dumpsys battery | grep level | grep -o "[0-9]*" | head -n 1)
    PROFILE=$(cat /data/adb/modules/whyred_battery_optimizer/profile.txt 2>/dev/null || echo "unknown")
    TIMESTAMP=$(date +%s)
    
    LINES=$(wc -l < $CSV_FILE 2>/dev/null || echo 0)
    if [ "$LINES" -gt 1000 ]; then
        tail -n 500 $CSV_FILE > $CSV_FILE.tmp
        mv $CSV_FILE.tmp $CSV_FILE
        chmod 666 $CSV_FILE
    fi

    if [ ! -z "$BATTERY" ]; then
        echo "$TIMESTAMP,$BATTERY,$PROFILE" >> $CSV_FILE
    fi
    
    sleep 600
done
