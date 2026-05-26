# ZORON-X Documentation

Welcome to the official documentation for **ZORON-X**, the ultra-intelligent power conservation engine for Android devices.

## The ZORON-X Philosophy

Zoron-X abandons traditional continuous-throttling and static power profiles. Instead, it utilizes an aggressive **Race-To-Idle** strategy combined with deep contextual awareness:

1. **Execute Rapidly**: Workloads are executed in highly optimized microbursts. We boost frequencies and un-park cores temporarily to finish the task as fast as possible.
2. **Deep Sleep Mastery**: Once the task completes, the system is immediately forced back into a suspended state.
3. **Batching**: Network and alarm wakeups are aggregated and processed simultaneously rather than waking the device repeatedly.
4. **Context Awareness**: The device shouldn't run a generic "powersave" mode when you're gaming. Zoron-X intelligently detects your environment (screen state, touch frequency, active app, battery state) and adapts on the fly.

Our ultimate target is to achieve the **Maximum Deep Sleep Time Ratio** without compromising on UI smoothness, instant touch responsiveness, or notification reliability.

## Core Algorithms

### 1. Intent Prediction Engine
The `zoron_intent_engine` runs completely in the background without relying on heavy Android `dumpsys` commands. It reads directly from raw hardware paths (`sysfs` and `/dev/input/event*`) to monitor:
- Screen backlight states
- Raw touch inputs (to detect active user engagement vs. passive reading)
- Hardware charging states

Based on this, it predicts the user's intent and transitions the device seamlessly between five distinct states:
- **HYPER_ACTIVE**: Touch-heavy gaming. Unparks all cores, forces input boosts.
- **INTERACTIVE**: Standard UI navigation. Normal race-to-idle scheduling.
- **LIGHT_IDLE**: Reading or watching a video. Reduces timer frequency, drops input boosts.
- **DEEP_IDLE**: Screen off. Aggressive doze mode, parks 50% to 75% of CPU cores.
- **SLEEP_IDLE**: Extended screen off (over 30 minutes). Extreme app freezing and maximum core parking.

### 2. Autopilot (UsageStats Heuristic Mode)
Using Android's `UsageStatsManager`, the Autopilot service scans foreground activity every 10 seconds.
- Detects gaming apps (`pubg`, `roblox`, `genshin`) and applies the **Burst** algorithm.
- Detects media apps (`youtube`, `netflix`) and applies the **Balanced** algorithm.
- Detects the home screen or lock screen and drops the system into **Deep** hibernation.
- Preserves extreme battery during the night (battery < 20% and unplugged) by applying the **Nightwatch** profile.

### 3. Delta-Based Fastpath
Switching power profiles traditionally causes UI stutter due to rebuilding `cpuset` configurations and resetting CPU governor matrices.
Zoron-X utilizes a "Fastpath" engine (`zoron_fastpath.sh`) that intercepts profile switch commands. It compares the *requested* sysfs values against the *currently applied* sysfs values. Only deltas (changes) are actually written to the kernel. This eliminates micro-stutters during dynamic transitions and drastically lowers CPU overhead by up to 90%.

### 4. Microburst Engine
When a sudden heavy workload is detected, Zoron-X triggers a `microburst`. It instantly forces the CPU into `performance` mode, completes the execution in milliseconds, and then uses the Fastpath engine to immediately drop back to the standard profile. This guarantees snappiness without sustained battery drain.

### 5. Process Tiering Monitor
The `zoron_process_monitor` periodically scans `/proc` to evaluate running apps. It scores them based on OOM adjustments, CPU time consumed, and current state, assigning tiers (Foreground, Background, Cached, Dormant). Misbehaving background apps are aggressively frozen to prevent wakelocks.

## Root vs. Non-Root Compatibility

ZORON-X supports both **Rooted** (Magisk/KernelSU) and **Non-Rooted** devices natively.

> [!IMPORTANT]
> **Recommended for Root Devices**: ZORON-X is designed primarily for rooted devices to achieve optimal results. Non-root fallback mode is recommended **only** when the device has a heavily worn-out battery that cannot be replaced.

### Why Do Rooted Devices Require Explicit Battery Savers?
Rooted devices are often modified with custom ROMs, third-party kernels, and administrative tweaks. These custom packages frequently bypass standard OEM power configurations:
- **Core Parking**: Custom kernels may disable core parking to maximize multi-threaded synthetic benchmark scores, running all CPU clusters at high frequencies even during idle tasks.
- **Aggressive Governors**: Governor tunings in custom kernels often prioritize benchmark execution speeds over thermals/efficiency, triggering frequency spikes on minimal touch inputs.
- **Wakelocks & Background Daemons**: Root applications frequently install background daemons that hold continuous CPU wakelocks, preventing the system-on-chip (SoC) from entering its ultra-low-power `suspend-to-RAM` (deep sleep) state.
- **Custom Doze Profiles**: Rom configurations often disable or extend Android's aggressive Doze window, resulting in significant overnight standby battery drain (often 10% to 20%).

Due to these factors, rooted devices require an explicit battery saver like ZORON-X to actively park unused cores, throttle scaling governors, batch doze alarms, and restrict aggressive background threads.

### Why Do Stock (Non-Rooted) Devices Not Require It?
Stock non-rooted devices running factory firmware (Pixel, Samsung, OnePlus, etc.) are already tuned at the hardware level by the OEM's battery engineers:
- **Hardware-Specific Tuning**: Governors, cpuset limits, scheduler parameters, and task placement policies are optimized for that specific SoC and board configuration.
- **App Standby Buckets**: OEM firmware natively enforces restrictive standby buckets, preventing cached/dormant applications from executing background threads.
- **Optimized Idle Suspends**: The factory ROM ensures the device enters deep sleep efficiently within minutes of screen-off.

Because stock devices are highly optimized out-of-the-box, adding third-party battery savers can introduce redundant background overhead.

### ZORON-X Non-Root Fallback Engine
ZORON-X provides a Non-Root Fallback mode for cases where a user has a stock device with a **heavily degraded (worn-out) battery** that cannot be physically replaced. In this fallback mode, ZORON-X runs a Java-based daemon to apply supplementary settings-level optimizations:
- Toggles system-wide master sync dynamically to prevent sync adapters from keeping the CPU awake.
- Caps maximum screen off timeouts to 15 seconds to prevent accidental battery drain.
- Sets manual screen brightness limits and toggles off automatic brightness spikes.
- Disables haptic motor feedback and touch sound effects to conserve extra battery cycles.
- Enforces an AMOLED pure black theme (`#000000`) across all controller activity layouts to minimize display power draw in deep saving profiles.

---

## Getting Started

1. Download the latest release from GitHub.
2. Flash the module zip via Magisk or KernelSU.
3. Reboot your device.
4. Open the Zoron-X companion app to monitor real-time telemetry, battery graphs, and configure Autopilot settings.
