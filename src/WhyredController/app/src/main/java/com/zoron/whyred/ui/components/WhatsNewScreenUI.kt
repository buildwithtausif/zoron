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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoron.whyred.ui.ComposeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewScreenUI(onNavigateBack: () -> Unit) {
    val changelog by ComposeState.otaChangelog
    
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
        
        if (trimmed.endsWith(":") || (trimmed.startsWith("v") && !trimmed.startsWith("-"))) {
            if (currentVersion.isNotEmpty()) {
                releases.add(ReleaseInfo(currentVersion, currentDate, currentNotes))
            }
            
            val versionClean = trimmed.removeSuffix(":")
            val match = Regex("""(v\d+\.\d+\.\d+)(?:\s*\((.*?)\))?""").find(versionClean)
            if (match != null) {
                currentVersion = match.groupValues[1]
                currentDate = if (match.groupValues.size > 2 && match.groupValues[2].isNotBlank()) match.groupValues[2] else "Date unavailable"
            } else {
                currentVersion = versionClean
                currentDate = "Date unavailable"
            }
            currentNotes = mutableListOf()
        } else if (trimmed.startsWith("-")) {
            currentNotes.add(trimmed.removePrefix("-").trim())
        } else {
            currentNotes.add(trimmed)
        }
    }
    if (currentVersion.isNotEmpty()) {
        releases.add(ReleaseInfo(currentVersion, currentDate, currentNotes))
    }
    
    return releases
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
                Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                    Text("•", modifier = Modifier.padding(end = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = note, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
