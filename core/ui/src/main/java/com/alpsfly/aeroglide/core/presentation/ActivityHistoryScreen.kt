package com.alpsfly.aeroglide.core.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.toLocalDateString
import com.alpsfly.aeroglide.core.common.toLocalTimeString
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
import com.alpsfly.aeroglide.core.viewmodel.ActivityHistoryData
import com.alpsfly.aeroglide.core.viewmodel.ActivityHistoryUiState
import com.alpsfly.aeroglide.core.viewmodel.ActivityHistoryViewModel
import com.alpsfly.aeroglide.core.ui.R

@Composable
fun ActivityHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val activityHistoryUiState by viewModel.activityHistoryUiState.collectAsStateWithLifecycle()

    when (activityHistoryUiState) {
        ActivityHistoryUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ActivityHistoryUiState.Success -> {
            val activityHistoryList = (activityHistoryUiState as ActivityHistoryUiState.Success).activityHistory
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(activityHistoryList) { activity ->
                    CardItem(activity = activity)
                }
            }
        }
    }
}

@Composable
fun CardItem(activity: ActivityHistoryData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = cardElevation()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.outline_play_arrow_24),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.sid_date), fontSize = 16.sp)
                    Text(text = toLocalDateString(activity.date), fontSize = 16.sp)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.sid_time), fontSize = 16.sp)
                    Text(text = toLocalTimeString(activity.time), fontSize = 16.sp)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.sid_distance), fontSize = 16.sp)
                    Text(
                        text = LocalUnit
                            .of(activity.distance/1000f, UnitConverter.Unit.KM)
                            .withDigits(2)
                            .toLocalString(),
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.outline_stop_24),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}