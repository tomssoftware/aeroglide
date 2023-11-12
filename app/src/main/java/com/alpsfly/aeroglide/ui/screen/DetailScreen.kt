package com.alpsfly.aeroglide.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(navController: NavController) {
    val pages = listOf(
        "Page 1",
        "Page 2",
        "Page 3",
        "Page 4"
    )

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = {
        4
    })

    Scaffold(
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(0, 0f)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Mark as favorite"
                        )
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(1, 0f)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Edit notes"
                        )
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(2, 0f)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Mark as favorite"
                        )
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(3, 0f)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Mark as favorite"
                        )
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
                // Our page content
                Text(
                    text = "Page: $page",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                )
            }
        }
    }
}