package com.zoron.whyred.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoron.whyred.ui.ComposeState
import com.zoron.whyred.ui.MainActions

@Composable
fun SettingsScreenUI(
    onToggleAutoPilot: (Boolean) -> Unit,
    isAutoPilotEnabled: Boolean,
    onCheckUpdate: () -> Unit,
    mainActions: MainActions? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("SETTINGS", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
        
        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggle(
                    title = "Autopilot Mode",
                    subtitle = "Adapts to active apps and battery",
                    checked = isAutoPilotEnabled,
                    onCheckedChange = onToggleAutoPilot
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                SettingToggle(
                    title = "Fastpath Engine",
                    subtitle = "Quick-apply cached parameters",
                    checked = ComposeState.fastpathEnabled.value,
                    onCheckedChange = { 
                        ComposeState.fastpathEnabled.value = it 
                        mainActions?.setPreferenceBoolean("fastpath_enabled", it)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                SettingToggle(
                    title = "Adaptive Learning",
                    subtitle = "Learn usage patterns",
                    checked = ComposeState.adaptiveLearningEnabled.value,
                    onCheckedChange = { 
                        ComposeState.adaptiveLearningEnabled.value = it 
                        mainActions?.setPreferenceBoolean("adaptive_learning", it)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("DIAGNOSTICS & EXPORT", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))

        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggle(
                    title = "Export as ZIP",
                    subtitle = "Bundle all logs into a compressed archive",
                    checked = ComposeState.zipExportEnabled.value,
                    onCheckedChange = { 
                        ComposeState.zipExportEnabled.value = it 
                        mainActions?.setPreferenceBoolean("zip_export", it)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = { mainActions?.clearLogs() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear Data")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = { mainActions?.exportLogs(ComposeState.zipExportEnabled.value) }) {
                        Text("Export Logs")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggle(
                    title = "Developer Mode",
                    subtitle = "Show experimental features",
                    checked = ComposeState.developerModeEnabled.value,
                    onCheckedChange = { 
                        ComposeState.developerModeEnabled.value = it 
                        mainActions?.setPreferenceBoolean("dev_mode", it)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Button(onClick = onCheckUpdate, modifier = Modifier.fillMaxWidth()) {
                    Text("Check for Updates")
                }
            }
        }
    }
}

@Composable
fun SettingToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
