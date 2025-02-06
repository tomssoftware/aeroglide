package com.alpsfly.aeroglide

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alpsfly.aeroglide.core.Screen
import com.alpsfly.aeroglide.feature.activityhistory.activityDetail
import com.alpsfly.aeroglide.feature.activityhistory.activityList
import com.alpsfly.aeroglide.feature.devicestatus.presentation.DeviceStatusScreen
import com.alpsfly.aeroglide.feature.diagnosis.presentation.DiagnosisDetailScreen
import com.alpsfly.aeroglide.feature.diagnosis.presentation.DiagnosisScreen
import com.alpsfly.aeroglide.feature.variometer.presentation.VariometerScreen


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
            route = Screen.DeviceStatusScreen.route
        ) {
            DeviceStatusScreen(navController = navController)
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
        activityDetail(navController = navController)
    }
}