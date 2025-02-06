package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data object ActivityListRoute
@Serializable data class ActivityDetailRoute(val activityId: Long)

fun NavController.navigateToActivityList(navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = ActivityListRoute) {
        navOptions()
    }
}

fun NavGraphBuilder.activityList(navController: NavController) {
    composable<ActivityListRoute> {
        ActivityListScreen(navController = navController)
    }
}

fun NavController.navigateToActivityDetail(activityId: Long, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = ActivityDetailRoute(activityId)) {
        navOptions()
    }
}

//fun NavGraphBuilder.activityDetail(navController: NavController) {
//    composable<ActivityDetailRoute> { backStackEntry ->
//        val args: ActivityDetailRoute = backStackEntry.toRoute()
//        ActivityDetailScreen(navController = navController, id = args.activityId)
//    }
//}

fun NavGraphBuilder.activityDetail(navController: NavController) {
    composable<ActivityDetailRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<ActivityDetailRoute>()

        // Ensure that the id remains stable
        val id = remember { args.activityId }

        ActivityDetailScreen(navController = navController, id = id)
    }
}

