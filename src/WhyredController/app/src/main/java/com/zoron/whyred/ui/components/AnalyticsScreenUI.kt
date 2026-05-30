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
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zoron.whyred.ui.ComposeState
import com.zoron.whyred.ui.MainActions
import kotlin.math.abs

data class TrendInsights(
    val summary: String,
    val direction: String, // Improving, Stable, Declining
    val contributors: List<String>,
    val recommendations: List<String>,
    val confidence: String
)

fun generateTrendRead(csvData: String, processReport: String): TrendInsights {
    val lines = csvData.trim().split("\n")
    if (lines.size < 2 || csvData.isEmpty()) {
        return TrendInsights(
            summary = "Insufficient data available for analysis.",
            direction = "N/A",
            contributors = listOf("Not enough data to determine contributors."),
            recommendations = listOf("Please wait for more data to be collected."),
            confidence = "Low"
        )
    }

    var slope = 0f
    var conf = "Low"
    
    try {
        val first = lines.first().split(",")
        val last = lines.last().split(",")
        if (first.size >= 2 && last.size >= 2) {
            val t1 = first[0].toLong()
            val l1 = first[1].toFloat()
            val t2 = last[0].toLong()
            val l2 = last[1].toFloat()
            
            val dt = (t2 - t1) / 60f // minutes
            if (dt > 0) {
                slope = (l1 - l2) / dt // drop per minute
                conf = if (lines.size > 10 && dt > 5) "High" else "Medium"
            }
        }
    } catch (e: Exception) {}

    val direction = when {
        slope > 0.5f -> "Declining"
        slope < 0.1f -> "Improving"
        else -> "Stable"
    }

    val summary = when (direction) {
        "Declining" -> "Battery is discharging rapidly. High background activity detected."
        "Improving" -> "Battery drain is minimal. Device is in an optimal state."
        else -> "Battery drain is at a normal, stable rate."
    }

    val topContributors = mutableListOf<String>()
    if (processReport.isNotBlank()) {
        val pLines = processReport.split("\n")
        var count = 0
        for (p in pLines) {
            if (p.contains(Regex("\\d+%"))) { // very basic heuristic for CPU/Mem line
                val clean = p.trim().replace(Regex("\\s+"), " ")
                topContributors.add(clean)
                count++
                if (count >= 3) break
            }
        }
    }
    if (topContributors.isEmpty()) topContributors.add("No significant background drains detected.")

    val recommendations = mutableListOf<String>()
    if (direction == "Declining") {
        recommendations.add("Switch to PowerSave or Battery profile.")
        recommendations.add("Clear background apps.")
    } else {
        recommendations.add("Continue normal usage.")
    }

    return TrendInsights(summary, direction, topContributors, recommendations, conf)
}

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
    val processReport by ComposeState.currentProcessReport
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    var minY by remember { mutableStateOf(0f) }
    var maxY by remember { mutableStateOf(100f) }
    var validEntriesCount by remember { mutableStateOf(0) }

    LaunchedEffect(csvData) {
        if (csvData.isNotEmpty()) {
            val lines = csvData.trim().split("\n")
            val entries = mutableListOf<com.patrykandpatrick.vico.core.entry.FloatEntry>()
            var firstTs = -1L
            var minL = 100f
            var maxL = 0f
            for (line in lines) {
                val parts = line.split(",")
                if (parts.size >= 2) {
                    try {
                        val ts = parts[0].toLong()
                        val level = parts[1].toFloat()
                        if (firstTs == -1L) firstTs = ts
                        val x = (ts - firstTs) / 60f // minutes
                        if (entries.isEmpty() || x > entries.last().x) {
                            entries.add(entryOf(x, level))
                            if (level < minL) minL = level
                            if (level > maxL) maxL = level
                        }
                    } catch (e: Exception) {}
                }
            }
            validEntriesCount = entries.size
            if (entries.size >= 2) {
                chartEntryModelProducer.setEntries(entries)
                minY = (minL - 2f).coerceAtLeast(0f)
                maxY = (maxL + 2f).coerceAtMost(100f)
            }
        } else {
            validEntriesCount = 0
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("BATTERY DISCHARGE TREND", style = MaterialTheme.typography.labelSmall)
            OutlinedButton(onClick = { mainActions.exportCsv() }) {
                Text("Export")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BentoCard(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                if (validEntriesCount < 2) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(if (csvData.isEmpty()) "Loading battery data..." else "Insufficient data available for analysis. Waiting for more data...", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    Chart(
                        chart = lineChart(
                            axisValuesOverrider = com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.fixed(minY = minY, maxY = maxY),
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
                            label = com.patrykandpatrick.vico.compose.component.textComponent(
                                color = MaterialTheme.colorScheme.onSurface,
                                textSize = 10.sp
                            ),
                            axis = com.patrykandpatrick.vico.compose.component.lineComponent(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 1.dp),
                            tick = com.patrykandpatrick.vico.compose.component.lineComponent(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 1.dp),
                            guideline = com.patrykandpatrick.vico.compose.component.lineComponent(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), 
                                thickness = 1.dp
                            ),
                            valueFormatter = { value, _ -> "${value.toInt()}%" }
                        ),
                        bottomAxis = rememberBottomAxis(
                            label = com.patrykandpatrick.vico.compose.component.textComponent(
                                color = MaterialTheme.colorScheme.onSurface,
                                textSize = 10.sp
                            ),
                            axis = com.patrykandpatrick.vico.compose.component.lineComponent(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 1.dp),
                            tick = com.patrykandpatrick.vico.compose.component.lineComponent(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 1.dp),
                            guideline = null,
                            valueFormatter = { value, _ -> "${value.toInt()}m" }
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        val insights = remember(csvData, processReport) { generateTrendRead(csvData, processReport) }
        Text("TREND INSIGHTS", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(8.dp))
        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TrendSection("Summary", insights.summary)
                TrendSection("Direction", insights.direction, color = when(insights.direction) {
                    "Improving" -> Color(0xFF4CAF50)
                    "Declining" -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurface
                })
                TrendSection("Confidence", insights.confidence)
                
                Text("Key Contributors", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Column {
                    insights.contributors.forEach { c ->
                        Text("• $c", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Text("Recommendations", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Column {
                    insights.recommendations.forEach { r ->
                        Text("• $r", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun TrendSection(title: String, content: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(content, style = MaterialTheme.typography.bodyMedium, color = color)
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
