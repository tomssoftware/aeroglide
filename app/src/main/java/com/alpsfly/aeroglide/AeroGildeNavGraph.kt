package com.alpsfly.aeroglide

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alpsfly.aeroglide.core.Screen
import com.alpsfly.aeroglide.feature.activityhistory.activityList
import com.alpsfly.aeroglide.feature.activityhistory.activityMain
import com.alpsfly.aeroglide.feature.billing.PremiumScreen
import com.alpsfly.aeroglide.feature.dataexchange.navigation.dataExport
import com.alpsfly.aeroglide.feature.diagnosis.presentation.DiagnosisDetailScreen
import com.alpsfly.aeroglide.feature.diagnosis.presentation.DiagnosisScreen
import com.alpsfly.aeroglide.feature.livetracking.VariometerScreen
import com.alpsfly.aeroglide.feature.settings.presentation.SettingsScreen

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
            route = Screen.DiagnosisScreen.route
        ) {
            DiagnosisScreen(navController = navController)
        }
        composable(
            route = Screen.DiagnosisDetailScreen.route
        ) {
            DiagnosisDetailScreen(navController = navController)
        }
        activityList(navController = navController)
        activityMain(navController = navController)
        dataExport(navController = navController)
    }
}