package com.alpsfly.aeroglide.core

sealed class Screen(val route: String) {
    data object SettingsScreen : Screen(route = "SettingsScreen")
    data object DiagnosisDetailScreen : Screen(route = "DiagnosisDetailScreen")
    data object DiagnosisScreen : Screen(route = "DiagnosisScreen")
    data object VariometerScreen : Screen(route = "VariometerScreen")
}