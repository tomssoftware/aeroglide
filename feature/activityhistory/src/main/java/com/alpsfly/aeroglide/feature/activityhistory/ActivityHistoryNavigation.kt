package com.alpsfly.aeroglide.feature.activityhistory

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data class ActivityHistoryListRoute(val activityId: Long)
@Serializable data class ActivityHistoryDetailRoute(val activityId: Long)

fun NavController.navigateToActivityHistoryList(activityId: Long, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = ActivityHistoryListRoute(activityId)) {
        navOptions()
    }
}

fun NavGraphBuilder.activityHistoryList(navController: NavController) {
    composable<ActivityHistoryListRoute> { backStackEntry ->
        val args: ActivityHistoryListRoute = backStackEntry.toRoute()
        ActivityHistoryListScreen(navController = navController)
    }
}

fun NavController.navigateToActivityHistoryDetail(activityId: Long, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = ActivityHistoryListRoute(activityId)) {
        navOptions()
    }
}

fun NavGraphBuilder.activityHistoryDetail(navController: NavController) {
    composable<ActivityHistoryDetailRoute> { backStackEntry ->
        val args: ActivityHistoryDetailRoute = backStackEntry.toRoute()
        ActivityHistoryDetailScreen(navController = navController, id = args.activityId)
    }
}