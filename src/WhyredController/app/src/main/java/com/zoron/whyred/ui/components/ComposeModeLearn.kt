package com.zoron.whyred.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Understanding Zoron Modes",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Zoron uses dynamic power profiles to control your device's background behavior and CPU scaling. Below is a deep dive into each mode.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider()

                ModeArticle(
                    title = "Balanced Mode",
                    lottieRes = R.raw.lottie_balanced,
                    whatItDoes = "Dynamically adjusts CPU frequency bounds to provide smooth daily usage without excessive battery drain.",
                    performanceImpact = "Smooth UI scrolling, minimal background restrictions.",
                    whenToUse = "Recommended for 90% of daily usage."
                )

                ModeArticle(
                    title = "Deep Mode",
                    lottieRes = R.raw.lottie_deep,
                    whatItDoes = "Aggressively restricts background tasks, limits maximum CPU speed, and shifts scheduler focus to battery preservation.",
                    performanceImpact = "Slightly reduced app launch speeds, much higher battery retention.",
                    whenToUse = "When you need your battery to last through a long day."
                )

                ModeArticle(
                    title = "Hibernation Mode",
                    lottieRes = R.raw.lottie_hibernation,
                    whatItDoes = "Maximum power savings. Disables all non-critical wake locks, caps CPU to absolute minimum, and freezes background syncing.",
                    performanceImpact = "Heavy UI lag, notifications may be delayed.",
                    whenToUse = "Extreme emergencies (under 10% battery)."
                )

                ModeArticle(
                    title = "Burst Mode",
                    lottieRes = R.raw.lottie_burst,
                    whatItDoes = "Performance uncapped. Uses race-to-idle methodology to complete tasks instantly and return to sleep.",
                    performanceImpact = "Maximum smoothness, generates heat under sustained load.",
                    whenToUse = "Gaming, heavy multitasking, or video rendering."
                )

                ModeArticle(
                    title = "Nightwatch",
                    lottieRes = R.raw.lottie_nightwatch,
                    whatItDoes = "Ultra-low power state designed strictly for overnight preservation. Triggers extreme doze instantly.",
                    performanceImpact = "Near-zero battery drain.",
                    whenToUse = "Before going to sleep."
                )
                
                Spacer(modifier = Modifier.height(100.dp)) // Floating nav padding
            }
        }
    }
}

@Composable
fun ModeArticle(title: String, lottieRes: Int, whatItDoes: String, performanceImpact: String, whenToUse: String) {
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            
            Text(text = "What it does", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = whatItDoes, style = MaterialTheme.typography.bodyMedium)
            
            Text(text = "Performance Impact", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = performanceImpact, style = MaterialTheme.typography.bodyMedium)
            
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                    Text(text = "💡", modifier = Modifier.padding(end = 8.dp))
                    Column {
                        Text(text = "When to use", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(text = whenToUse, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    }
}

