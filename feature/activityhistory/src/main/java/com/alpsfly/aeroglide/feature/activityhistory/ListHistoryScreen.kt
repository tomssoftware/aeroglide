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
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.alpsfly.aeroglide.navigation.navigateToDataExportScreen

/**
 * Main screen for viewing the history of recorded activities.
 */
@Composable
fun ListHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val activityHistoryUiState by viewModel.allActivitiesUiState.collectAsStateWithLifecycle()
    
    // Smart casting using a local variable
    val state = activityHistoryUiState
    
    when (state) {
        ActivityListUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is ActivityListUiState.Success -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.list,
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

/**
 * A card representing an individual flight activity.
 */
@Composable
fun ActivityCard(
    modifier: Modifier = Modifier,
    navController: NavController,
    activity: Activity
) {
    Card(
        modifier = modifier
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
            IconButton(
                onClick = {
                    navController.navigateToDataExportScreen(activity.activityId)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.share_24px),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}

/**
 * Provides the background visuals for the swipe-to-dismiss action.
 */
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFF1744) // Red for delete
        SwipeToDismissBoxValue.EndToStart -> Color(0xFF1DE9B6) // Teal for archive/timer
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete"
        )
        Icon(
            painter = painterResource(R.drawable.timer_24px),
            contentDescription = "Archive"
        )
    }
}

/**
 * A wrapper for [ActivityCard] that implements the swipe-to-dismiss pattern.
 */
@Composable
fun ActivityItem(
    activity: Activity,
    modifier: Modifier = Modifier,
    navController: NavController,
    onRemove: (Activity) -> Unit
) {
    val currentItem by rememberUpdatedState(activity)
    
    // Explicit key fixes the "disappearing items" bug by ensuring state is tied to the ID
    val dismissState = key(activity.activityId) {
        rememberSwipeToDismissBoxState(
            positionalThreshold = { distance -> distance * 0.5f }
        )
    }
    
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onRemove(currentItem)
        }
    }
    
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
