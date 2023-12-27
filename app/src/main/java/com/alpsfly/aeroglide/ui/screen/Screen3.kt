package com.alpsfly.aeroglide.ui.screen

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.data.util.SensorData
import com.alpsfly.aeroglide.data.util.SensorType
import com.alpsfly.aeroglide.viewmodel.SensorViewModel

@Composable
fun Screen3(
    navController: NavController,
    sensorViewModel: SensorViewModel = hiltViewModel()
) {
    val altitude = SensorData() //by sensorViewModel.getAltitude().collectAsState(initial = SensorData(type = SensorType.Altitude))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "this is $altitude",
                modifier = Modifier
                    .clickable {
                        navController.navigate(route = Screen.Screen1.route)
                    }
            )
        }
    }
}