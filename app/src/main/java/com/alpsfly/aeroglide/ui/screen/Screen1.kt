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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.data.util.SensorValues
import com.alpsfly.aeroglide.viewmodel.SensorViewModel


@Composable
fun Screen1(
    navController: NavController,
    sensorViewModel: SensorViewModel = hiltViewModel()
) {
    val accelData by sensorViewModel.getAccelSenorData().collectAsState(initial = SensorValues(0L, floatArrayOf(0f, 0f, 0f)))
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
            Text(text = "this is x: ${accelData.x} y: ${accelData.y} z: ${accelData.z}}",
                modifier = Modifier.clickable {
                    navController.navigate(route = Screen.Screen2.route)
                }
            )
        }
    }
}