package com.alpsfly.aeroglide.core.ui.page

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
import com.alpsfly.aeroglide.core.data.util.SensorData
import com.alpsfly.aeroglide.core.viewmodel.SensorViewModel

@Composable
fun Page2(sensorViewModel: SensorViewModel = hiltViewModel()) {
    val altitude by sensorViewModel.altitude.collectAsState(initial = SensorData())
    val climbrate by sensorViewModel.climbrate.collectAsState(initial = SensorData())
    val pressure by sensorViewModel.pressure.collectAsState(initial = SensorData())

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
            Text(text = "climbrate: ${climbrate.values[0]}@${climbrate.frequency}",
                modifier = Modifier.clickable {
                }
            )

            Text(text = "pressure: ${pressure.values[0]}@${pressure.frequency}",
                modifier = Modifier.clickable {
                }
            )

            Text(text = "altitude ${altitude.values[0]}@${altitude.frequency}",
                modifier = Modifier.clickable {
                }
            )

        }
    }
}

