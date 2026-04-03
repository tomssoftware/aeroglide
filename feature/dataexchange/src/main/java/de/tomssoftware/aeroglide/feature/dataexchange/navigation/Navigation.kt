package de.tomssoftware.aeroglide.feature.dataexchange.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import de.tomssoftware.aeroglide.feature.dataexchange.DataExportScreen
import de.tomssoftware.aeroglide.navigation.DataExportRoute

fun NavGraphBuilder.dataExport(navController: NavController) {
    composable<DataExportRoute> { backStackEntry ->
        DataExportScreen(
            route = backStackEntry.toRoute<DataExportRoute>(),
            onDismiss = { navController.popBackStack() }
        )
    }
}
