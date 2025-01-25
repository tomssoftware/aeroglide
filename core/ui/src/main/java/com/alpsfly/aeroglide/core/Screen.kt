package com.alpsfly.aeroglide.core

sealed class Screen(val route:String) {
    data object DeviceStatusScreen : Screen(route = "DeviceStatusScreen")
    data object DiagnosisDetailScreen : Screen(route = "DiagnosisDetailScreen")
    data object DiagnosisScreen : Screen(route = "DiagnosisScreen")
    data object VariometerScreen : Screen(route = "VariometerScreen")
}