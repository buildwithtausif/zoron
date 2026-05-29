package com.zoron.whyred.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoron.whyred.data.RuleEntity
import com.zoron.whyred.data.ZoronDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RulesScreenUI(showSnackbar: (String) -> Unit) {
    val context = LocalContext.current
    var rulesList by remember { mutableStateOf<List<RuleEntity>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = ZoronDatabase.getDatabase(context)
            rulesList = db.ruleDao().getAllRules()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSnackbar("Rule editing requires the legacy Activity for now.") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Rule")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text("AUTOMATION RULES", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (rulesList.isEmpty()) {
                BentoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No active rules", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Create a rule to automate Zoron profiles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(rulesList) { rule ->
                        RuleCard(rule, onToggle = { enabled ->
                            // Optimistic update
                            val updatedRule = rule.apply { isEnabled = enabled }
                            rulesList = rulesList.map { if (it.ruleId == rule.ruleId) updatedRule else it }
                            
                            // Save to DB
                            kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                                ZoronDatabase.getDatabase(context).ruleDao().updateRule(updatedRule)
                            }
                            showSnackbar("Rule ${if (enabled) "Enabled" else "Disabled"}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun RuleCard(rule: RuleEntity, onToggle: (Boolean) -> Unit) {
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("IF ${rule.conditionType.uppercase()} = ${rule.conditionValue}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("THEN ${rule.actionType.uppercase()} = ${rule.actionValue}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}
