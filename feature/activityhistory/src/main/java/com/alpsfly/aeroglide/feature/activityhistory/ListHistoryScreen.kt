package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.toLocalDateString
import com.alpsfly.aeroglide.core.common.toLocalTimeString
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
import com.alpsfly.aeroglide.core.model.database.Activity

@Composable
fun ListHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ActivityViewModel = hiltViewModel()
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
                items(
                    items = activityHistoryList,
                    key = { activity -> activity.activityId }
                ) { activity ->
                    ActivityItem(
                        navController = navController,
                        activity = activity,
                        modifier = Modifier.animateItem(),
                        onRemove = viewModel::deleteActivity
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityCard(
    modifier: Modifier = Modifier,
    navController: NavController,
    activity: Activity
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        onClick = {
            navController.navigateToActivityMain(activity.activityId)
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
                            .of(activity.distance / 1000f, UnitConverter.Unit.KM)
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

@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFF1744)
        SwipeToDismissBoxValue.EndToStart -> Color(0xFF1DE9B6)
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "delete"
        )
        Spacer(modifier = Modifier)
        Icon(
            // make sure add baseline_archive_24 resource to drawable folder
            painter = painterResource(R.drawable.timer_24px),
            contentDescription = "Archive"
        )
    }
}

@Composable
fun ActivityItem(
    activity: Activity,
    modifier: Modifier = Modifier,
    navController: NavController,
    onRemove: (Activity) -> Unit
) {
    val currentItem by rememberUpdatedState(activity)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onRemove(currentItem)
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onRemove(currentItem)
                }

                SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
            }
            return@rememberSwipeToDismissBoxState true
        },
        positionalThreshold = { it * .50f }
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { DismissBackground(dismissState) },
        content = {
            ActivityCard(
                navController = navController,
                activity = activity
            )
        }
    )
}