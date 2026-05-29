package com.zoron.whyred.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoron.whyred.ui.theme.ZoronTheme

@Composable
fun ModeLearnScreenUI() {
    ZoronTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Learn About Modes",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                InfoBentoCard(
                    icon = "⚡", title = "Balanced Mode",
                    desc = "Dynamically adjusts CPU frequency bounds to provide smooth daily usage without excessive battery drain."
                )
                InfoBentoCard(
                    icon = "🔋", title = "Deep Mode",
                    desc = "Aggressively restricts background tasks, limits maximum CPU speed, and shifts scheduler focus to battery preservation."
                )
                InfoBentoCard(
                    icon = "❄️", title = "Hibernation Mode",
                    desc = "Maximum power savings. Disables all non-critical wake locks, caps CPU to absolute minimum, and freezes background syncing."
                )
                InfoBentoCard(
                    icon = "🚀", title = "Burst Mode",
                    desc = "Performance uncapped. Uses race-to-idle methodology to complete tasks instantly and return to sleep."
                )
                InfoBentoCard(
                    icon = "🌙", title = "Nightwatch",
                    desc = "Ultra-low power state designed strictly for overnight preservation. Triggers extreme doze instantly."
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dynamic & Legacy Profiles",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                InfoBentoCard(
                    icon = "🎬", title = "Video Mode",
                    desc = "Automatically triggered by Autopilot Service when video playback is detected. Prevents frame drops while minimizing CPU clocks to reduce heat."
                )
                InfoBentoCard(
                    icon = "📱", title = "Legacy: Stock",
                    desc = "Reverts to original OEM kernel settings. Useful for testing baseline behavior."
                )
                InfoBentoCard(
                    icon = "🔋", title = "Legacy: Battery",
                    desc = "Standard legacy battery saver, forces conservative governor."
                )
                InfoBentoCard(
                    icon = "⚖️", title = "Legacy: Balanced",
                    desc = "Standard legacy balanced profile. Inferior to ZORON-X Balanced."
                )
                InfoBentoCard(
                    icon = "⚡", title = "Legacy: Perform",
                    desc = "Standard legacy performance profile. High heat output."
                )
            }
        }
    }
}

@Composable
fun InfoBentoCard(icon: String, title: String, desc: String) {
    BentoCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row {
                Text(text = icon, style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = desc, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
