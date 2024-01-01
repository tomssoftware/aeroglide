package com.alpsfly.aeroglide

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alpsfly.aeroglide.feature.analogvario.AnalogVarioScreen
import com.alpsfly.aeroglide.ui.screen.DetailScreen
import com.alpsfly.aeroglide.ui.screen.Screen
import com.alpsfly.aeroglide.ui.screen.Screen1
import com.alpsfly.aeroglide.ui.screen.Screen2

@Composable
fun AeroGlideNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Screen1.route
    ) {
        composable(
            route = Screen.Screen1.route
        ) {
            Screen1(navController = navController)
        }
        composable(
            route = Screen.Screen2.route
        ) {
            Screen2(navController = navController)
        }
        composable(
            route = Screen.Screen3.route
        ) {
            AnalogVarioScreen(navController = navController)
        }
        composable(
            route = Screen.DetailScreen.route
        ) {
            DetailScreen(navController = navController)
        }
    }
}