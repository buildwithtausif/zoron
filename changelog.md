# Zoron Changelog

## v2.9.0
- **Unified Native OTA**: App now seamlessly downloads the Magisk Module ZIP in the background and natively flashes it using root (libsu) without ever leaving the app.
- **Expressive Material 3 UI**: Completely redesigned the dashboard to use Google's modern expressive fluid shapes.

## v2.8.0
- Migrated all UI elements to Android 16 Material 3 standards.
- Fixed silent root script execution failures caused by CRLF line endings.
- Relocated log tracking to Magisk module folder to bypass SELinux restrictions.
- Added native OTA check in settings menu.

## v2.7.0
- **Root Toast Spam Eliminated:** Overhauled the root execution engine using topjohnwu's `libsu`. The app now maintains a single persistent root shell, meaning Magisk will only prompt you for root access exactly once.
- **OTA Updates Configured:** Introduced a dual OTA update system. Magisk will now check for background zip updates natively, and the Zoron app features an in-app OTA checker to prompt you for new APK releases automatically.
- **Bug Fixes:** Resolved an issue where the "none" profile would intermittently fail to save to disk due to premature shell stream closures.

## v2.6.3
- **Battery Tracker Graph:** Integrated MPAndroidChart. A lightweight kernel script tracks your battery drainage every 10 minutes and plots a beautiful comparison line graph natively on the app dashboard.
- **"No Tweak" Profile:** Added a dedicated profile button that completely reverts the device's CPU and thermals back to stock, allowing you to establish a baseline for your battery graphs.
- **Log Mount Fix:** Moved logs to `/data/local/tmp/` to bypass Android's strict SD Card FUSE scoping limitations.

## v1.1.0
- **UI Enhancements:** Restructured the layout and added detailed impact descriptions beneath each profile.
- **Bootloop Security:** Introduced a failsafe boot marker. If your phone kernel panics and hard-reboots while applying tweaks, the module instantly rolls back to the safest `battery` profile on the next boot to prevent bootloops.
- **Reliability:** Converted all scripts to Unix (LF) line endings and moved the APK to `/system/app/` to prevent auto-injection bugs on Android 14.

## v1.0.0
- **Initial Release:** Created the `whyred_opt` kernel tuning script with Battery, Balanced, and Performance modes. Built the original Android controller app.
