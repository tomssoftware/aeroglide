package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.toLocalDateString
import com.alpsfly.aeroglide.core.common.toLocalDurationString
import com.alpsfly.aeroglide.core.common.toLocalTimeString
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
import com.alpsfly.aeroglide.core.item.DataField
import com.alpsfly.aeroglide.core.model.database.Activity
import timber.log.Timber
import com.alpsfly.aeroglide.core.ui.R as uiR

@Composable
fun ActivityDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    id: Long,
    activityHistoryViewModel: ActivityHistoryViewModel = hiltViewModel(),
) {
    val activityUiState = activityHistoryViewModel.activity.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = id) {
        activityHistoryViewModel.loadActivityById(id)
    }

    when (activityUiState.value) {
        is ActivityUiState.Loading -> {
            Timber.d("Loading")
        }
        is ActivityUiState.Success -> {
            val activity = (activityUiState.value as ActivityUiState.Success).item
            Timber.d("Success ${activity.activityId}")
            ActivityDataRow(activity)
        }
    }
}

@Composable
fun ActivityDataRow(activity: Activity) {
    Column(modifier = Modifier.padding(8.dp)) {
        Row {
            DataField(
                caption = stringResource(uiR.string.sid_date),
                value = toLocalDateString(activity.begin),
                drawableRes = R.drawable.calendar_month_24px,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(uiR.string.sid_time),
                value = toLocalTimeString(activity.begin),
                drawableRes = R.drawable.nest_clock_farsight_digital_24px,
                modifier = Modifier.weight(1f)
            )
        }
        Row {
            DataField(
                caption = stringResource(uiR.string.sid_duration),
                value = toLocalDurationString(activity.duration),
                drawableRes = R.drawable.timer_24px,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(uiR.string.sid_distance),
                value = LocalUnit
                    .of(activity.distance, UnitConverter.Unit.KM)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.KM).toLocalSymbol(),
                drawableRes = R.drawable.straighten_24px,
                modifier = Modifier.weight(1f)
            )
        }
        Row {
            DataField(
                caption = stringResource(uiR.string.sid_ascent),
                value = LocalUnit
                    .of(activity.ascent, UnitConverter.Unit.M)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.M).toLocalSymbol(),
                drawableRes = com.alpsfly.aeroglide.core.ui.R.drawable.stairs_24px,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(uiR.string.sid_descent),
                value = LocalUnit
                    .of(activity.descent, UnitConverter.Unit.M)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.M).toLocalSymbol(),
                drawableRes = com.alpsfly.aeroglide.core.ui.R.drawable.stairs_24px,
                modifier = Modifier.weight(1f)
            )
        }
        Row {
            DataField(
                caption = stringResource(uiR.string.sid_max_altitude),
                value = LocalUnit
                    .of(activity.maxAltitude, UnitConverter.Unit.M)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.M).toLocalSymbol(),
                drawableRes = R.drawable.landscape_24px,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(uiR.string.sid_min_altitude),
                value = LocalUnit
                    .of(activity.minAltitude, UnitConverter.Unit.M)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.M).toLocalSymbol(),
                drawableRes = R.drawable.landscape_24px,
                modifier = Modifier.weight(1f)
            )
        }
        Row {
            DataField(
                caption = stringResource(uiR.string.sid_max_climbrate),
                value = LocalUnit
                    .of(activity.maxClimbrate, UnitConverter.Unit.MS)
                    .withDigits(1)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.MS).toLocalSymbol(),
                drawableRes = R.drawable.swap_vert_24px,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(uiR.string.sid_min_climbrate),
                value = LocalUnit
                    .of(activity.minClimbrate, UnitConverter.Unit.MS)
                    .withDigits(1)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.MS).toLocalSymbol(),
                drawableRes = R.drawable.swap_vert_24px,
                modifier = Modifier.weight(1f)
            )
        }
        Row {
            DataField(
                caption = stringResource(uiR.string.sid_avg_climbrate_pos),
                value = LocalUnit
                    .of(activity.positiveAvgClimbrate, UnitConverter.Unit.MS)
                    .withDigits(2)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.MS).toLocalSymbol(),
                drawableRes = R.drawable.swap_vert_24px,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(uiR.string.sid_avg_climbrate_neg),
                value = LocalUnit
                    .of(activity.negativeAvgClimbrate, UnitConverter.Unit.MS)
                    .withDigits(2)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.MS).toLocalSymbol(),
                drawableRes = R.drawable.swap_vert_24px,
                modifier = Modifier.weight(1f)
            )
        }
        Row {
            DataField(
                caption = stringResource(uiR.string.sid_max_speed),
                value = LocalUnit
                    .of(activity.maxSpeed, UnitConverter.Unit.MS)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.KMH).toLocalSymbol(),
                drawableRes = R.drawable.shutter_speed_24px,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(uiR.string.sid_avg_speed),
                value = LocalUnit
                    .of(activity.avgSpeed, UnitConverter.Unit.MS)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.KMH).toLocalSymbol(),
                drawableRes = R.drawable.avg_pace_24px,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

