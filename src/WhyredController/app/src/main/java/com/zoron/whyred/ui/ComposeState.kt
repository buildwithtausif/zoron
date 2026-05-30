package com.zoron.whyred.ui

import androidx.compose.runtime.mutableStateOf

object ComposeState {
    val powerState = mutableStateOf("DETECTING...")
    val profile = mutableStateOf("Unknown")
    val cpuGovernor = mutableStateOf("Unknown")
    val selectedGpuGovernor = mutableStateOf("")
    val availableCpuGovernors = mutableStateOf(listOf<String>())
    val availableGpuGovernors = mutableStateOf(listOf<String>())
    val batteryHealth = mutableStateOf("98%")
    val rulesActive = mutableStateOf("3 Active")
    val optimizationScore = mutableStateOf(85)
    
    val autopilotEnabled = mutableStateOf(false)
    val fastpathEnabled = mutableStateOf(true)
    val adaptiveLearningEnabled = mutableStateOf(true)
    val developerModeEnabled = mutableStateOf(false)
    val zipExportEnabled = mutableStateOf(true)
    val logLevel = mutableStateOf("INFO")
    
    val isTransitioning = mutableStateOf(false)
    val transitionProgress = mutableStateOf(0f)
    val transitionLog = mutableStateOf("Initializing...")
    
    val currentLogs = mutableStateOf("No logs yet.")
    val currentProcessReport = mutableStateOf("No process data yet.")
    val batteryCsvData = mutableStateOf("")

    // OTA State
    val otaAvailable = mutableStateOf(false)
    val otaVersion = mutableStateOf("")
    val otaChangelog = mutableStateOf("")
    val otaZipUrl = mutableStateOf("")
    val otaDownloading = mutableStateOf(false)
    val otaDownloadProgress = mutableStateOf(0f)
    val otaFlashing = mutableStateOf(false)
    val otaFlashResult = mutableStateOf("")
    val otaFlashSuccess = mutableStateOf(false)
    val showOtaDialog = mutableStateOf(false)
}

interface MainActions {
    fun applyMode(mode: String)
    fun checkUpdates()
    fun downloadAndFlashUpdate(zipUrl: String)
    fun toggleAutopilot(enabled: Boolean)
    fun setPreferenceBoolean(key: String, value: Boolean)
    fun setPreferenceString(key: String, value: String)
    fun exportLogs(zipIt: Boolean)
    fun exportCsv()
    fun exportProcessReport()
    fun clearLogs()
    fun applyCpuGovernor(governor: String)
    fun applyGpuGovernor(governor: String)
}
