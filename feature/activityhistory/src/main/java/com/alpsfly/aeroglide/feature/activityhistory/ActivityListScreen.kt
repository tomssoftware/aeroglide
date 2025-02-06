package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.alpsfly.aeroglide.core.model.database.Activity

@Composable
fun ActivityListScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    id: Long = 0L,
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val activityHistoryUiState by viewModel.allActivitiesUiState.collectAsStateWithLifecycle()

    when (activityHistoryUiState) {
        ActivityListUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ActivityListUiState.Success -> {
            val activityHistoryList = (activityHistoryUiState as ActivityListUiState.Success).list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activityHistoryList) { activity ->
                    CardItem(
                        navController = navController,
                        activity = activity
                    )
                }
            }
        }
    }
}

@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    navController: NavController,
    activity: Activity
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = {
            navController.navigateToActivityDetail(activity.activityId)
        }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.check_small_24px),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(com.alpsfly.aeroglide.core.ui.R.string.sid_date), fontSize = 16.sp)
                    Text(text = toLocalDateString(activity.begin), fontSize = 16.sp)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(com.alpsfly.aeroglide.core.ui.R.string.sid_time), fontSize = 16.sp)
                    Text(text = toLocalTimeString(activity.end), fontSize = 16.sp)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(com.alpsfly.aeroglide.core.ui.R.string.sid_distance), fontSize = 16.sp)
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
            Icon(
                painter = painterResource(id = R.drawable.share_24px),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}