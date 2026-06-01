package com.zoron.whyred.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.zoron.whyred.R
import com.zoron.whyred.ui.theme.ZoronTheme
import kotlinx.coroutines.launch

data class DocTopic(
    val title: String,
    val whatItIs: String,
    val whyExists: String,
    val whenToUse: String,
    val benefits: String,
    val tradeoffs: String,
    val recommended: String,
    val lottieRes: Int? = null
)

val documentationTopics = listOf(
    DocTopic(
        title = "Balanced Mode",
        lottieRes = R.raw.lottie_balanced,
        whatItIs = "The standard operating state. Uses intermediate scaling limits.",
        whyExists = "To provide a seamless Android experience without unnecessary drain.",
        whenToUse = "For 90% of your daily tasks.",
        benefits = "Smooth scrolling, responsive UI.",
        tradeoffs = "Moderate battery consumption compared to Deep Mode.",
        recommended = "Leave it on by default for regular usage."
    ),
    DocTopic(
        title = "Deep Mode",
        lottieRes = R.raw.lottie_deep,
        whatItIs = "Aggressive battery preservation mode. Offlines secondary CPU clusters and drops max frequencies.",
        whyExists = "To extend battery life drastically by restricting background tasks and thermal ceilings.",
        whenToUse = "When you need your battery to last through a long day.",
        benefits = "Much higher battery retention, less idle drain.",
        tradeoffs = "Slightly reduced app launch speeds, delayed sync.",
        recommended = "Use when under 30% battery or during long trips."
    ),
    DocTopic(
        title = "Burst Mode",
        lottieRes = R.raw.lottie_burst,
        whatItIs = "Uncapped performance state (internally tracked as Burst). Onlines all cores and removes frequency limits.",
        whyExists = "To utilize race-to-idle methodology for maximum throughput.",
        whenToUse = "Gaming, heavy multitasking, or video rendering.",
        benefits = "Maximum smoothness, zero frame drops.",
        tradeoffs = "Generates heat under sustained load, high battery drain.",
        recommended = "Use for specific heavy applications only."
    ),
    DocTopic(
        title = "Battery Mode",
        lottieRes = R.raw.lottie_hibernation,
        whatItIs = "Maximum power savings state (Hibernation/Nightwatch). Forces aggressive cpuset grouping and Doze.",
        whyExists = "To prevent the device from dying in emergency situations.",
        whenToUse = "Extreme emergencies (under 10% battery) or overnight.",
        benefits = "Absolute minimum battery drain, blocks wakelocks.",
        tradeoffs = "Heavy UI lag, notifications will be severely delayed.",
        recommended = "Use only as a last resort or when sleeping."
    ),
    DocTopic(
        title = "Legacy Modes",
        whatItIs = "Compatibility fallbacks for generic devices.",
        whyExists = "Older kernels lack device-specific CPU/GPU frequency mapping tables in zoron_engine.",
        whenToUse = "If your device hardware is unrecognized by the backend.",
        benefits = "Prevents kernel panics by using safe generic SysFS bounds.",
        tradeoffs = "Lacks dynamic tuning tailored to your specific SoC.",
        recommended = "Automatically deployed if hardware detection fails."
    ),
    DocTopic(
        title = "FastPath",
        whatItIs = "A bypass script (zoron_fastpath.sh) for instant hardware changes.",
        whyExists = "To reduce the latency when switching profiles without waking the main heavy zoron_engine.",
        whenToUse = "Always active. Handles 1000ms 'microbursts' for touch inputs.",
        benefits = "Profile switching is nearly instantaneous and touch latency is eliminated.",
        tradeoffs = "Overrides main engine bounds temporarily.",
        recommended = "Keep enabled unless experiencing thermal issues."
    ),
    DocTopic(
        title = "Autopilot",
        whatItIs = "Machine-learning driven mode switching via zoron_intent_engine.",
        whyExists = "To remove the need for manual profile toggling.",
        whenToUse = "Polls state every 5-120 seconds depending on device activity.",
        benefits = "Transitions between HYPER_ACTIVE, INTERACTIVE, LIGHT_IDLE, DEEP_IDLE, and SLEEP_IDLE automatically.",
        tradeoffs = "Polling intent adds a minor (but optimized) overhead.",
        recommended = "Highly recommended for daily driving."
    ),
    DocTopic(
        title = "Video Mode",
        whatItIs = "Dynamic media playback optimization.",
        whyExists = "To prevent frame drops during high-res playback while keeping heat low.",
        whenToUse = "Automatically invoked by Autopilot via Fastpath's 'video_boost_on' hook.",
        benefits = "Onlines all big cores, switches to schedutil, removes scaling_max_freq limits, and pins GPU to max via msm-adreno-tz.",
        tradeoffs = "Reduces background task efficiency to prioritize foreground frames. Forces maximum GPU draw.",
        recommended = "Let Autopilot toggle this automatically. Cannot be manually invoked."
    ),
    DocTopic(
        title = "CPU Governors",
        whatItIs = "Kernel-level frequency scaling algorithms (schedutil, interactive, ondemand, performance).",
        whyExists = "To dictate how the CPU ramps up or scales down frequency.",
        whenToUse = "Configured in Settings -> CPU Governor.",
        benefits = "Granular control over device responsiveness vs battery.",
        tradeoffs = "Wrong governor can cause massive drain.",
        recommended = "Leave on 'schedutil' for dynamic frequency scaling."
    ),
    DocTopic(
        title = "GPU Governors",
        whatItIs = "Graphics frequency algorithms (msm-adreno-tz, simple_ondemand, powersave).",
        whyExists = "To optimize GPU power draw during UI rendering and gaming.",
        whenToUse = "Configured in Settings -> GPU Governor.",
        benefits = "Higher FPS or better thermal management.",
        tradeoffs = "Can cause visual stutter if forced to powersave.",
        recommended = "Leave on 'msm-adreno-tz' for Qualcomm devices."
    ),
    DocTopic(
        title = "Rule Engine",
        whatItIs = "Automation daemon (zoron_rule_engine) evaluating rules.csv every 30 seconds.",
        whyExists = "To customize behavior based on battery levels and charging state.",
        whenToUse = "To create a personalized automated experience that overrides Autopilot.",
        benefits = "Total control over device state triggers.",
        tradeoffs = "Overrides other engines when conditions are met.",
        recommended = "Use for emergency battery triggers (e.g., Battery < 15%)."
    ),
    DocTopic(
        title = "Optimization Score",
        whatItIs = "A calculated Compose UI metric of your device's efficiency.",
        whyExists = "To give users a quick glance at their system configuration health.",
        whenToUse = "Check daily to ensure features like Autopilot and Fastpath are active.",
        benefits = "Gamifies battery management, easy to understand.",
        tradeoffs = "Score is an aggregate of settings, not a realtime hardware benchmark.",
        recommended = "Aim for a score above 80 by enabling Autopilot."
    ),
    DocTopic(
        title = "Battery Health",
        whatItIs = "Hardware battery capacity tracking directly from /sys/class/power_supply/battery.",
        whyExists = "To monitor physical battery degradation.",
        whenToUse = "When suspecting battery hardware issues.",
        benefits = "Provides raw data output from the kernel fuel gauge.",
        tradeoffs = "Accuracy depends on the kernel's tracking precision.",
        recommended = "Monitor monthly."
    ),
    DocTopic(
        title = "Analytics",
        whatItIs = "Data visualization powered by zoron_process_monitor.",
        whyExists = "To track battery drain against app oom_adj penalties.",
        whenToUse = "Troubleshooting battery drain.",
        benefits = "Identifies rogue apps by classifying them into Tiers S (System) through D (Denied).",
        tradeoffs = "Process monitor adds minor overhead when active.",
        recommended = "Review weekly to identify battery abusers."
    ),
    DocTopic(
        title = "Adaptive Learning",
        whatItIs = "On-device intelligence that tracks your manual profile overrides and learns your app preferences.",
        whyExists = "To personalize the Autopilot experience over time, reducing the need for manual toggles.",
        whenToUse = "Always active alongside Autopilot. Works automatically in the background.",
        benefits = "Gradually molds device behavior to your specific usage habits. Runs entirely offline.",
        tradeoffs = "Takes time to build confidence in patterns. Can be temporarily confused by abnormal usage.",
        recommended = "Leave enabled. It decays outdated patterns automatically after 7 days."
    )
)

@Composable
fun ModeLearnScreenUI() {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showTocOverlay by remember { mutableStateOf(false) }
    
    // First item is the Hero, so topics start at index 1
    val activeIndex by remember { derivedStateOf { maxOf(0, listState.firstVisibleItemIndex - 1) } }

    ZoronTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                
                // MAIN PANE: Content
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp) // padding for FAB
                ) {
                    item {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Zoron Architecture",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Deep dive into dynamic power profiles, engine mechanics, and battery management strategies. All documentation is sourced directly from Zoron-X daemon runtime scripts.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    itemsIndexed(documentationTopics) { _, topic ->
                        DocSectionRender(topic)
                    }
                }
                
                // FLOATING TOC BUTTON
                FloatingActionButton(
                    onClick = { showTocOverlay = !showTocOverlay },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "Toggle TOC")
                }

                // GLASSMORPHIC TOC OVERLAY
                AnimatedVisibility(
                    visible = showTocOverlay,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            .clickable { showTocOverlay = false } // Click outside to close
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .fillMaxHeight()
                                .align(Alignment.Center)
                                .padding(vertical = 32.dp),
                            contentPadding = PaddingValues(bottom = 64.dp)
                        ) {
                            item {
                                Text(
                                    text = "TABLE OF CONTENTS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                                )
                            }
                            itemsIndexed(documentationTopics) { index, topic ->
                                val isSelected = activeIndex == index
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable {
                                            showTocOverlay = false
                                            scope.launch { listState.animateScrollToItem(index + 1) }
                                        }
                                        .padding(vertical = 14.dp, horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = topic.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocSectionRender(topic: DocTopic) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            if (topic.lottieRes != null) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(topic.lottieRes))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(48.dp).padding(end = 16.dp)
                )
            }
            Text(
                text = topic.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        DocField("What it is", topic.whatItIs)
        DocField("Why it exists", topic.whyExists)
        
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DocField("When to use it", topic.whenToUse, color = MaterialTheme.colorScheme.onPrimaryContainer)
                DocField("Recommended usage", topic.recommended, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            DocField("Benefits", topic.benefits)
            Spacer(modifier = Modifier.height(8.dp))
            DocField("Tradeoffs", topic.tradeoffs)
        }
        
        HorizontalDivider(modifier = Modifier.padding(top = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun DocField(label: String, text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
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
