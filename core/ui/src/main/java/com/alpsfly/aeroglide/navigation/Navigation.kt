package com.alpsfly.aeroglide.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import kotlinx.serialization.Serializable

@Serializable
data class DataExportRoute(val id: Long)

fun NavController.navigateToDataExportScreen(activityId: Long, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = DataExportRoute(activityId)) {
        navOptions()
    }
}


