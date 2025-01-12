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
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Pressure
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.viewmodel.FlightStatusViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlightStatusScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    flightStatusViewModel: FlightStatusViewModel = hiltViewModel()
) {
    val altitude by flightStatusViewModel.altitudeFlow.collectAsStateWithLifecycle(initialValue = Altitude())
    val climbrate by flightStatusViewModel.climbrateFlow.collectAsStateWithLifecycle(initialValue = Climbrate())
    val pressure by flightStatusViewModel.pressureFlow.collectAsStateWithLifecycle(initialValue = Pressure())
    val location by flightStatusViewModel.locationFlow.collectAsStateWithLifecycle(initialValue = Location("none"))
    val verticalAcceleration by flightStatusViewModel.verticalAccelerationFlow.collectAsStateWithLifecycle(initialValue = SensorData())
    val calibrationUiState by flightStatusViewModel.calibrationUiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataCell(
                caption = "Altitude",
                value = LocalUnit
                    .of(altitude.altitude, UnitConverter.Unit.M)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = "m",
                modifier = Modifier.weight(1f)
            )
            DataCell(
                caption = "Climbrate",
                value = LocalUnit
                    .of(climbrate.climbrate, UnitConverter.Unit.MS)
                    .withDigits(2)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(
                    climbrate.climbrate,
                    UnitConverter.Unit.MS)
                    .toLocalSymbol(),
                modifier = Modifier.weight(1f)
            )
            DataCell(
                caption = "Pressure",
                value = LocalUnit
                    .of(pressure.pressure, UnitConverter.Unit.M) // todo: hpa
                    .withSymbol(false)
                    .withDigits(1)
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
                caption = "Vert. Accel.",
                value = LocalUnit.of(
                    verticalAcceleration.values[0],
                    UnitConverter.Unit.M)
                    .withSymbol(false)
                    .withDigits(2)
                    .toLocalString(),
                unit = "a",
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

