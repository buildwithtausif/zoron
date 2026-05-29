package com.zoron.whyred.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zoron.whyred.ui.components.ComposeHome
import com.zoron.whyred.ui.components.SettingsScreenUI
import com.zoron.whyred.ui.components.ModeLearnScreenUI

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import kotlinx.coroutines.launch
import com.zoron.whyred.ui.components.AnalyticsScreenUI
import com.zoron.whyred.ui.components.RulesScreenUI

@Composable
fun ZoronAppNavigation(
    mainActions: MainActions
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    selected = currentRoute == "analytics",
                    onClick = {
                        navController.navigate("analytics") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Gavel, contentDescription = "Rules") },
                    label = { Text("Rules") },
                    selected = currentRoute == "rules",
                    onClick = {
                        navController.navigate("rules") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.MenuBook, contentDescription = "Learn") },
                    label = { Text("Learn") },
                    selected = currentRoute == "learn",
                    onClick = {
                        navController.navigate("learn") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { ComposeHome(mainActions, showSnackbar) }
            composable("analytics") { AnalyticsScreenUI(mainActions, showSnackbar) }
            composable("rules") { RulesScreenUI(showSnackbar) }
            composable("learn") { ModeLearnScreenUI() }
            composable("settings") { 
                SettingsScreenUI(
                    onToggleAutoPilot = { 
                        mainActions.toggleAutopilot(it)
                        showSnackbar("Autopilot ${if(it) "Enabled" else "Disabled"}")
                    },
                    isAutoPilotEnabled = ComposeState.autopilotEnabled.value,
                    onCheckUpdate = { mainActions.checkUpdates() },
                    mainActions = mainActions,
                    showSnackbar = showSnackbar
                ) 
            }
        }
    }
}
