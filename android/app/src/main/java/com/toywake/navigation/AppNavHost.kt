package com.toywake.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.toywake.ui.settings.SettingsScreen

object Routes {
    const val SETTINGS = "settings"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SETTINGS) {
        composable(Routes.SETTINGS) { SettingsScreen() }
    }
}
