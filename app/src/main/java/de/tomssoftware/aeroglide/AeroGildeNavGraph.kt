package de.tomssoftware.aeroglide

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.tomssoftware.aeroglide.core.Screen
import de.tomssoftware.aeroglide.feature.activityhistory.activityList
import de.tomssoftware.aeroglide.feature.activityhistory.activityMain
import de.tomssoftware.aeroglide.feature.authentication.AuthenticationScreen
import de.tomssoftware.aeroglide.feature.authentication.RegisterScreen
import de.tomssoftware.aeroglide.feature.billing.PremiumScreen
import de.tomssoftware.aeroglide.feature.dataexchange.navigation.dataExport
import de.tomssoftware.aeroglide.feature.diagnosis.presentation.DiagnosisDetailScreen
import de.tomssoftware.aeroglide.feature.diagnosis.presentation.DiagnosisScreen
import de.tomssoftware.aeroglide.feature.livetracking.VariometerScreen
import de.tomssoftware.aeroglide.feature.mapmanager.MapManagerScreen
import de.tomssoftware.aeroglide.feature.settings.SettingsScreen

@Composable
fun AeroGlideNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.VariometerScreen.route
    ) {
        composable(
            route = Screen.VariometerScreen.route
        ) {
            VariometerScreen(navController = navController)
        }
        composable(
            route = Screen.SettingsScreen.route
        ) {
            SettingsScreen(navController = navController)
        }
        composable(
            route = Screen.PremiumsScreen.route
        ) {
            PremiumScreen(navController = navController)
        }
        composable(
            route = Screen.MapManagerScreen.route
        ) {
            MapManagerScreen(navController = navController)
        }
        composable(
            route = Screen.DiagnosisScreen.route
        ) {
            DiagnosisScreen(navController = navController)
        }
        composable(
            route = Screen.DiagnosisDetailScreen.route
        ) {
            DiagnosisDetailScreen(navController = navController)
        }
        composable(
            route = Screen.AuthenticationScreen.route
        ) {
            AuthenticationScreen(navController = navController)
        }
        composable(
            route = Screen.RegisterScreen.route
        ) {
            RegisterScreen(navController = navController)
        }
        activityList(navController = navController)
        activityMain(navController = navController)
        dataExport(navController = navController)
    }
}