package com.alpsfly.aeroglide.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.alpsfly.aeroglide.AeroGlideBottomBar
import com.alpsfly.aeroglide.ui.page.Page1
import com.alpsfly.aeroglide.ui.page.Page2
import com.alpsfly.aeroglide.ui.util.MenuItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(navController: NavController) {

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = {
        3
    })

    val items: List<MenuItem> = listOf(
        MenuItem(
            id = "home",
            title = "Home",
            contentDescription = "Go to home screen",
            icon = Icons.Default.Home
        ),
        MenuItem(
            id = "settings",
            title = "Settings",
            contentDescription = "Go to settings screen",
            icon = Icons.Default.Settings
        ),
        MenuItem(
            id = "help",
            title = "Help",
            contentDescription = "Get help",
            icon = Icons.Default.Info
        ),
    )

    Scaffold(
        bottomBar = {
            AeroGlideBottomBar(
                modifier = Modifier,
                items = items,
                pagerState = pagerState,
                coroutineScope = coroutineScope
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
        ) {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> Page1()
                    1 -> Page2()
                }
            }
        }
    }
}