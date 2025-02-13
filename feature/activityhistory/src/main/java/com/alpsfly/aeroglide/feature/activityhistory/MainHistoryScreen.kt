package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.MenuItem
import com.alpsfly.aeroglide.core.presentation.AeroGlideBottomBar
import com.alpsfly.aeroglide.core.presentation.ClimbrateProfileScreen
import com.alpsfly.aeroglide.core.ui.R

@Composable
fun MainHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    activityId: Long
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        pageCount = { 4 }
    )
    val items: List<MenuItem> = listOf(
        MenuItem(
            id = R.string.sid_activities.toString(),
            title = stringResource(R.string.sid_activities),
            contentDescription = stringResource(R.string.sid_activities),
            icon = ImageVector.vectorResource(R.drawable.list_alt_24px)
        ),
        MenuItem(
            id = R.string.sid_altitude.toString(),
            title = stringResource(R.string.sid_altitude),
            contentDescription = stringResource(R.string.sid_altitude),
            icon = ImageVector.vectorResource(R.drawable.altitude_24px)
        ),
        MenuItem(
            id = R.string.sid_climbrate.toString(),
            title = stringResource(R.string.sid_climbrate),
            contentDescription = stringResource(R.string.sid_climbrate),
            icon = ImageVector.vectorResource(R.drawable.stairs_24px)
        ),
        MenuItem(
            id = R.string.sid_map.toString(),
            title = stringResource(R.string.sid_map),
            contentDescription = stringResource(R.string.sid_map),
            icon = ImageVector.vectorResource(R.drawable.map_24px)
        ),
    )

    Scaffold(
        bottomBar = {
            AeroGlideBottomBar(
                modifier = Modifier,
                items = items,
                pagerState = pagerState,
                coroutineScope = coroutineScope
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                HorizontalPager(state = pagerState) { page ->
                    when (page) {

                        0 -> {
                            DetailHistoryScreen(
                                modifier = Modifier.fillMaxSize(),
                                navController = navController,
                                activityId = activityId,
                            )
                        }

                        1 -> {
                            AltitudeHistoryScreen(
                                modifier = Modifier.fillMaxSize(),
                                navController = navController,
                                activityId = activityId
                            )
                        }

                        2 -> {
                            ClimbrateHistoryScreen(
                                modifier = Modifier.fillMaxSize(),
                                navController = navController,
                                activityId = activityId
                            )
                        }

                        4 -> {
                            MapHistoryScreen(
                                modifier = Modifier.fillMaxSize(),
                                navController = navController,
                            )
                        }
                    }
                }
            }
        }
    }
}
