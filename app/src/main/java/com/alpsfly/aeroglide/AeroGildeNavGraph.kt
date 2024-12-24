package com.alpsfly.aeroglide

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alpsfly.aeroglide.core.ui.Screen
import com.alpsfly.aeroglide.core.ui.presentation.DetailScreen
import com.alpsfly.aeroglide.core.ui.presentation.Screen1
import com.alpsfly.aeroglide.feature.devicestatus.presentation.DeviceStatusScreen
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
            route = Screen.Screen3.route
        ) {
            Screen1(navController = navController)
        }
        composable(
            route = Screen.DetailScreen.route
        ) {
            DetailScreen(navController = navController)
        }
    }
}