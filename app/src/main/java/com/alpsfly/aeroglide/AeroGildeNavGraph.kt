package com.alpsfly.aeroglide

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alpsfly.aeroglide.core.ui.presentation.DetailScreen
import com.alpsfly.aeroglide.core.ui.presentation.FlightStatusScreen
import com.alpsfly.aeroglide.core.ui.Screen
import com.alpsfly.aeroglide.feature.devicestatus.presentation.DeviceStatusScreen
import com.alpsfly.aeroglide.feature.variometer.presentation.VariometerScreen

@Composable
fun AeroGlideNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Screen3.route
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
            route = Screen.Screen3.route
        ) {
            FlightStatusScreen(navController = navController)
        }
        composable(
            route = Screen.DetailScreen.route
        ) {
            DetailScreen(navController = navController)
        }
    }
}