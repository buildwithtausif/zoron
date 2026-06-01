package com.zoron.whyred.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoron.whyred.ui.ComposeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewScreenUI(onNavigateBack: () -> Unit) {
    val otaChangelog by ComposeState.otaChangelog
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val changelog = remember(otaChangelog) {
        if (otaChangelog.isNotEmpty()) {
            otaChangelog
        } else {
            try {
                context.assets.open("changelog.md").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                ""
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("What's New") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Release History",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            if (changelog.isNotEmpty()) {
                val releases = parseChangelog(changelog)
                if (releases.isEmpty()) {
                    EmptyState()
                } else {
                    releases.forEach { release ->
                        ReleaseCard(release)
                    }
                }
            } else {
                EmptyState(message = "Loading release history... Please ensure you have an active internet connection to fetch the OTA metadata.")
            }
        }
    }
}

@Composable
fun EmptyState(message: String = "No release history available.") {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

data class ReleaseInfo(val version: String, val date: String, val notes: List<String>)

fun parseChangelog(changelog: String): List<ReleaseInfo> {
    val releases = mutableListOf<ReleaseInfo>()
    val lines = changelog.split("\n")
    var currentVersion = ""
    var currentDate = ""
    var currentNotes = mutableListOf<String>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        
        // Match markdown headers: "## v4.5.15", "# v4.5.1", "## v4.5.12 (2026-05-30):", etc.
        val headerContent = when {
            trimmed.startsWith("## ") -> trimmed.removePrefix("## ").trim()
            trimmed.startsWith("# ") && !trimmed.startsWith("# Zoron") -> trimmed.removePrefix("# ").trim()
            trimmed.startsWith("v") && !trimmed.startsWith("-") -> trimmed
            else -> null
        }
        
        if (headerContent != null) {
            if (currentVersion.isNotEmpty()) {
                releases.add(ReleaseInfo(currentVersion, currentDate, currentNotes))
            }
            
            val versionClean = headerContent.removeSuffix(":")
            val match = Regex("""(v[\d]+\.[\d]+\.[\d]+(?:-\w+)?)(?:\s*[—\-]\s*(.+?))?(?:\s*\((.+?)\))?""").find(versionClean)
            if (match != null) {
                currentVersion = match.groupValues[1]
                val subtitle = match.groupValues[2].trim()
                val dateInParens = match.groupValues[3].trim()
                currentDate = when {
                    dateInParens.isNotBlank() -> dateInParens
                    subtitle.isNotBlank() -> subtitle
                    else -> ""
                }
            } else {
                currentVersion = versionClean
                currentDate = ""
            }
            currentNotes = mutableListOf()
        } else if (trimmed.startsWith("-")) {
            currentNotes.add(trimmed.removePrefix("-").trim())
        } else if (!trimmed.startsWith("# Zoron")) {
            currentNotes.add(trimmed)
        }
    }
    if (currentVersion.isNotEmpty()) {
        releases.add(ReleaseInfo(currentVersion, currentDate, currentNotes))
    }
    
    return releases
}

fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            var processLine = line.trim()
            var isHeading = false
            var headingLevel = 0
            
            val headingMatch = Regex("""^(#+)\s+(.*)""").find(processLine)
            if (headingMatch != null) {
                isHeading = true
                headingLevel = headingMatch.groupValues[1].length
                processLine = headingMatch.groupValues[2]
            }

            val style = if (isHeading) {
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = when (headingLevel) {
                        1 -> 20.sp
                        2 -> 18.sp
                        3 -> 16.sp
                        else -> 14.sp
                    }
                )
            } else {
                SpanStyle()
            }

            withStyle(style) {
                val parts = processLine.split("**")
                for (i in parts.indices) {
                    if (i % 2 == 1) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(parts[i])
                        }
                    } else {
                        append(parts[i])
                    }
                }
            }
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

@Composable
fun ReleaseCard(release: ReleaseInfo) {
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = release.version,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = release.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            release.notes.forEach { note ->
                val isHeading = note.trim().startsWith("#")
                Row(
                    modifier = Modifier.padding(
                        bottom = if (isHeading) 4.dp else 8.dp, 
                        top = if (isHeading) 8.dp else 0.dp
                    ), 
                    verticalAlignment = Alignment.Top
                ) {
                    if (!isHeading) {
                        Text("•", modifier = Modifier.padding(end = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(text = parseMarkdownToAnnotatedString(note), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
