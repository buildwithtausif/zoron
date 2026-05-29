package com.zoron.whyred.ui

import androidx.compose.ui.platform.ComposeView
import com.zoron.whyred.ui.theme.ZoronTheme

object ComposeInterop {

    @JvmStatic
    fun setMainNavigation(view: ComposeView, mainActions: MainActions) {
        view.setContent {
            ZoronTheme {
                ZoronAppNavigation(mainActions)
            }
        }
    }

    @JvmStatic
    fun setModeLearnContent(view: ComposeView) {
        view.setContent {
            ZoronTheme {
                com.zoron.whyred.ui.components.ModeLearnScreenUI()
            }
        }
    }

    @JvmStatic
    fun setSettingsContent(view: ComposeView) {
        view.setContent {
            ZoronTheme {
                // Dummy empty settings content since this activity is deprecated
            }
        }
    }
}
