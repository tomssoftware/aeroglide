package com.alpsfly.aeroglide.feature.livetracking

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
import com.alpsfly.aeroglide.core.item.DataField
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.GlideRatio
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.ui.R

@Composable
fun FlightStatusScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    flightStatusViewModel: FlightStatusViewModel = hiltViewModel()
) {
    val activity by flightStatusViewModel.activityFlow.collectAsStateWithLifecycle(initialValue = Activity())
    val altitude by flightStatusViewModel.altitudeFlow.collectAsStateWithLifecycle(initialValue = Altitude())
    val climbrate by flightStatusViewModel.climbrateFlow.collectAsStateWithLifecycle(initialValue = Climbrate())
    val glideRatio by flightStatusViewModel.glideRatioFlow.collectAsStateWithLifecycle(initialValue = GlideRatio())
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
                    .of(location.speed, UnitConverter.Unit.MS)
                    .withDigits(0)
                    .withSymbol( flag = false)
                    .toUnit(UnitConverter.Unit.KMH)
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
                value = LocalUnit
                    .of(glideRatio.glideRatio, UnitConverter.Unit.M) // none
                    .withDigits(0)
                    .withSymbol(flag = false)
                    .toLocalString(),
                unit = "", // none
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(R.string.sid_duration),
                value = DateUtils.formatElapsedTime(activity?.duration ?: 0),
                unit = "",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataField(
                caption = stringResource(R.string.sid_avg_climbrate_pos),
                value = LocalUnit
                    .of(activity?.positiveAvgClimbrate ?: 0f, UnitConverter.Unit.MS)
                    .withDigits(2)
                    .withSymbol(flag = false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.MS).toLocalSymbol(),
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(R.string.sid_avg_climbrate_neg),
                value = LocalUnit
                    .of(activity?.negativeAvgClimbrate ?: 0f, UnitConverter.Unit.MS)
                    .withDigits(2)
                    .withSymbol(flag = false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.MS).toLocalSymbol(),
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(R.string.sid_avg_speed),
                value = LocalUnit
                    .of(activity?.avgSpeed ?: 0f, UnitConverter.Unit.MS)
                    .withDigits(0)
                    .withSymbol(flag = false)
                    .toUnit(UnitConverter.Unit.KMH)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.KMH).toLocalSymbol(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

