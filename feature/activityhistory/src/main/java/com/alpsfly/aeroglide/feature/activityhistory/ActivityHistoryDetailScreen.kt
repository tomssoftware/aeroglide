package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun ActivityHistoryDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    id: Long = 0L,
    activityHistoryViewModel: ActivityHistoryViewModel = hiltViewModel(),
) {
    val activityUiState = activityHistoryViewModel.getActivityUiState(id).collectAsStateWithLifecycle()

    when (activityUiState.value) {
        is ActivityUiState.Loading -> {
        }
        is ActivityUiState.Success -> {
            val uiElementList = (activityUiState.value as ActivityUiState.Success).uiElementList
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    for (i in uiElementList.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            GridItem(modifier = Modifier.weight(1f), item = uiElementList[i])
                            if (i + 1 < uiElementList.size) {
                                GridItem(modifier = Modifier.weight(1f), item = uiElementList[i + 1])
                            } else {
                                Spacer(modifier = Modifier.weight(1f)) // Add spacer for empty cell
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridItem(modifier: Modifier = Modifier, item: UiActivityItem) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Image(
            painter = painterResource(id = item.drawableId),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = item.caption, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = item.value, fontSize = 12.sp)
        }
    }
}

