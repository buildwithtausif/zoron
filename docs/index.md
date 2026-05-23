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

## Getting Started

1. Download the latest release from GitHub.
2. Flash the module zip via Magisk or KernelSU.
3. Reboot your device.
4. Open the Zoron-X companion app to monitor real-time telemetry, battery graphs, and configure Autopilot settings.
