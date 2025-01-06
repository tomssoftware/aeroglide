package com.alpsfly.aeroglide.feature.diagnosis.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.Screen
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.feature.diagnosis.viewmodel.DiagnosisViewModel

@Composable
fun DiagnosisScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    diagnosisViewModel: DiagnosisViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()

    val calibrationStatus = diagnosisViewModel.altitudeCalibrationStatus.collectAsState(Calibration())
    val altitudeFlowRaw = diagnosisViewModel.altitudeFlowRaw.collectAsState(initial = null)
    val climbrateFlowRaw = diagnosisViewModel.climbrateFlowRaw.collectAsState(initial = null)
    val altitudeFlowUi = diagnosisViewModel.altitudeFlowUi.collectAsState(initial = null)
    val climbrateFlowUi = diagnosisViewModel.climbrateFlowUi.collectAsState(initial = null)
    val pressureFlowUi = diagnosisViewModel.pressureFlowUi.collectAsState(initial = null)
    val verticalAccelerationFlowUi = diagnosisViewModel.verticalAccelerationFlowUi.collectAsState(initial = null)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        Text(text = "calibration: ${calibrationStatus.value.isCalibrated}, ${calibrationStatus.value.altitude0}, ${calibrationStatus.value.verticalAccuracy}",
            modifier = Modifier.clickable {
            }
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            Text(text = "", modifier = Modifier.clickable {})
            Text(text = "${altitudeFlowRaw.value}", modifier = Modifier.clickable {})
            Text(text = "${altitudeFlowUi.value}", modifier = Modifier.clickable {})
            Text(text = "${climbrateFlowRaw.value}", modifier = Modifier.clickable {})
            Text(text = "${climbrateFlowUi.value}", modifier = Modifier.clickable {})
            Text(
                text = "${pressureFlowUi.value}",
                modifier = Modifier.clickable {
                    navController.navigate(route = Screen.DiagnosisDetailScreen.route)
                }
            )
            Text(text = "${verticalAccelerationFlowUi.value}", modifier = Modifier.clickable {})
        }
    }
}

@Composable
fun DiagnosisDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    diagnosisViewModel: DiagnosisViewModel = hiltViewModel()
) {

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val sensorData = diagnosisViewModel.pressureFlowUi.collectAsState(initial = SensorData())

            Text(text = "${sensorData.value.type}", modifier = Modifier.clickable {})
            Text(text = "${sensorData.value.timestamp}", modifier = Modifier.clickable {})
            Text(text = "${sensorData.value.frequency}", modifier = Modifier.clickable {})
            Text(text = "${sensorData.value.values[0]}", modifier = Modifier.clickable {})
        }
    }
}


