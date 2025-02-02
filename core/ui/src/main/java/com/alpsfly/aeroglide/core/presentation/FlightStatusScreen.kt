package com.alpsfly.aeroglide.core.presentation

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
import com.alpsfly.aeroglide.core.item.DataField
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Pressure
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.viewmodel.FlightStatusViewModel

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
            DataField(
                caption = "Altitude",
                value = LocalUnit
                    .of(altitude.altitude, UnitConverter.Unit.M)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = "m",
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = "Climbrate",
                value = LocalUnit
                    .of(climbrate?.climbrate ?: 0f, UnitConverter.Unit.MS)
                    .withDigits(2)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(0f,
                    UnitConverter.Unit.MS)
                    .toLocalSymbol(),
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = "Pressure",
                value = LocalUnit
                    .of(pressure.pressure, UnitConverter.Unit.M) // todo: hpa
                    .withSymbol(false)
                    .withDigits(0)
                    .toLocalString(),
                unit = "hpa",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataField(
                caption = "Speed",
                value = LocalUnit.of(location?.speed ?: 0f, UnitConverter.Unit.KMH)
                    .withSymbol(flag = false).toLocalString(),
                unit = "km/h",
                modifier = Modifier.weight(1f)
            )
            when (calibrationUiState) {
                is CalibrationUiState.Success -> {
                    DataField(
                        caption = "Alt. Acc.",
                        value = LocalUnit.of((calibrationUiState as CalibrationUiState.Success).calibration.verticalAccuracy, UnitConverter.Unit.M)
                            .withSymbol(false).toLocalString(),
                        unit = "ts",
                        modifier = Modifier.weight(1f)
                    )
                }
                is CalibrationUiState.Loading -> {
                    DataField(
                        caption = "Alt. Acc.",
                        value = "-",
                        unit = "-",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            DataField(
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

