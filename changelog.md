# Zoron Changelog

## v4.3.1
- **Tablet Friendly UI**: Overhauled all pages (`Main`, `Settings`, `Learn About Modes`) to restrict width to a centered 720dp maximum width on screens with width >= 600dp (tablets). This prevents awkward content stretching and ensures a premium, readable, and balanced layout on large displays.
- **Learn About Modes Documentation**: Added detailed description card for the new **📹 Video Playback** mode explaining dynamic hardware boost behavior.

## v4.3.0
- **Universal Video Playback Detection**: Leveraged `AudioManager.isMusicActive()` and advanced package name matching to dynamically identify video/media playback system-wide from any app.
- **Dynamic Video Boost**: Implemented automated foreground/background triggers to apply a temporary, high-frequency fastpath video boost during playback on *any* profile, reverting instantly once playback halts.
- **Governor Tuning**: Switched power-saving profiles (`deep`, `hibernation`, `battery`) to use `schedutil` governor when the screen is on (restricting bounds via `scaling_max_freq`) to completely eliminate playback stutters.
- **Dynamic Material 3 UI**: Upgraded all layouts, backgrounds, card borders, ripples, and glows to use color-state-lists tied to Material 3 dynamic theme attributes.

## v4.2.0
- **UI Revamp**: Re-styled the entire controller app (Main, Settings, Splash, Mode Learn screens) using a premium purple/violet dark glassmorphism theme.
- **Staggered Animations**: Introduced elegant staggered slide-up and fade-in entry transitions for all activity components.
- **Micro-interactions**: Added interactive touch-scaling animations to ZORON-X cards and an animated radial pulse glow on the power state dot.
- **System Integration**: Re-themed notification small icons to monochrome Z-orbit style with purple accents and customized the system launcher icon assets.

## v4.1.1
- **Universal Legacy Modes**: Legacy profiles (None, Battery, Performance) now fully support all Android devices by utilizing the new `zoron_engine`, fixing the `whyred_opt` error.
- **Battery Drain Fix**: Radically optimized parameter limits in Balanced and Deep modes. Balanced mode now severely restricts frequency scaling for background tasks, resolving the 14% drain issue.
- **Stability Improvement**: The ZORON-X mode switcher now includes robust thread safeguards, preventing the controller app from randomly crashing during transitions.

## v3.5.0 — ZORON-X Power Architecture
- **ZORON-X Engine**: Complete next-generation power optimization engine replacing the legacy profile system. Implements 5 intelligent power modes (Balanced, Deep, Hibernation, Burst, Nightwatch) with dynamic device capability detection.
- **Universal Device Support**: ZORON-X now works on ALL Android devices by dynamically probing sysfs paths and applying only compatible optimizations. Whyred/Tulip (SDM636/660) devices receive hardcoded optimal frequency tuning.
- **User Intent Prediction Engine**: Background daemon that monitors screen state, touch activity, foreground app, and charging state to dynamically transition between 5 power states (Hyper Active → Interactive → Light Idle → Deep Idle → Sleep Idle).
- **Process Monitor**: App behavior tracking daemon that detects wakelock abuse, sync spam, and alarm spam. Assigns dynamic priority scores and applies per-tier restrictions (Tier S unrestricted → Tier D frozen/denied).
- **Enhanced Battery Tracker**: CSV now includes power state, CPU frequency, and temperature alongside battery level and profile.
- **Material Expressive Chart**: Redesigned analytics chart with cubic bezier curves, gradient fills, vibrant color palette, and smooth animations.
- **Power State Indicator**: Real-time power state display with animated pulse dot and state-specific colors/emojis.
- **ZORON-X Mode Cards**: 5 new mode cards with icons (⚡ Balanced, 🔋 Deep, ❄️ Hibernate, 🚀 Burst, 🌙 Nightwatch).
- **Collapsible Legacy Profiles**: Old Stock/Battery/Balanced/Performance profiles preserved in a collapsible section.
- **Process Monitor UI**: Real-time scrollable view of process tier classifications and wakelock abuse reports.
- **Transitional Module Upgrade**: Flashing v3.5.0 automatically detects and removes the old `whyred_battery_optimizer` module, migrating settings to the new `zoron_x_optimizer` module ID.
- **CPU/GPU/VM/Scheduler/IO/Thermal/Doze/zRAM tuning**: Each mode configures the full stack — CPU frequencies & governors, GPU clocks, VM parameters, kernel scheduler, I/O scheduler, thermal zones, doze timers, wakelock blocking, and zRAM.

## v3.0.1
- **Fixed OTA Update Dialogue**: Update notification now only appears when the installed app version differs from the released version. Previously used a stale hardcoded version code (v2.9.5) causing false update prompts.
- **CSV Export**: Added "Export" button in Analytics section to share battery drain CSV data via Android ShareSheet.
- **Log Export**: Added "Export" button in Diagnostics section to share diagnostic logs via Android ShareSheet.
- **Scrollable Diagnostics**: Diagnostics section now has a fixed max height (300dp) with vertical scrolling to prevent unbounded growth.

## v3.0.0
- **FIXED: Module zip structure** — PowerShell's `Compress-Archive` was creating backslash paths (`system\bin\whyred_opt`) inside the zip, which Android/Linux cannot extract as proper directories. The `system/bin/whyred_opt` file was never actually deployed to the filesystem. Zips are now built with proper forward-slash paths using .NET `ZipFile`.
- **Added META-INF** — Proper Magisk module installer bootstrap (`update-binary` + `updater-script`).
- **Robust service.sh** — Scripts are now copied from `$MODDIR/system/bin/` to `/data/local/tmp/zoron/` with CRLF stripping on every boot, ensuring they always work regardless of zip path structure.
- **App searches 3 paths** — `/data/local/tmp/zoron/`, `/system/bin/`, and `/data/adb/modules/.../system/bin/` with full filesystem diagnostics on failure.
- **Error dialog** — Full stdout/stderr/exit code with Copy to Clipboard button.

## v2.9.6
- **Fixed script execution**: Magisk scripts are now executed directly from PATH instead of from `/data/local/tmp` to prevent SELinux and noexec mount denials.

## v2.9.5
- **Adaptive App Icons**: Added full support for modern Android 8.0+ adaptive icons. The app icon will now shape-shift to match your launcher (circles, squarcles, teardrops) and supports Android 13+ monochrome Material You themed icons!

## v2.9.4
- **Full Fixes Applied**: Completely fixed Magisk overlay bugs by mapping execution paths globally.
- **App Version Code Fixed**: Synchronized Android APK `versionName` and `versionCode` properly so it upgrades correctly in the OS.

## v2.9.2
- **Android 16 Fix**: Fully implemented Material 3 `DynamicColors` to gracefully adapt to system Light/Dark mode instead of being stuck on bright green.
- **SELinux Bypass**: Moved profile states and logs from Magisk restricted folders directly into `/data/local/tmp/zoron` globally writable space to fix `Permission Denied` silent failures.
- **Debug Improvements**: Fixed error toasts not showing actual bash stdout/stderr on script failure.

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
