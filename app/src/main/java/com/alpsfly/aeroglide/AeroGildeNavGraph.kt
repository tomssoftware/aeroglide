package com.alpsfly.aeroglide

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alpsfly.aeroglide.core.Screen
import com.alpsfly.aeroglide.feature.activityhistory.activityList
import com.alpsfly.aeroglide.feature.activityhistory.activityMain
import com.alpsfly.aeroglide.feature.authentication.AuthenticationScreen
import com.alpsfly.aeroglide.feature.authentication.RegisterScreen
import com.alpsfly.aeroglide.feature.billing.PremiumScreen
import com.alpsfly.aeroglide.feature.dataexchange.navigation.dataExport
import com.alpsfly.aeroglide.feature.diagnosis.presentation.DiagnosisDetailScreen
import com.alpsfly.aeroglide.feature.diagnosis.presentation.DiagnosisScreen
import com.alpsfly.aeroglide.feature.livetracking.VariometerScreen
import com.alpsfly.aeroglide.feature.mapmanager.MapManagerScreen
import com.alpsfly.aeroglide.feature.settings.SettingsScreen

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