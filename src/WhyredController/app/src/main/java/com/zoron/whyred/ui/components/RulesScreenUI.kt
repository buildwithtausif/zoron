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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Condition", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var conditionExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = conditionExpanded,
                        onExpandedChange = { conditionExpanded = !conditionExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = conditionType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = conditionExpanded, onDismissRequest = { conditionExpanded = false }) {
                            DropdownMenuItem(text = { Text("battery_level") }, onClick = { conditionType = "battery_level"; conditionExpanded = false })
                            DropdownMenuItem(text = { Text("app_launched") }, onClick = { conditionType = "app_launched"; conditionExpanded = false })
                        }
                    }
                    OutlinedTextField(
                        value = conditionValue,
                        onValueChange = { conditionValue = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Value") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Action", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var actionExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = actionExpanded,
                        onExpandedChange = { actionExpanded = !actionExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = actionType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                            DropdownMenuItem(text = { Text("set_profile") }, onClick = { actionType = "set_profile"; actionExpanded = false })
                        }
                    }
                    OutlinedTextField(
                        value = actionValue,
                        onValueChange = { actionValue = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Value") }
                    )
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
