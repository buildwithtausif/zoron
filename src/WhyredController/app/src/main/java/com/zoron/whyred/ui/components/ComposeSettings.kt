package com.zoron.whyred.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoron.whyred.ui.theme.ZoronTheme

@Composable
fun SettingsScreenUI(
    onToggleAutoPilot: (Boolean) -> Unit,
    isAutoPilotEnabled: Boolean,
    onCheckUpdate: () -> Unit
) {
    ZoronTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                BentoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Autopilot Mode", style = MaterialTheme.typography.titleLarge)
                            Text("Automatically adapt power states", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isAutoPilotEnabled, onCheckedChange = onToggleAutoPilot)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                BentoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text("System", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onCheckUpdate, modifier = Modifier.fillMaxWidth()) {
                            Text("Check for Updates")
                        }
                    }
                }
            }
        }
    }
}
