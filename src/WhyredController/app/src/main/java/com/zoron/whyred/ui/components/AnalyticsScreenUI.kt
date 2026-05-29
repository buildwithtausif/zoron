package com.zoron.whyred.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zoron.whyred.ui.ComposeState
import com.zoron.whyred.ui.MainActions

@Composable
fun AnalyticsScreenUI(mainActions: MainActions, showSnackbar: (String) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Battery Trends", "Process Monitor", "Diagnostics")
    val icons = listOf(Icons.Filled.Timeline, Icons.Filled.List, Icons.Filled.Warning)

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = { Icon(icons[index], contentDescription = null) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedTab) {
                0 -> BatteryChartTab(mainActions)
                1 -> ProcessReportTab(mainActions)
                2 -> DiagnosticsTab(mainActions, showSnackbar)
            }
        }
    }
}

@Composable
fun BatteryChartTab(mainActions: MainActions) {
    val csvData by ComposeState.batteryCsvData
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(csvData) {
        if (csvData.isNotEmpty()) {
            val lines = csvData.trim().split("\n")
            val entries = mutableListOf<com.patrykandpatrick.vico.core.entry.FloatEntry>()
            var firstTs = -1L
            for (line in lines) {
                val parts = line.split(",")
                if (parts.size >= 2) {
                    try {
                        val ts = parts[0].toLong()
                        val level = parts[1].toFloat()
                        if (firstTs == -1L) firstTs = ts
                        val x = (ts - firstTs) / 60f // minutes
                        entries.add(entryOf(x, level))
                    } catch (e: Exception) {}
                }
            }
            if (entries.isNotEmpty()) {
                chartEntryModelProducer.setEntries(entries)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("BATTERY DISCHARGE TREND", style = MaterialTheme.typography.labelSmall)
            Button(onClick = { 
                mainActions.exportCsv()
            }) {
                Text("Export CSV")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BentoCard(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                if (csvData.isEmpty()) {
                    Text("No battery data available. Wait for a few minutes.", color = Color.Gray)
                } else {
                    val marker = com.patrykandpatrick.vico.compose.component.shapeComponent(
                        shape = com.patrykandpatrick.vico.core.component.shape.Shapes.pillShape,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Chart(
                        chart = lineChart(
                            lines = listOf(
                                com.patrykandpatrick.vico.compose.chart.line.lineSpec(
                                    lineColor = MaterialTheme.colorScheme.primary,
                                    lineBackgroundShader = com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient(
                                        arrayOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), Color.Transparent)
                                    )
                                )
                            )
                        ),
                        chartModelProducer = chartEntryModelProducer,
                        startAxis = rememberStartAxis(
                            label = com.patrykandpatrick.vico.compose.component.textComponent(color = MaterialTheme.colorScheme.onSurface),
                            axis = null,
                            tick = null,
                            guideline = com.patrykandpatrick.vico.compose.component.lineComponent(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                        ),
                        bottomAxis = rememberBottomAxis(
                            label = com.patrykandpatrick.vico.compose.component.textComponent(color = MaterialTheme.colorScheme.onSurface),
                            axis = null,
                            tick = null,
                            guideline = null
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessReportTab(mainActions: MainActions) {
    val processReport by ComposeState.currentProcessReport
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("TOP RUNNING PROCESSES", style = MaterialTheme.typography.labelSmall)
            Button(onClick = { 
                mainActions.exportProcessReport()
            }) {
                Text("Export")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BentoCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Text(
                    text = processReport,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
fun DiagnosticsTab(mainActions: MainActions, showSnackbar: (String) -> Unit) {
    val logs by ComposeState.currentLogs
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ENGINE LOGS", style = MaterialTheme.typography.labelSmall)
            Button(onClick = { 
                mainActions.exportLogs(ComposeState.zipExportEnabled.value)
                showSnackbar("Exporting logs...")
            }) {
                Text("Export")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BentoCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Text(
                    text = logs,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())
                )
            }
        }
    }
}
