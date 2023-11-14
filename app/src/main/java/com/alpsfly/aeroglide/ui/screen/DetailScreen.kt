package com.alpsfly.aeroglide.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alpsfly.aeroglide.ui.page.Page1
import com.alpsfly.aeroglide.ui.page.Page2
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(navController: NavController) {

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = {
        3
    })

    data class MenuItem(
        val id: String,
        val title: String,
        val contentDescription: String,
        val icon: ImageVector
    )

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
            BottomAppBar {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    itemsIndexed(items) { page, item ->
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(page, 0f)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.contentDescription
                            )
                        }
                    }
                }
            }
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