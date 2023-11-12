package com.alpsfly.aeroglide.ui.screen

sealed class Screen(val route:String) {
    data object Screen1 : Screen(route = "page_1")
    data object Screen2 : Screen(route = "page_2")
    data object Screen3 : Screen(route = "page_3")
    data object DetailScreen : Screen(route = "detailScreen")
}