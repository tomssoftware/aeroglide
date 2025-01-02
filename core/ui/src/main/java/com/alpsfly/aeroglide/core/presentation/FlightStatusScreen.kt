package com.alpsfly.aeroglide.core.presentation

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.viewmodel.FlightStatusViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlightStatusScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    flightStatusViewModel: FlightStatusViewModel = hiltViewModel()
) {
    val altitude by flightStatusViewModel.altitudeFlow.collectAsStateWithLifecycle(initialValue = SensorData())
    val climbrate by flightStatusViewModel.climbrateFlow.collectAsStateWithLifecycle(initialValue = SensorData())
    val pressure by flightStatusViewModel.pressureFlow.collectAsStateWithLifecycle(initialValue = SensorData())
    val location by flightStatusViewModel.locationFlow.collectAsStateWithLifecycle(initialValue = Location("none"))
    val calibrationUiState by flightStatusViewModel.calibrationUiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataCell(
                caption = "Altitude",
                value = LocalUnit
                    .of(altitude.values[0], UnitConverter.Unit.M)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = "m",
                modifier = Modifier.weight(1f)
            )
            DataCell(
                caption = "Climbrate",
                value = LocalUnit
                    .of(climbrate.values[0], UnitConverter.Unit.MS)
                    .withDigits(2)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(climbrate.values[0], UnitConverter.Unit.MS).toLocalSymbol(),
                modifier = Modifier.weight(1f)
            )
            DataCell(
                caption = "Pressure",
                value = LocalUnit
                    .of(pressure.values[0], UnitConverter.Unit.M) // todo: hpa
                    .withSymbol(false)
                    .toLocalString(),
                unit = "hpa",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataCell(
                caption = "Speed",
                value = LocalUnit.of(location.speed, UnitConverter.Unit.KMH)
                    .withSymbol(flag = false).toLocalString(),
                unit = "km/h",
                modifier = Modifier.weight(1f)
            )
            when (calibrationUiState) {
                is CalibrationUiState.Success -> {
                    DataCell(
                        caption = "Alt. Acc.",
                        value = LocalUnit.of((calibrationUiState as CalibrationUiState.Success).calibration.verticalAccuracy, UnitConverter.Unit.M)
                            .withSymbol(false).toLocalString(),
                        unit = "ts",
                        modifier = Modifier.weight(1f)
                    )
                }
                is CalibrationUiState.Loading -> {
                    DataCell(
                        caption = "Alt. Acc.",
                        value = "-",
                        unit = "-",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            DataCell(
                caption = "GPS Alt.",
                value = LocalUnit.of(location.altitude.toFloat(), UnitConverter.Unit.M)
                    .withSymbol(false).toLocalString(),
                unit = "m",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DataCell(
    caption: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = caption,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = unit,
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.Gray
                    ),
                )
            }
        }
    }
}

