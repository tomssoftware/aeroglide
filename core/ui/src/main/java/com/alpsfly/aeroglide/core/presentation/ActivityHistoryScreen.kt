package com.alpsfly.aeroglide.core.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.viewmodel.ActivityHistoryUiState
import com.alpsfly.aeroglide.core.viewmodel.ActivityHistoryViewModel


@Composable
fun ActivityHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val activityHistoryUiState by viewModel.activityHistoryUiState.collectAsStateWithLifecycle()

    when (activityHistoryUiState) {
        ActivityHistoryUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ActivityHistoryUiState.Success -> {
            val activityHistoryList = (activityHistoryUiState as ActivityHistoryUiState.Success).activityHistory
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(activityHistoryList) { activity ->
                    CardItem(activity = activity)
                }
            }
        }
    }
}

@Composable
fun CardItem(activity: Activity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = cardElevation()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Image(
//                painter = painterResource(id = activity.leftDrawableId),
//                contentDescription = null,
//                modifier = Modifier.size(48.dp)
//            )
//            Spacer(modifier = Modifier.width(16.dp))
//            Column(modifier = Modifier.weight(1f)) {
//                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
//                    Text(text = activity.text1, fontSize = 16.sp)
//                    Text(text = activity.userId, fontSize = 16.sp)
//                }
//                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
//                    Text(text = activity.text3, fontSize = 16.sp)
//                    Text(text = activity.text4, fontSize = 16.sp)
//                }
//                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
//                    Text(text = activity.text5, fontSize = 16.sp)
//                    Text(text = activity.text6, fontSize = 16.sp)
//                }
//            }
//            Spacer(modifier = Modifier.width(16.dp))
//            Image(
//                painter = painterResource(id = activity.rightDrawableId),
//                contentDescription = null,
//                modifier = Modifier.size(48.dp)
//            )
        }
    }
}