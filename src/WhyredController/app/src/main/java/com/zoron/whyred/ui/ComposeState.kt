package com.zoron.whyred.ui

import androidx.compose.runtime.mutableStateOf

object ComposeState {
    val powerState = mutableStateOf("DETECTING...")
    val profile = mutableStateOf("Unknown")
    val cpuGovernor = mutableStateOf("Unknown")
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
}

interface MainActions {
    fun applyMode(mode: String)
    fun checkUpdates()
    fun toggleAutopilot(enabled: Boolean)
    fun setPreferenceBoolean(key: String, value: Boolean)
    fun setPreferenceString(key: String, value: String)
    fun exportLogs(zipIt: Boolean)
    fun clearLogs()
}
