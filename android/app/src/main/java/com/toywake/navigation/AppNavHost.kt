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
import java.net.URLEncoder

object Routes {
    const val SCAN = "scan"
    const val BIND = "bind"
    const val PLAY = "play"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    fun navigatePlay(toyId: Int, toyName: String, toyType: String, popToScan: Boolean) {
        val name = URLEncoder.encode(toyName, "UTF-8")
        val type = URLEncoder.encode(toyType, "UTF-8")
        navController.navigate("${Routes.PLAY}/$toyId?toyName=$name&toyType=$type") {
            if (popToScan) popUpTo(Routes.SCAN)
        }
    }

    NavHost(navController = navController, startDestination = Routes.SCAN) {
        composable(Routes.SCAN) {
            ScanScreen(
                onNavigateBind = { tagUid -> navController.navigate("${Routes.BIND}/$tagUid") },
                onNavigatePlay = { id, name, type -> navigatePlay(id, name, type, popToScan = false) },
                onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            route = "${Routes.BIND}/{tagUid}",
            arguments = listOf(navArgument("tagUid") { type = NavType.StringType }),
        ) {
            BindScreen(
                onBack = { navController.popBackStack() },
                onBound = { id, name, type -> navigatePlay(id, name, type, popToScan = true) },
            )
        }
        composable(
            route = "${Routes.PLAY}/{toyId}?toyName={toyName}&toyType={toyType}",
            arguments = listOf(
                navArgument("toyId") { type = NavType.IntType },
                navArgument("toyName") { type = NavType.StringType; defaultValue = ""; nullable = true },
                navArgument("toyType") { type = NavType.StringType; defaultValue = ""; nullable = true },
            ),
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
