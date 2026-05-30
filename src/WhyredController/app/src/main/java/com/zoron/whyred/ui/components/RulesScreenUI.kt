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
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = ZoronDatabase.getDatabase(context)
            rulesList = db.ruleDao().getAllRules()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
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

    if (showAddDialog) {
        AddRuleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { rule ->
                showAddDialog = false
                coroutineScope.launch(Dispatchers.IO) {
                    val db = ZoronDatabase.getDatabase(context)
                    db.ruleDao().insertRule(rule)
                    rulesList = db.ruleDao().getAllRules()
                    withContext(Dispatchers.Main) {
                        showSnackbar("Rule Added Successfully")
                    }
                }
            }
        )
    }
}

@Composable
fun RuleCard(rule: RuleEntity, onToggle: (Boolean) -> Unit) {
    val displayCond = when(rule.conditionType) {
        "battery_level" -> "Battery Level (%)"
        "app_launched" -> "App Launched"
        else -> rule.conditionType
    }
    val displayAction = when(rule.actionType) {
        "set_profile" -> "Switch Profile"
        else -> rule.actionType
    }
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("IF ${displayCond.uppercase()} = ${rule.conditionValue}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("THEN ${displayAction.uppercase()} = ${rule.actionValue}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onSave: (RuleEntity) -> Unit
) {
    var conditionType by remember { mutableStateOf("battery_level") }
    var conditionValue by remember { mutableStateOf("20") }
    var actionType by remember { mutableStateOf("set_profile") }
    var actionValue by remember { mutableStateOf("battery") }

    val conditionDisplay = when(conditionType) {
        "battery_level" -> "Battery Level (%)"
        "app_launched" -> "App Launched"
        else -> conditionType
    }

    val actionDisplay = when(actionType) {
        "set_profile" -> "Switch Profile"
        else -> actionType
    }

    val conditionInfo = when(conditionType) {
        "battery_level" -> "What: Triggers when your battery drops below the set percentage.\nWhy: Automate battery savings.\nExample: 20"
        "app_launched" -> "What: Triggers when a specific app is opened.\nWhy: Boost performance for games.\nExample: com.tencent.ig"
        else -> ""
    }

    val actionInfo = when(actionType) {
        "set_profile" -> "What: Changes the system performance mode.\nWhy: Adapts to your usage.\nExample: battery, performance, balanced"
        else -> ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Automation Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("WHEN (Condition)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        var conditionExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = conditionExpanded,
                            onExpandedChange = { conditionExpanded = !conditionExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = conditionDisplay,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = conditionExpanded, onDismissRequest = { conditionExpanded = false }) {
                                DropdownMenuItem(text = { Text("Battery Level (%)") }, onClick = { conditionType = "battery_level"; conditionExpanded = false })
                                DropdownMenuItem(text = { Text("App Launched") }, onClick = { conditionType = "app_launched"; conditionExpanded = false })
                            }
                        }
                        OutlinedTextField(
                            value = conditionValue,
                            onValueChange = { conditionValue = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Value") }
                        )
                    }
                    Text(conditionInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("THEN (Action)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        var actionExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = actionExpanded,
                            onExpandedChange = { actionExpanded = !actionExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = actionDisplay,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                                DropdownMenuItem(text = { Text("Switch Profile") }, onClick = { actionType = "set_profile"; actionExpanded = false })
                            }
                        }
                        OutlinedTextField(
                            value = actionValue,
                            onValueChange = { actionValue = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Value") }
                        )
                    }
                    Text(actionInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val rule = RuleEntity()
                rule.ruleId = java.util.UUID.randomUUID().toString()
                rule.conditionType = conditionType
                rule.conditionValue = conditionValue
                rule.actionType = actionType
                rule.actionValue = actionValue
                rule.isEnabled = true
                
                onSave(rule)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
