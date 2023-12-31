package com.alpsfly.aeroglide.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.model.SensorData
import com.alpsfly.aeroglide.core.model.SensorType
import com.alpsfly.aeroglide.ui.module.AltitudeGraph
import com.alpsfly.aeroglide.ui.module.AnalogVario
import com.alpsfly.aeroglide.viewmodel.SensorViewModel

@Composable
fun Screen3(
    navController: NavController,
    sensorViewModel: SensorViewModel = hiltViewModel()
) {
    val altitude =
        sensorViewModel.altitude.collectAsState(initial = com.alpsfly.aeroglide.core.model.SensorData(type = com.alpsfly.aeroglide.core.model.SensorType.Altitude))
    val climbrate =
        sensorViewModel.climbrate.collectAsState(initial = com.alpsfly.aeroglide.core.model.SensorData(type = com.alpsfly.aeroglide.core.model.SensorType.Climbrate))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "this is ${altitude.value.values[0]}",
                modifier = Modifier
                    .clickable {
                        navController.navigate(route = Screen.Screen1.route)
                    }
            )

            AnalogVario(climbrate)
            AltitudeGraph(navController = navController, sensorViewModel)
        }
    }
}