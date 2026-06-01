package com.zoron.whyred.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoron.whyred.ui.ComposeState
import com.zoron.whyred.ui.MainActions

@Composable
fun OTADialogUI(mainActions: MainActions?) {
    val showDialog by ComposeState.showOtaDialog
    val available by ComposeState.otaAvailable
    val version by ComposeState.otaVersion
    val changelog by ComposeState.otaChangelog
    val zipUrl by ComposeState.otaZipUrl
    val downloading by ComposeState.otaDownloading
    val downloadProgress by ComposeState.otaDownloadProgress
    val flashing by ComposeState.otaFlashing
    val flashSuccess by ComposeState.otaFlashSuccess
    val flashResult by ComposeState.otaFlashResult

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!downloading && !flashing) ComposeState.showOtaDialog.value = false 
            },
            title = {
                Text(
                    text = if (available) "Update Available: $version" else "Zoron Update Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (flashResult.isNotEmpty()) {
                        Text(
                            text = flashResult,
                            color = if (flashSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else if (flashing) {
                        Text("Flashing Magisk Module... Do not close the app or turn off your device.", modifier = Modifier.padding(bottom = 8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else if (downloading) {
                        Text("Downloading update... ${(downloadProgress * 100).toInt()}%", modifier = Modifier.padding(bottom = 8.dp))
                        LinearProgressIndicator(progress = downloadProgress, modifier = Modifier.fillMaxWidth())
                    } else {
                        Text("Changelog:", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
                        Text(text = parseMarkdownToAnnotatedString(changelog), style = MaterialTheme.typography.bodyMedium)
                        if (available) {
                            Text(
                                "\nThis will automatically download and flash the module, including the latest app update.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (flashSuccess) {
                    Button(onClick = { 
                        com.topjohnwu.superuser.Shell.cmd("reboot").exec() 
                    }) {
                        Text("Reboot Now")
                    }
                } else if (available && !downloading && !flashing) {
                    Button(onClick = { 
                        mainActions?.downloadAndFlashUpdate(zipUrl) 
                    }) {
                        Text("Download & Install")
                    }
                } else if (!downloading && !flashing) {
                    Button(onClick = { ComposeState.showOtaDialog.value = false }) {
                        Text("OK")
                    }
                }
            },
            dismissButton = {
                if (!downloading && !flashing && !flashSuccess) {
                    TextButton(onClick = { ComposeState.showOtaDialog.value = false }) {
                        Text(if (available) "Later" else "Close")
                    }
                }
            }
        )
    }
}
