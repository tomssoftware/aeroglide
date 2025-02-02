package com.alpsfly.aeroglide.feature.activityhistory

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.alpsfly.aeroglide.core.ui.R as uiR

@Composable
fun ActivityDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    id: Long = 0L,
    activityHistoryViewModel: ActivityHistoryViewModel = hiltViewModel(),
) {
    val activityUiState = activityHistoryViewModel.getActivityUiState(id).collectAsStateWithLifecycle()

    when (activityUiState.value) {
        is ActivityUiState.Loading -> {
            val context = LocalContext.current
            Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show()
        }
        is ActivityUiState.Success -> {
            val uiElementList = (activityUiState.value as ActivityUiState.Success).activity
            ActivityDataRow(uiElementList)
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
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = LocalUnit.of(UnitConverter.Unit.MS).toLocalSymbol(),
                drawableRes = R.drawable.landscape_24px,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

