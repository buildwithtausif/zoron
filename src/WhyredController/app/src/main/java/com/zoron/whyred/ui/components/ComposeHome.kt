package com.zoron.whyred.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zoron.whyred.ui.ComposeState
import com.zoron.whyred.ui.MainActions

@Composable
fun ComposeHome(mainActions: MainActions, showSnackbar: (String) -> Unit) {
    var showOptimizationDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var legacyExpanded by remember { mutableStateOf(false) }
    
    val isTransitioning by ComposeState.isTransitioning
    val transitionProgress by ComposeState.transitionProgress
    val transitionLog by ComposeState.transitionLog

    if (isTransitioning) {
        Dialog(onDismissRequest = { }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ZORON-X TERMINAL", color = Color.Green, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("> $transitionLog", color = Color.White, style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { transitionProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Green,
                        trackColor = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(transitionProgress * 100).toInt()}%", color = Color.Green, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.End))
                }
            }
        }
    }

    if (showOptimizationDialog) {
        AlertDialog(
            onDismissRequest = { showOptimizationDialog = false },
            title = { Text("Optimization Score") },
            text = { Text("Calculated dynamically by analyzing the active CPU governor, memory pressure, and currently running background processes. 100 means fully optimized for the selected profile.") },
            confirmButton = { TextButton(onClick = { showOptimizationDialog = false }) { Text("Got it") } }
        )
    }

    if (showBatteryDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryDialog = false },
            title = { Text("Battery Health") },
            text = { Text("Calculated by checking the real-time hardware cycle count at /sys/class/power_supply/battery/cycle_count (on rooted devices) combined with charge history in the Zoron database.") },
            confirmButton = { TextButton(onClick = { showBatteryDialog = false }) { Text("Got it") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero Section (Score & Health)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BentoCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("OPTIMIZATION", style = MaterialTheme.typography.labelSmall)
                        IconButton(onClick = { showOptimizationDialog = true }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${ComposeState.optimizationScore.value}", fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text("System tuned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            BentoCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("BATTERY HEALTH", style = MaterialTheme.typography.labelSmall)
                        IconButton(onClick = { showBatteryDialog = true }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${ComposeState.batteryHealth.value}", fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text("Capacity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Status Row (Power State, Engine)
        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("POWER STATE", style = MaterialTheme.typography.labelSmall)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(ComposeState.powerState.value, fontWeight = FontWeight.Bold)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("PROFILE", style = MaterialTheme.typography.labelSmall)
                    Text(ComposeState.profile.value, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Active Engines Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (ComposeState.autopilotEnabled.value) {
                SuggestionChip(
                    onClick = { },
                    label = { Text("Autopilot Active") },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
            if (ComposeState.fastpathEnabled.value) {
                SuggestionChip(
                    onClick = { },
                    label = { Text("FastPath Engine") },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("ZORON-X MODES", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        // High Density Mode Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeCompactCard(Modifier.weight(1f), "⚡", "Balanced", { mainActions.applyMode("balanced") })
            ModeCompactCard(Modifier.weight(1f), "🔋", "Deep", { mainActions.applyMode("deep") })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeCompactCard(Modifier.weight(1f), "❄️", "Hibernate", { mainActions.applyMode("hibernation") })
            ModeCompactCard(Modifier.weight(1f), "🚀", "Burst", { mainActions.applyMode("burst") })
        }
        Spacer(modifier = Modifier.height(8.dp))
        ModeCompactCard(Modifier.fillMaxWidth(), "🌙", "Nightwatch (Ultra Deep)", { mainActions.applyMode("nightwatch") })
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Legacy Profiles Collapsible
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { legacyExpanded = !legacyExpanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LEGACY PROFILES", style = MaterialTheme.typography.labelLarge)
            Icon(
                if (legacyExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = "Expand legacy profiles"
            )
        }
        
        AnimatedVisibility(visible = legacyExpanded) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeCompactCard(Modifier.weight(1f), "📱", "Stock", { mainActions.applyMode("stock") })
                    ModeCompactCard(Modifier.weight(1f), "🔋", "Battery", { mainActions.applyMode("battery") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeCompactCard(Modifier.weight(1f), "⚖️", "Balanced", { mainActions.applyMode("balanced_legacy") })
                    ModeCompactCard(Modifier.weight(1f), "⚡", "Perform", { mainActions.applyMode("performance") })
                }
            }
        }
    }
}

@Composable
fun ModeCompactCard(modifier: Modifier, icon: String, title: String, onClick: () -> Unit) {
    BentoCard(modifier = modifier.clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontWeight = FontWeight.SemiBold)
        }
    }
}
