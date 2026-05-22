#!/system/bin/sh
# ============================================================================
# ZORON-X Tracker — Background battery and system telemetry logger
# CSV format: timestamp,battery_level,profile,power_state,cpu_freq,temp
# ============================================================================

LOG_DIR="/data/local/tmp/zoron"
CSV_FILE="$LOG_DIR/battery.csv"

mkdir -p "$LOG_DIR"
chmod 777 "$LOG_DIR"
if [ ! -f "$CSV_FILE" ]; then
    touch "$CSV_FILE"
    chmod 666 "$CSV_FILE"
fi

while true; do
    BATTERY=$(dumpsys battery | grep level | grep -o "[0-9]*" | head -n 1)
    [ -z "$BATTERY" ] && BATTERY="N/A"

    PROFILE=$(cat /data/local/tmp/zoron/profile.txt 2>/dev/null)
    [ -z "$PROFILE" ] && PROFILE="N/A"

    TIMESTAMP=$(date +%s)
    [ -z "$TIMESTAMP" ] && TIMESTAMP="N/A"

    # New columns: power_state, cpu_freq, temp
    POWER_STATE=$(cat /data/local/tmp/zoron/power_state.txt 2>/dev/null | awk '{print $1}')
    [ -z "$POWER_STATE" ] && POWER_STATE="N/A"

    CPU_FREQ=""
    if [ -f /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq ]; then
        CPU_FREQ=$(cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq 2>/dev/null)
    fi
    [ -z "$CPU_FREQ" ] && CPU_FREQ="N/A"

    TEMP=""
    if [ -f /sys/class/thermal/thermal_zone0/temp ]; then
        TEMP=$(cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null)
    fi
    [ -z "$TEMP" ] && TEMP="N/A"

    # Rotate log if it gets too large
    LINES=$(wc -l < "$CSV_FILE" 2>/dev/null || echo 0)
    if [ "$LINES" -gt 1000 ]; then
        tail -n 500 "$CSV_FILE" > "$CSV_FILE.tmp"
        mv "$CSV_FILE.tmp" "$CSV_FILE"
        chmod 666 "$CSV_FILE"
    fi

    # Atomic row generation
    ROW="$TIMESTAMP,$BATTERY,$PROFILE,$POWER_STATE,$CPU_FREQ,$TEMP"
    echo "$ROW" >> "$CSV_FILE"

    sleep 600
done
