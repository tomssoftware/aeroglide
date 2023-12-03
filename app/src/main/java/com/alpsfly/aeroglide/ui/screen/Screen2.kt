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
import com.alpsfly.aeroglide.viewmodel.SensorViewModel

@Composable
fun Screen2(
    navController: NavController,
    sensorViewModel: SensorViewModel = hiltViewModel()
) {
    val locationData by sensorViewModel.getLocation().collectAsState(initial = Location(""))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Green)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            Text(text = "this is ${locationData.longitude},${locationData.latitude}@${locationData.time}",
                modifier = Modifier.clickable {
                    navController.navigate(route = Screen.Screen3.route)
                }
            )
        }
    }
}