# Zoron - Whyred Battery & Thermal Optimizer

Zoron is an advanced, open-source Magisk module and Android controller app designed specifically for the `whyred` (SDM636/660) device running Android 14+ Custom ROMs (e.g., Project Matrixx).

It forcefully optimizes your device's battery life, thermal throttling, and overall performance through deep kernel-level adjustments.

## Features

- **Dynamic Kernel Profiles:** Instantly switch between "No Tweak (Stock)", "Battery Saver", "Balanced", and "Performance" profiles without rebooting.
- **Real-time System Status Dashboard:** See exactly what CPU Governor is currently active at a glance.
- **Interactive Battery Graphing:** A microscopic background script silently tracks your battery drain every 10 minutes and uses `MPAndroidChart` to plot a comparative multi-line graph directly on your dashboard. See exactly which profile saves you the most battery!
- **Bootloop Security:** Employs a kernel panic failsafe. If your device crashes while applying an aggressive profile, Zoron detects the crash on reboot and instantly rolls back to the safe `battery` profile to prevent bootloops.
- **Persistent Root Shell:** Engineered using topjohnwu's `libsu` for lightning-fast command execution and zero "Root Granted" toast spam.
- **Dual OTA Updates:** Fully supports automatic Magisk background zip updates, as well as an in-app minor update checker for seamless APK updates.

## Installation

1. Download the latest `zoron_optimizations_vX.X.X.zip` from the [Releases](https://github.com/buildwithtausif/zoron/releases) page.
2. Open Magisk Manager -> Modules -> Install from storage.
3. Select the zip file and Reboot.
4. Open the **Zoron** app from your app drawer and grant it Root permissions.

## Profiles

- **No Tweak (Stock Device):** Restores stock Android frequencies and thermal behavior.
- **Battery Saver:** Limits CPU to 1.4GHz, enforces strict thermal limits, aggressively caches memory.
- **Balanced:** Normal CPU limits (1.6GHz), default thermals.
- **Performance:** Highest frequencies (1.8GHz), relaxed thermals, maximum GPU clock speed.

## Building from Source

This project consists of the Magisk module template and an Android Studio project.

```bash
git clone https://github.com/buildwithtausif/zoron.git
cd zoron/WhyredController
./gradlew assembleDebug
```
Copy the compiled APK into `whyred_module/Zoron.apk` and compress the `whyred_module` folder into a ZIP file to create the flashable Magisk module.

## License
MIT License
