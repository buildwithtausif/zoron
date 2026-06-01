# Zoron Changelog

## v4.5.22
- **Adaptive Learning**: Added an on-device machine learning model that tracks your manual profile overrides and adapts the Autopilot.
- **Autopilot Refactor**: Added hysteresis, flapping prevention, and debouncing to prevent excessive state switches.
- **FastPath Smooth Transitions**: Enabled `schedutil` rate limiting during scaling to prevent micro-stutters and UI jank.
- **Analytics UI**: Added an interactive "Learning Patterns" tab to monitor the system's learning progress.

## v4.5.16
- **Analytics**: Fixed missing X-axis labels on battery chart with human-readable time formatting (5m, 1h, 2h30m).
- **What's New**: Fixed changelog parser to correctly handle markdown `##` headers, fully populating release history.

## v4.5.15
- **Documentation**: Corrected mode naming, replacing legacy 'Gaming Mode' references with 'Burst Mode'.

## v4.5.14
- **Analytics**: Implemented strict X-axis bucketing to permanently resolve Vico chart crashes and visualized statistical guidelines (Mean) directly on the trend chart.
- **Documentation**: Removed deprecated FAQs and Troubleshooting sections from the Learn page.
- **What's New**: Implemented robust fallback to bundled changelog.md when OTA metadata is unreachable.

## v4.5.13
- **Analytics Hotfix**: Removed dynamic Vico viewport overriders. The graph now perfectly matches the exact Y-bounds of the real battery data.

## v4.5.12
- **Analytics**: Implemented advanced bucketing compression and added Math stats (Mean, Variance, StdDev) while strictly fixing viewport rendering bugs.
- **Documentation**: Implemented an all-new Wiki-style split-pane UX with Table of Contents auto-highlighting. Added specific facts for Gaming, Battery, and Video Modes.
- **What's New**: Implemented date parsing and explicit empty states for seamless changelog rendering.

## v4.5.11
- **Analytics Fix**: Refactored Vico chart async model loading to fix empty model crashes permanently and handle invalid rendering gracefully.
- **UI Redesign**: Transformed Settings changelog into a dedicated 'What's New' screen parsed dynamically from OTA metadata.
- **Documentation**: Overhauled 'Learn Hub' with 100% factual data retrieved from backend script reverse-engineering (Fastpath, Intent Engine, Rule Engine, etc.), adding a new Table of Contents for rapid navigation.


## v4.5.10
- **Analytics Fix**: Resolved a crash in the Analytics tab caused by the charting library rejecting identical timestamps when processing battery telemetry.


## v4.5.9
- **UI Modernization**: Redesigned 'About Modes' into a full documentation experience with comprehensive details for 10 missing mode sections.
- **What's New Fix**: Resolved 'What's New' page showing empty placeholders by fetching and displaying live OTA metadata history.
- **Navigation**: Reverted floating pill navigation to a fixed bottom bar with a translucent frosted-glass effect.

## v4.5.6
- **Analytics UI**: Decluttered the Analytics header by removing the "Trend Reads" dialog button and rendering smart insights directly inline below the chart.
- **Chart Accuracy**: Dynamic axis scaling is now implemented. The chart axes will dynamically stretch around minimum and maximum thresholds, eliminating flat lines and ensuring visual trends are immediately obvious and meaningful.

## v4.5.5
- **UX Enhancements**: Redesigned Rule Creation Dialog with human-readable labels, contextual hints, and examples.
- **Trend Reads (Analytics)**: Introduced a dynamic algorithmic parser for Battery and Process analytics that generates human-readable insights, summaries, and recommendations directly within the app.
- **Analytics Charts**: Fixed blank chart rendering issues and added contextual markers/axes for Battery Discharge Trends.
- **Backend Optimization**: Exempted the Zoron Controller app (`com.zoron.whyred`) from the engine's aggressive background killer in extreme and restricted modes, ensuring stable operation.

## v4.5.4
- **Backend Optimizations**: Fixed engine validation logic to prevent false FAILs for zRAM and I/O schedulers. Added support for fallback schedulers (`mq-deadline`, `kyber`, `bfq`).
- **Engine FastPath**: Reduced redundant engine reapplication loops by utilizing FastPath for burst optimizations.
- **Compose Migration Complete**: Migrated the Rule Editor and OTA Updater from legacy views directly into Jetpack Compose.
- **Analytics Exporter**: Added data export capabilities, allowing users to save CSV logs and process reports directly to the `Downloads/Zoron` directory.
- **Governor Tuning**: Added GPU Governor selection via the UI with detailed mode documentation outlining hardware impacts.

## v4.5.3
- **Compose UI Cleanup**: Finalized the Compose migration by completely removing legacy XML dependencies and Views from the app host.
- **Analytics UI**: Redesigned Analytics using Vico charts with Material You styling and added data export capabilities.
- **Rules Dashboard**: Replaced the legacy Rule Activity with a pure Compose-based Rules dashboard and dialog.
- **Modern Dialogs**: Migrated mode transitions from Termux-style popups to native Material 3 alert dialogs.
## v4.5.2
- **UI/UX Fixes**: Restored Analytics Page (Charts, Process Monitor, Diagnostics) entirely in Compose using Vico charts.
- **Rules**: Rebuilt Rules dashboard in Compose.
- **Modes & Navigation**: Restored Mode transition dialog with a modern Termux-style console, replaced navigation emojis with Material icons, and restored Legacy & Video Mode explanations.
- **Core Integration**: Fixed dynamic reading of sysfs battery cycles and optimization calculations on the Home dashboard.
## v4.5.1-fix
- Fixed Compose interoperability bugs and UI scaling issues.
- Restored missing engine settings toggles to Compose UI.
- Rewired legacy Java diagnostic export flows into the new dashboard navigation.
- Fixed layout structure to prevent null pointers on MainActivity inflation.

# v4.5.1
- **UI/UX Modernization**: Completely migrated the UI layer to Jetpack Compose for a fluid, modern experience.
- **Premium Dark Aesthetic**: Introduced a dark-first design system with custom bento-style elevated cards and vibrant accent colors.
- **Navigation Redesign**: Replaced legacy overflow menus with an expressive, auto-hiding floating bottom navigation bar.
- **Modern Analytics**: Upgraded MPAndroidChart visuals to feature smooth bezier curves and rich gradients.
- **Global Dialogs**: Standardized all app prompts, settings toggles, and alerts to match the new Compose design language.

## v4.5.0
- **Intelligent Rule Engine**: Added `ZoronAutopilotService` to continuously evaluate user-defined hardware conditional rules (e.g., `IF BATTERY_BELOW (15) THEN SET_MODE (NIGHTWATCH)`).
- **Rule Editor UI**: Built a native Android activity allowing users to easily configure custom conditionals directly in the app.
- **Battery Health Dashboard**: Integrated an on-device Battery Health tracking module, actively estimating `healthScore` based on cumulative charge cycles mapped against a 500-cycle degradation baseline.
- **Audit & Recommendation Engine**: Added passive telemetry analysis that scans raw system data (`process_report.txt`, `power_state.txt`) to surface high-confidence actionable optimization recommendations to the user (e.g., "Restrict background battery for com.android.chrome").
- **Room Database Migration**: Fully migrated all legacy CSV-based file logging into a highly performant local SQLite Room Database (`ZoronDatabase`).


## v4.4.2
- **CRITICAL FIX — Infinite Update Loop**: The `module.prop` was never bumped from v4.4.0 (versionCode 31) in the v4.4.1 release. This caused Magisk Manager and the in-app OTA to perpetually detect an "update available" — downloading and flashing the zip would reinstall the same module with versionCode 31, while `update.json` advertised versionCode 32. The loop repeated infinitely.
- **Improved Update Comparison**: Changed the OTA version check from `!=` to `>` so the app only prompts for updates when the server has a *strictly newer* version. Previously, any version mismatch (including accidental downgrades) would trigger the update dialog.
- **Version String Sync**: Synchronized all hardcoded version strings across `customize.sh` (was stuck at v3.5.0) and `service.sh` (was stuck at v4.0.0) to reflect the current release.

## v4.4.1
- Added in-app Non-Root advisory dialog with 'Learn More' and 'Don't show again' options.
- Minor UI and documentation updates.
- **Known Issue**: Module zip shipped with stale `module.prop` (v4.4.0/versionCode 31), causing infinite update loop. Fixed in v4.4.2.

## v4.4.0
- **Non-Root Device Fallback**: Introduced native fallback support for non-root devices. The app runs a sandboxed optimization daemon when root is missing, preventing blocking the user.
- **Simulated Sandbox Diagnostics**: Simulated `profile.txt`, `log.txt`, `battery.csv`, and `process_report.txt` in the local app directory (`context.getFilesDir()`) for seamless dashboard and chart updates on non-root.
- **Java Fallback Engine**: Configured custom system-level optimizations using Android APIs (Master Sync, screen off timeout, screen brightness, haptics overrides, and touch sound management).
- **Non-Root Advisory Dialog**: Added an in-app advisory that explains the technical limitations of non-root operation. It appears on first launch for non-root devices and provides a "Don't show this advisory again" checkbox and a "Learn More" button to open detailed documentation.
- **AMOLED Pure Black Mode**: Dynamically switches the app background to solid black (`#000000`) instead of gradient dark purple when a deep battery saving profile is active to save extra OLED display power.
- **Developer Attributions**: Added developer attribution card acknowledging **Tausif Alam aka buildwithtausif** with a direct repository link in both the main dashboard and settings pages.

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
