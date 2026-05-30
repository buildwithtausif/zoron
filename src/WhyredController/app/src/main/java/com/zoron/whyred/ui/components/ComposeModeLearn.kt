package com.zoron.whyred.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.zoron.whyred.R
import com.zoron.whyred.ui.theme.ZoronTheme
import kotlinx.coroutines.launch

@Composable
fun ModeLearnScreenUI() {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    // Y-offsets for TOC scrolling (rough estimates, ideally we'd use onGloballyPositioned, but fixed values work for simple TOCs)
    val sections = listOf(
        "Core Modes" to 250,
        "Specialized Modes" to 1400,
        "Engines & Hardware" to 1900,
        "Analytics & Rules" to 2800
    )

    ZoronTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                // Table of Contents Row (Sticky Top)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sections.forEach { (title, yPos) ->
                            FilterChip(
                                selected = false,
                                onClick = { 
                                    scope.launch { scrollState.animateScrollTo(yPos) }
                                },
                                label = { Text(title, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Hero Section
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Zoron Architecture",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Deep dive into dynamic power profiles, engine mechanics, and battery management strategies. All documentation is sourced directly from Zoron-X daemon runtime scripts.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Core Modes
                    SectionHeader("Core Modes")
                    DocSection(
                        title = "Balanced Mode", lottieRes = R.raw.lottie_balanced,
                        whatItIs = "The standard operating state. Uses intermediate scaling limits.",
                        whyExists = "To provide a seamless Android experience without unnecessary drain.",
                        whenToUse = "For 90% of your daily tasks.",
                        benefits = "Smooth scrolling, responsive UI.",
                        tradeoffs = "Moderate battery consumption compared to Deep Mode.",
                        recommended = "Leave it on by default for regular usage."
                    )
                    DocSection(
                        title = "Deep Mode", lottieRes = R.raw.lottie_deep,
                        whatItIs = "Aggressive battery preservation mode. Offlines secondary CPU clusters and drops max frequencies.",
                        whyExists = "To extend battery life drastically by restricting background tasks and thermal ceilings.",
                        whenToUse = "When you need your battery to last through a long day.",
                        benefits = "Much higher battery retention, less idle drain.",
                        tradeoffs = "Slightly reduced app launch speeds, delayed sync.",
                        recommended = "Use when under 30% battery or during long trips."
                    )
                    DocSection(
                        title = "Hibernation Mode", lottieRes = R.raw.lottie_hibernation,
                        whatItIs = "Maximum power savings state. Forces aggressive cpuset grouping and Doze.",
                        whyExists = "To prevent the device from dying in emergency situations.",
                        whenToUse = "Extreme emergencies (under 10% battery).",
                        benefits = "Absolute minimum battery drain, blocks wakelocks.",
                        tradeoffs = "Heavy UI lag, notifications will be severely delayed.",
                        recommended = "Use only as a last resort."
                    )
                    DocSection(
                        title = "Burst Mode", lottieRes = R.raw.lottie_burst,
                        whatItIs = "Uncapped performance state. Onlines all cores and uses maximum scaling_max_freq.",
                        whyExists = "To utilize race-to-idle methodology for maximum throughput.",
                        whenToUse = "Gaming, heavy multitasking, or video rendering.",
                        benefits = "Maximum smoothness, zero frame drops.",
                        tradeoffs = "Generates heat under sustained load, high battery drain.",
                        recommended = "Use for specific heavy applications only."
                    )
                    DocSection(
                        title = "Nightwatch", lottieRes = R.raw.lottie_nightwatch,
                        whatItIs = "Ultra-low power state. Migrates to powersave governor and parks all but 1 active core.",
                        whyExists = "To prevent overnight battery drain without turning off the device.",
                        whenToUse = "Before going to sleep.",
                        benefits = "Near-zero battery drain overnight.",
                        tradeoffs = "Background tasks are completely suspended.",
                        recommended = "Enable before bedtime."
                    )

                    // Legacy & Special Modes
                    SectionHeader("Specialized Modes")
                    DocSection(
                        title = "Legacy Modes", lottieRes = null,
                        whatItIs = "Compatibility fallbacks for generic devices.",
                        whyExists = "Older kernels lack device-specific CPU/GPU frequency mapping tables in zoron_engine.",
                        whenToUse = "If your device hardware is unrecognized by the backend.",
                        benefits = "Prevents kernel panics by using safe generic SysFS bounds.",
                        tradeoffs = "Lacks dynamic tuning tailored to your specific Snapdragon/MediaTek SoC.",
                        recommended = "Automatically deployed if hardware detection fails."
                    )
                    DocSection(
                        title = "Video Mode", lottieRes = null,
                        whatItIs = "Dynamic media playback optimization via Fastpath.",
                        whyExists = "To prevent frame drops during high-res playback while keeping heat low.",
                        whenToUse = "Automatically invoked by Autopilot when detecting intensive media/games.",
                        benefits = "Onlines all big cores, switches to schedutil, and sets msm-adreno-tz to maximum GPU clock.",
                        tradeoffs = "Reduces background task efficiency to prioritize foreground frames.",
                        recommended = "Let Autopilot toggle this automatically via Fastpath (video_boost_on)."
                    )

                    // Engines & Hardware
                    SectionHeader("Engines & Hardware")
                    DocSection(
                        title = "FastPath Engine", lottieRes = null,
                        whatItIs = "A bypass script (zoron_fastpath.sh) for instant hardware changes.",
                        whyExists = "To reduce the latency when switching profiles without waking the main heavy zoron_engine.",
                        whenToUse = "Always active. Handles 1000ms 'microbursts' for touch inputs.",
                        benefits = "Profile switching is nearly instantaneous and touch latency is eliminated.",
                        tradeoffs = "Overrides main engine bounds temporarily.",
                        recommended = "Keep enabled unless experiencing thermal issues."
                    )
                    DocSection(
                        title = "Autopilot", lottieRes = null,
                        whatItIs = "Machine-learning driven mode switching via zoron_intent_engine.",
                        whyExists = "To remove the need for manual profile toggling.",
                        whenToUse = "Polls state every 5-120 seconds depending on device activity.",
                        benefits = "Transitions between HYPER_ACTIVE, INTERACTIVE, LIGHT_IDLE, DEEP_IDLE, and SLEEP_IDLE automatically.",
                        tradeoffs = "Polling intent adds a minor (but optimized) overhead.",
                        recommended = "Highly recommended for daily driving."
                    )
                    DocSection(
                        title = "CPU Governors", lottieRes = null,
                        whatItIs = "Kernel-level frequency scaling algorithms (schedutil, interactive, ondemand, performance).",
                        whyExists = "To dictate how the CPU ramps up or scales down frequency.",
                        whenToUse = "Configured in Settings -> CPU Governor.",
                        benefits = "Granular control over device responsiveness vs battery.",
                        tradeoffs = "Wrong governor can cause massive drain.",
                        recommended = "Leave on 'schedutil' for dynamic frequency scaling."
                    )
                    DocSection(
                        title = "GPU Governors", lottieRes = null,
                        whatItIs = "Graphics frequency algorithms (msm-adreno-tz, simple_ondemand, powersave).",
                        whyExists = "To optimize GPU power draw during UI rendering and gaming.",
                        whenToUse = "Configured in Settings -> GPU Governor.",
                        benefits = "Higher FPS or better thermal management.",
                        tradeoffs = "Can cause visual stutter if forced to powersave.",
                        recommended = "Leave on 'msm-adreno-tz' for Qualcomm devices."
                    )

                    // Analytics & Rules
                    SectionHeader("Analytics & Rules")
                    DocSection(
                        title = "Rule Engine", lottieRes = null,
                        whatItIs = "Automation daemon (zoron_rule_engine) evaluating rules.csv every 30 seconds.",
                        whyExists = "To customize behavior based on battery levels and charging state.",
                        whenToUse = "To create a personalized automated experience that overrides Autopilot.",
                        benefits = "Total control over device state triggers.",
                        tradeoffs = "Overrides other engines when conditions are met.",
                        recommended = "Use for emergency battery triggers (e.g., Battery < 15%)."
                    )
                    DocSection(
                        title = "Optimization Score", lottieRes = null,
                        whatItIs = "A calculated Compose UI metric of your device's efficiency.",
                        whyExists = "To give users a quick glance at their system configuration health.",
                        whenToUse = "Check daily to ensure features like Autopilot and Fastpath are active.",
                        benefits = "Gamifies battery management, easy to understand.",
                        tradeoffs = "Score is an aggregate of settings, not a realtime hardware benchmark.",
                        recommended = "Aim for a score above 80 by enabling Autopilot."
                    )
                    DocSection(
                        title = "Battery Health", lottieRes = null,
                        whatItIs = "Hardware battery capacity tracking directly from /sys/class/power_supply/battery.",
                        whyExists = "To monitor physical battery degradation.",
                        whenToUse = "When suspecting battery hardware issues.",
                        benefits = "Provides raw data output from the kernel fuel gauge.",
                        tradeoffs = "Accuracy depends on the kernel's tracking precision.",
                        recommended = "Monitor monthly."
                    )
                    DocSection(
                        title = "Analytics", lottieRes = null,
                        whatItIs = "Data visualization powered by zoron_process_monitor.",
                        whyExists = "To track battery drain against app oom_adj penalties.",
                        whenToUse = "Troubleshooting battery drain.",
                        benefits = "Identifies rogue apps by classifying them into Tiers S (System) through D (Denied).",
                        tradeoffs = "Process monitor adds minor overhead when active.",
                        recommended = "Review weekly to identify battery abusers."
                    )
                    
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun DocSection(
    title: String,
    lottieRes: Int?,
    whatItIs: String,
    whyExists: String,
    whenToUse: String,
    benefits: String,
    tradeoffs: String,
    recommended: String
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            if (lottieRes != null) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(56.dp).padding(end = 16.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        DocField("What it is", whatItIs)
        DocField("Why it exists", whyExists)
        
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DocField("When to use it", whenToUse, color = MaterialTheme.colorScheme.onPrimaryContainer)
                DocField("Recommended usage", recommended, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                DocField("Benefits", benefits)
            }
            Column(modifier = Modifier.weight(1f)) {
                DocField("Tradeoffs", tradeoffs)
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(top = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun DocField(label: String, text: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}
