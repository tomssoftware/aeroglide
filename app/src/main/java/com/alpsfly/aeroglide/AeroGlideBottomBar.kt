package com.alpsfly.aeroglide

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpsfly.aeroglide.ui.util.MenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AeroGlideBottomBar(modifier: Modifier, items: List<MenuItem>, pagerState: PagerState, coroutineScope: CoroutineScope) {
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