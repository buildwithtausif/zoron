# Zoron-X - Ultra Intelligent Power Conservation Engine

Zoron-X is a next-generation, open-source Magisk module and Android controller app engineered specifically for the `whyred` (SDM636/660) device running Android 14+ Custom ROMs (e.g., Project Matrixx). 

It forcefully optimizes your device's battery life, thermal throttling, and overall performance through deep kernel-level adjustments and an intelligent power conservation architecture.

## The Zoron-X Core Philosophy
Zoron-X abandons traditional continuous-throttling methods. Instead, it utilizes a **Race-To-Idle** strategy:
- **Execute workloads rapidly in bursts.**
- **Complete tasks ASAP.**
- **Immediately return the system to deep idle.**
- **Batch all possible wakeups together.**
- **Aggressively freeze non-essential tasks.**
- **Intelligently prioritize foreground usability.**

The ultimate target is to achieve the **Maximum Deep Sleep Time Ratio** without compromising on UI smoothness, instant touch responsiveness, or notification reliability.

## Zoron-X Dynamic Power States
Zoron-X dynamically scales between 5 different power states based on user intent and interaction:
- **State 0 (Hyper Active):** Gaming, rapid touch input. Big cores enabled, normal touchboost, minimal latency.
- **State 1 (Interactive):** Normal usage (browsing, messaging). Race-to-idle scheduling, GPU adaptive scaling.
- **State 2 (Light Idle):** Static screen, reading. Reduced timer frequency, network batching, parked cores.
- **State 3 (Deep Idle):** Screen off. Aggressive doze, suspended non-whitelisted apps, maximum CPU parking.
- **State 4 (Sleep Idle):** Overnight/Long idle. Maintenance windows every 30-60 mins, extreme app freezing.

## Features

- **Process Priority Engine:** Dynamically assigns priority to every process based on user visibility, interaction history, and battery impact. Apps are tiered from S (Foreground) to D (Dormant).
- **Wakeup Batching Engine:** Minimizes system wakeups by combining alarms, syncs, network requests, and maintenance tasks.
- **Interactive Battery Graphing:** A microscopic background script silently tracks your battery drain every 10 minutes and uses `MPAndroidChart` to plot a comparative multi-line graph directly on your dashboard.
- **Bootloop Security:** Employs a kernel panic failsafe. If your device crashes while applying an aggressive profile, Zoron-X detects the crash on reboot and instantly rolls back to a safe profile.
- **Persistent Root Shell:** Engineered using topjohnwu's `libsu` for lightning-fast command execution and zero "Root Granted" toast spam.
- **Dual OTA Updates:** Fully supports automatic Magisk background zip updates, as well as an in-app minor update checker for seamless APK updates.

## Installation

1. Download the latest `zoron_optimizations_vX.X.X.zip` from the [Releases](https://github.com/buildwithtausif/zoron/releases) page.
2. Open Magisk Manager -> Modules -> Install from storage.
3. Select the zip file and Reboot.
4. Open the **Zoron-X** app from your app drawer and grant it Root permissions.

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
