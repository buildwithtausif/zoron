#!/sbin/sh
DEVICE=$(getprop ro.product.device)
MODEL=$(getprop ro.product.model)
SOC=$(getprop ro.board.platform)
CORES=$(ls -d /sys/devices/system/cpu/cpu[0-9]* 2>/dev/null | wc -l)
[ "$CORES" -eq 0 ] && CORES=$(nproc 2>/dev/null || cat /proc/cpuinfo | grep -c processor 2>/dev/null || echo "unknown")
RAM_KB=$(grep MemTotal /proc/meminfo 2>/dev/null | awk '{print $2}')
if [ -n "$RAM_KB" ]; then
  RAM_MB=$((RAM_KB / 1024))
else
  RAM_MB="unknown"
fi

ui_print "==========================================="
ui_print "  ZORON-X v4.5.16 Power Optimizer"
ui_print "  Ultra Intelligent Power Conservation"
ui_print "==========================================="
ui_print ""
ui_print "- Device: $MODEL ($DEVICE)"
ui_print "- SoC: $SOC"
ui_print "- CPU Cores: $CORES"
ui_print "- RAM: ${RAM_MB}MB"
ui_print ""
ui_print "- ZORON-X is a universal optimizer."
ui_print "- All ARM/ARM64 Android devices supported."
ui_print ""

# --- Transitional upgrade: clean up old module ---
OLD_MODULE_DIR="/data/adb/modules/whyred_battery_optimizer"
OLD_PROFILE="/data/local/tmp/zoron/profile.txt"
if [ -d "$OLD_MODULE_DIR" ]; then
  ui_print "! Legacy module detected: whyred_battery_optimizer"
  ui_print "- Migrating profile and cleaning up..."

  # Migrate existing profile if present
  if [ -f "$OLD_PROFILE" ]; then
    OLD_PROF=$(cat "$OLD_PROFILE")
    ui_print "- Existing profile preserved: $OLD_PROF"
  fi

  # Mark old module for removal
  touch "$OLD_MODULE_DIR/remove" 2>/dev/null
  ui_print "- Old module marked for removal on next reboot"
  ui_print ""
fi

# Strip Windows CRLF line endings from all scripts (critical fix)
ui_print "- Fixing line endings..."
for f in $MODPATH/service.sh \
         $MODPATH/system/bin/whyred_opt \
         $MODPATH/system/bin/zoron_tracker.sh \
         $MODPATH/system/bin/zoron_engine \
         $MODPATH/system/bin/zoron_intent_engine \
         $MODPATH/system/bin/zoron_process_monitor; do
  if [ -f "$f" ]; then
    sed -i 's/\r$//' "$f"
  fi
done

ui_print "- Setting permissions..."
set_perm_recursive $MODPATH 0 0 0755 0644
set_perm $MODPATH/system/bin/whyred_opt 0 2000 0755
set_perm $MODPATH/system/bin/zoron_tracker.sh 0 2000 0755
set_perm $MODPATH/system/bin/zoron_engine 0 2000 0755
set_perm $MODPATH/system/bin/zoron_intent_engine 0 2000 0755
set_perm $MODPATH/system/bin/zoron_process_monitor 0 2000 0755
set_perm $MODPATH/service.sh 0 0 0755

ui_print ""
ui_print "- Device capability detection:"
if [ -d "/sys/devices/system/cpu/cpufreq" ]; then
  ui_print "  * CPU frequency scaling: SUPPORTED"
else
  ui_print "  * CPU frequency scaling: NOT DETECTED"
fi
if [ -d "/sys/class/thermal" ]; then
  ui_print "  * Thermal management: SUPPORTED"
else
  ui_print "  * Thermal management: NOT DETECTED"
fi
if [ -f "/sys/class/kgsl/kgsl-3d0/max_gpuclk" ] || [ -d "/sys/class/devfreq" ]; then
  ui_print "  * GPU scaling: SUPPORTED"
else
  ui_print "  * GPU scaling: NOT DETECTED"
fi
if [ -f "/proc/sys/vm/dirty_ratio" ]; then
  ui_print "  * VM tuning: SUPPORTED"
else
  ui_print "  * VM tuning: NOT DETECTED"
fi

ui_print ""
ui_print "==========================================="
ui_print "  Installation complete!"
ui_print "  Default profile: balanced (ZORON-X)"
ui_print "  Use the ZORON app to change profiles."
ui_print "==========================================="
