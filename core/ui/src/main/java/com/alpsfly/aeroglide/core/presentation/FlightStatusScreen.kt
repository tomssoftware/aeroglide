package com.alpsfly.aeroglide.core.presentation

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
import com.alpsfly.aeroglide.core.item.DataField
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.ui.R
import com.alpsfly.aeroglide.core.viewmodel.CalibrationUiState
import com.alpsfly.aeroglide.core.viewmodel.FlightStatusViewModel

@Composable
fun FlightStatusScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    flightStatusViewModel: FlightStatusViewModel = hiltViewModel()
) {
    val activity by flightStatusViewModel.activityFlow.collectAsStateWithLifecycle(initialValue = Activity())
    val altitude by flightStatusViewModel.altitudeFlow.collectAsStateWithLifecycle(initialValue = Altitude())
    val climbrate by flightStatusViewModel.climbrateFlow.collectAsStateWithLifecycle(initialValue = Climbrate())
    val location by flightStatusViewModel.locationFlow.collectAsStateWithLifecycle(initialValue = Location())
    val calibration by flightStatusViewModel.calibrationUiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataField(
                caption = stringResource(R.string.sid_altitude),
                value = LocalUnit
                    .of(altitude.altitude, UnitConverter.Unit.M)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.M).toLocalSymbol(),
                blinking = calibration is CalibrationUiState.Loading,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(R.string.sid_climbrate),
                value = LocalUnit
                    .of(climbrate.climbrate, UnitConverter.Unit.MS)
                    .withDigits(2)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.MS).toLocalSymbol(),
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(R.string.sid_speed),
                value = LocalUnit
                    .of(location.speed, UnitConverter.Unit.KMH)
                    .withDigits(0)
                    .withSymbol( flag = false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.KMH).toLocalSymbol(),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataField(
                caption = stringResource(R.string.sid_distance),
                value = LocalUnit
                    .of(activity?.distance ?: 0f, UnitConverter.Unit.M)
                    .withSymbol(flag = false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.M).toLocalSymbol(),
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(R.string.sid_glide_ratio),
                value = "0",
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(R.string.sid_duration),
                value = DateUtils.formatElapsedTime(activity?.duration ?: 0L),
                unit = "",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

