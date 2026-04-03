package de.tomssoftware.aeroglide.feature.activityhistory

import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data object ActivityListRoute
@Serializable data class ActivityMainRoute(val activityId: Long)

fun NavController.navigateToActivityList(navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = ActivityListRoute) {
        navOptions()
    }
}

fun NavGraphBuilder.activityList(navController: NavController) {
    composable<ActivityListRoute> {
        ListHistoryScreen(navController = navController)
    }
}

fun NavController.navigateToActivityMain(activityId: Long, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = ActivityMainRoute(activityId)) {
        navOptions()
    }
}

fun NavGraphBuilder.activityMain(navController: NavController) {
    composable<ActivityMainRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<ActivityMainRoute>()

        // Ensure that the id remains stable
        val id = remember { args.activityId }

        MainHistoryScreen(navController = navController, activityId = id)
    }
}

