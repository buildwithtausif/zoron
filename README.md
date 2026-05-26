# Zoron-X - Ultra Intelligent Power Conservation Engine

Zoron-X is a next-generation, open-source Magisk module and Android controller app engineered to maximize battery life, control thermal throttling, and ensure peak performance via deep kernel-level optimizations.

## 📖 Official Documentation

Read the full explanation of the Zoron-X philosophy, features, and core algorithms (like the Microburst Engine and Intent Prediction) on our Official GitHub Pages site:

👉 **[ZORON-X Documentation](https://buildwithtausif.github.io/zoron/)**

## ⚖️ Root vs. Non-Root Compatibility

ZORON-X supports both **Rooted** (Magisk/KernelSU) and **Non-Rooted** devices.

> [!IMPORTANT]
> **Recommended for Root Devices**: ZORON-X is designed primarily for rooted devices to achieve optimal results. Non-root fallback mode is recommended **only** when the device has a heavily worn-out battery that cannot be replaced.

### Technical Aspects: Why Root Needs ZORON-X (and Stock Doesn't)
- **Root Devices (Custom ROMs/Kernels)**: Custom ROMs, kernels, and root modifications often bypass standard OEM power management. They can disable CPU core parking, run aggressive scaling governors, allow unmanaged background wakelocks, and prevent the SOC from entering deep suspend states. Thus, root devices need an explicit kernel-level saver like ZORON-X to force-regulate resource allocations.
- **Non-Root Devices (Stock OEM Firmware)**: Stock OEM firmware already contains highly optimized, hardware-specific power managers tuned by manufacturers. They manage system resources, app standby buckets, and doze timers natively. Adding third-party battery managers is generally unnecessary.
- **ZORON-X Fallback**: On non-root devices, ZORON-X runs a Java-based fallback engine utilizing user-granted system settings permissions to toggle sync, reduce screen timeouts/brightness, and disable power-hungry haptics dynamically. This is intended solely to squeeze extra life out of degraded, unreplaceable batteries.

## 🚀 Features at a Glance
- **Autopilot Mode**: AI-driven profile switching based on foreground app and battery level.
- **Race-To-Idle**: Finishes tasks instantly and drops into deep sleep.
- **Intent Prediction Engine**: Monitors raw hardware touch inputs to detect user activity.
- **Delta-based Fastpath**: Prevents UI stutters by only executing necessary kernel changes.
- **Interactive Dashboard**: Real-time battery graphs, process monitoring, and telemetry.
- **Bootloop Security**: Kernel panic failsafe that rolls back profiles on crashes.

## 💾 Installation
1. Download the latest `zoron_vX.X.X.zip` from the [Releases](https://github.com/buildwithtausif/zoron/releases) page.
2. Flash in Magisk or KernelSU.
3. Reboot and open the **Zoron-X** app.

## 📜 License
MIT License
