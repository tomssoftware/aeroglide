package com.alpsfly.aeroglide.core.ui

sealed class Screen(val route:String) {
    data object VariometerScreen : Screen(route = "VariometerScreen")
    data object DeviceStatusScreen : Screen(route = "DeviceStatusScreen")
    data object Screen3 : Screen(route = "page_3")
    data object Screen4 : Screen(route = "page_4")
    data object DetailScreen : Screen(route = "detailScreen")
}