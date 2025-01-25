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
import com.alpsfly.aeroglide.core.common.toLocalTimeString
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
import com.alpsfly.aeroglide.core.item.DataField
import com.alpsfly.aeroglide.core.model.database.Activity

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
            ActivityDataRow(uiElementList, R.drawable.timer_24px)
        }
    }
}

@Composable
fun ActivityDataRow(activity: Activity, drawableRes: Int) {
    Column(modifier = Modifier.padding(8.dp)) {
        Row {
            DataField(
                caption = stringResource(com.alpsfly.aeroglide.core.ui.R.string.sid_date),
                value = toLocalDateString(activity.begin),
                drawableRes = R.drawable.calendar_month_24px,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(com.alpsfly.aeroglide.core.ui.R.string.sid_time),
                value = toLocalTimeString(activity.begin),
                drawableRes = R.drawable.nest_clock_farsight_digital_24px,
                modifier = Modifier.weight(1f)
            )
        }
        Row {
            DataField(
                caption = stringResource(com.alpsfly.aeroglide.core.ui.R.string.sid_duration),
                value = toLocalTimeString(activity.end - activity.begin),
                drawableRes = R.drawable.timer_24px,
                modifier = Modifier.weight(1f)
            )
            DataField(
                caption = stringResource(com.alpsfly.aeroglide.core.ui.R.string.sid_distance),
                value = LocalUnit
                    .of(activity.distance, UnitConverter.Unit.KM)
                    .withDigits(0)
                    .withSymbol(false)
                    .toLocalString(),
                unit = "km",
                drawableRes = R.drawable.straighten_24px,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

