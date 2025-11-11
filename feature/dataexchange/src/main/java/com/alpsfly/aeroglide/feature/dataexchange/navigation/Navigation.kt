package com.alpsfly.aeroglide.feature.dataexchange.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.alpsfly.aeroglide.feature.dataexchange.DataExportScreen
import com.alpsfly.aeroglide.navigation.DataExportRoute

fun NavGraphBuilder.dataExport(navController: NavController) {
    composable<DataExportRoute> { backStackEntry ->
        DataExportScreen(
            route = backStackEntry.toRoute<DataExportRoute>(),
            onDismiss = { navController.popBackStack() }
        )
    }
}
