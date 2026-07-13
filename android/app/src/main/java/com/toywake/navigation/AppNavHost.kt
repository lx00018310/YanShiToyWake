package com.toywake.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.toywake.ui.bind.BindScreen
import com.toywake.ui.play.PlayScreen
import com.toywake.ui.scan.ScanScreen
import com.toywake.ui.settings.SettingsScreen

object Routes {
    const val SCAN = "scan"
    const val BIND = "bind"
    const val PLAY = "play"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SCAN) {
        composable(Routes.SCAN) {
            ScanScreen(
                onNavigateBind = { tagUid -> navController.navigate("${Routes.BIND}/$tagUid") },
                onNavigatePlay = { toyId -> navController.navigate("${Routes.PLAY}/$toyId") },
                onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            route = "${Routes.BIND}/{tagUid}",
            arguments = listOf(navArgument("tagUid") { type = NavType.StringType }),
        ) {
            BindScreen(
                onBack = { navController.popBackStack() },
                onBound = { toyId ->
                    navController.navigate("${Routes.PLAY}/$toyId") {
                        popUpTo(Routes.SCAN)
                    }
                },
            )
        }
        composable(
            route = "${Routes.PLAY}/{toyId}",
            arguments = listOf(navArgument("toyId") { type = NavType.IntType }),
        ) {
            PlayScreen(
                onBack = { navController.popBackStack() },
                onNavigateScan = { navController.popBackStack(Routes.SCAN, inclusive = false) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
