package com.zoron.whyred.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.zoron.whyred.R
import com.zoron.whyred.ui.theme.ZoronTheme

@Composable
fun ModeLearnScreenUI() {
    ZoronTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
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
                        text = "Master your device. Deep dive into dynamic power profiles, engine mechanics, and battery management strategies.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Core Modes
                SectionHeader("Core Modes")
                DocSection(
                    title = "Balanced Mode", lottieRes = R.raw.lottie_balanced,
                    whatItIs = "The standard operating state of Zoron.",
                    whyExists = "To provide a seamless Android experience without unnecessary drain.",
                    whenToUse = "For 90% of your daily tasks.",
                    benefits = "Smooth scrolling, responsive UI.",
                    tradeoffs = "Moderate battery consumption compared to Deep Mode.",
                    recommended = "Leave it on by default for regular usage."
                )
                DocSection(
                    title = "Deep Mode", lottieRes = R.raw.lottie_deep,
                    whatItIs = "Aggressive battery preservation mode.",
                    whyExists = "To extend battery life drastically by restricting background tasks.",
                    whenToUse = "When you need your battery to last through a long day.",
                    benefits = "Much higher battery retention, less idle drain.",
                    tradeoffs = "Slightly reduced app launch speeds, delayed sync.",
                    recommended = "Use when under 30% battery or during long trips."
                )
                DocSection(
                    title = "Hibernation Mode", lottieRes = R.raw.lottie_hibernation,
                    whatItIs = "Maximum power savings state.",
                    whyExists = "To prevent the device from dying in emergency situations.",
                    whenToUse = "Extreme emergencies (under 10% battery).",
                    benefits = "Absolute minimum battery drain, disables wake locks.",
                    tradeoffs = "Heavy UI lag, notifications will be severely delayed.",
                    recommended = "Use only as a last resort."
                )
                DocSection(
                    title = "Burst Mode", lottieRes = R.raw.lottie_burst,
                    whatItIs = "Uncapped performance state.",
                    whyExists = "To utilize race-to-idle methodology for maximum throughput.",
                    whenToUse = "Gaming, heavy multitasking, or video rendering.",
                    benefits = "Maximum smoothness, zero frame drops.",
                    tradeoffs = "Generates heat under sustained load, high battery drain.",
                    recommended = "Use for specific heavy applications only."
                )
                DocSection(
                    title = "Nightwatch", lottieRes = R.raw.lottie_nightwatch,
                    whatItIs = "Ultra-low power state for overnight preservation.",
                    whyExists = "To prevent overnight battery drain without turning off the device.",
                    whenToUse = "Before going to sleep.",
                    benefits = "Near-zero battery drain overnight.",
                    tradeoffs = "Background tasks are paused until morning.",
                    recommended = "Enable before bedtime."
                )

                // Legacy & Special Modes
                SectionHeader("Specialized Modes")
                DocSection(
                    title = "Legacy Modes", lottieRes = null,
                    whatItIs = "Compatibility profiles for older Android versions (Android 9/10).",
                    whyExists = "Older kernels do not support modern dynamic scheduling.",
                    whenToUse = "If you are running an outdated custom ROM.",
                    benefits = "Prevents kernel panics and UI freezes on older devices.",
                    tradeoffs = "Lacks dynamic tuning, lower efficiency.",
                    recommended = "Only use if modern profiles fail to apply."
                )
                DocSection(
                    title = "Video Mode", lottieRes = null,
                    whatItIs = "Media playback optimization.",
                    whyExists = "To prevent frame drops during high-res video playback while keeping CPU low.",
                    whenToUse = "Watching 4K/HDR content or long movies.",
                    benefits = "Perfect 60fps playback, low heat generation.",
                    tradeoffs = "Background app performance is reduced.",
                    recommended = "Toggle automatically via Rule Engine when opening media apps."
                )

                // Engines & Features
                SectionHeader("Engines & Hardware")
                DocSection(
                    title = "FastPath Engine", lottieRes = null,
                    whatItIs = "A caching mechanism for kernel parameters.",
                    whyExists = "To reduce the latency when switching between Zoron profiles.",
                    whenToUse = "Always active by default in settings.",
                    benefits = "Profile switching is nearly instantaneous.",
                    tradeoffs = "Consumes ~5MB of RAM for cache.",
                    recommended = "Keep enabled unless experiencing memory issues."
                )
                DocSection(
                    title = "Autopilot", lottieRes = null,
                    whatItIs = "Machine-learning driven mode switching.",
                    whyExists = "To remove the need for manual profile toggling.",
                    whenToUse = "If you want Zoron to manage everything automatically.",
                    benefits = "Adapts to your usage patterns and active apps seamlessly.",
                    tradeoffs = "May occasionally select a sub-optimal profile for unknown apps.",
                    recommended = "Highly recommended for daily driving."
                )
                DocSection(
                    title = "CPU Governors", lottieRes = null,
                    whatItIs = "Kernel-level frequency scaling algorithms.",
                    whyExists = "To dictate how the CPU ramps up or scales down frequency.",
                    whenToUse = "Advanced users who want specific frequency scaling behaviors.",
                    benefits = "Granular control over device responsiveness vs battery.",
                    tradeoffs = "Wrong governor can cause massive drain or extreme lag.",
                    recommended = "Leave on 'schedutil' unless you know what you are doing."
                )
                DocSection(
                    title = "GPU Governors", lottieRes = null,
                    whatItIs = "Graphics frequency scaling algorithms.",
                    whyExists = "To optimize GPU power draw during UI rendering and gaming.",
                    whenToUse = "Tweaking game performance.",
                    benefits = "Higher FPS or better thermal management.",
                    tradeoffs = "Can cause visual stutter if misconfigured.",
                    recommended = "Leave on default."
                )

                // Analytics & Rules
                SectionHeader("Analytics & Rules")
                DocSection(
                    title = "Rule Engine", lottieRes = null,
                    whatItIs = "Automation system based on app triggers and device state.",
                    whyExists = "To customize behavior (e.g., enable Burst mode when opening PUBG).",
                    whenToUse = "To create a personalized automated experience.",
                    benefits = "Total control over device state per-app.",
                    tradeoffs = "Complex rules can conflict with Autopilot.",
                    recommended = "Use for specific gaming or reading apps."
                )
                DocSection(
                    title = "Optimization Score", lottieRes = null,
                    whatItIs = "A calculated metric of your device's efficiency.",
                    whyExists = "To give users a quick glance at their system health.",
                    whenToUse = "Check daily to ensure no rogue apps are draining battery.",
                    benefits = "Gamifies battery management, easy to understand.",
                    tradeoffs = "Score is an estimate and not an absolute benchmark.",
                    recommended = "Aim for a score above 80."
                )
                DocSection(
                    title = "Battery Health", lottieRes = null,
                    whatItIs = "Hardware battery capacity tracking.",
                    whyExists = "To monitor battery degradation over time.",
                    whenToUse = "When suspecting battery hardware issues.",
                    benefits = "Helps decide when to replace the physical battery.",
                    tradeoffs = "Requires multiple charge cycles to calibrate accurately.",
                    recommended = "Monitor monthly."
                )
                DocSection(
                    title = "Analytics", lottieRes = null,
                    whatItIs = "Comprehensive charting of CPU, GPU, and battery metrics.",
                    whyExists = "To provide deep insights into device behavior over time.",
                    whenToUse = "Troubleshooting battery drain or performance drops.",
                    benefits = "Visual proof of Zoron's effectiveness.",
                    tradeoffs = "Slight overhead to record data points.",
                    recommended = "Review weekly for usage trends."
                )
                
                Spacer(modifier = Modifier.height(32.dp))
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
