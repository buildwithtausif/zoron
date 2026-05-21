#!/sbin/sh
DEVICE=$(getprop ro.product.device)
MODEL=$(getprop ro.product.model)

ui_print "- Installing Zoron Battery & Thermal Optimizer"
ui_print "- Device: $MODEL ($DEVICE)"

if [ "$DEVICE" != "whyred" ] && [ "$DEVICE" != "tulip" ]; then
  ui_print "! Warning: This module is intended for whyred/tulip."
  ui_print "! Proceeding with caution..."
fi

ui_print "- Setting permissions..."
set_perm_recursive $MODPATH 0 0 0755 0644
set_perm $MODPATH/system/bin/whyred_opt 0 2000 0755
set_perm $MODPATH/system/bin/zoron_tracker.sh 0 2000 0755
set_perm $MODPATH/service.sh 0 0 0755
ui_print "- Done!"
