package com.alpsfly.aeroglide.core

sealed class Screen(val route: String) {
    data object SettingsScreen : Screen(route = "SettingsScreen")
    data object DiagnosisDetailScreen : Screen(route = "DiagnosisDetailScreen")
    data object DiagnosisScreen : Screen(route = "DiagnosisScreen")
    data object VariometerScreen : Screen(route = "VariometerScreen")
    data object DataExportScreen : Screen(route = "DataExportScreen")
    data object PremiumsScreen : Screen(route = "PremiumsScreen")
    data object MapManagerScreen : Screen(route = "MapManagerScreen")
}