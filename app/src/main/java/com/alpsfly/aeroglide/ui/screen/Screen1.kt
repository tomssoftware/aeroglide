package com.alpsfly.aeroglide.ui.screen

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.model.SensorData
import com.alpsfly.aeroglide.viewmodel.SensorViewModel

@Composable
fun Screen1(
    navController: NavController,
    sensorViewModel: SensorViewModel = hiltViewModel()
) {
    val acceleration by sensorViewModel.acceleration.collectAsState(initial = com.alpsfly.aeroglide.core.model.SensorData())
    val verticalAccel by sensorViewModel.verticalAcceleration.collectAsState(initial = com.alpsfly.aeroglide.core.model.SensorData())
    val altitude by sensorViewModel.altitude.collectAsState(initial = com.alpsfly.aeroglide.core.model.SensorData())
    val climbrate by sensorViewModel.climbrate.collectAsState(initial = com.alpsfly.aeroglide.core.model.SensorData())
    val pressure by sensorViewModel.pressure.collectAsState(initial = com.alpsfly.aeroglide.core.model.SensorData())
    val location by sensorViewModel.location.collectAsState(initial = Location("none"))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            Text(text = "Accel:  x:${acceleration.values[0]}@${acceleration.frequency}Hz",
                modifier = Modifier.clickable {
                    navController.navigate(route = Screen.Screen2.route)
                }
            )
            Text(text = "Vert. accel:  x:${verticalAccel.values[0]}@${verticalAccel.frequency}Hz",
                modifier = Modifier.clickable {
                    navController.navigate(route = Screen.Screen2.route)
                }
            )
            Text(text = "Altitude: ${altitude.values[0]}@${altitude.frequency}Hz")
            Text(text = "Climbrate: ${climbrate.values[0]}@${climbrate.frequency}Hz")
            Text(text = "Pressure: ${pressure.values[0]}@${pressure.frequency}Hz")
            Text(text = "Location: ${location.altitude}")
        }
    }
}