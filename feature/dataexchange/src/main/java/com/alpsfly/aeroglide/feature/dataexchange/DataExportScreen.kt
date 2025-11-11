package com.alpsfly.aeroglide.feature.dataexchange


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.navigation.DataExportRoute
import kotlinx.coroutines.flow.collectLatest


@Composable
fun DataExportScreen(
    route: DataExportRoute,
    viewModel: DataExportViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // This LaunchedEffect will listen for one-time events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.shareEventFlow.collectLatest { event ->
            when (event) {
                is ShareEvent.ShareFile -> {
                    context.startActivity(event.intent)
                    onDismiss() // Dismiss the dialog after sharing
                }
            }
        }
    }
    DataExportDetailScreen(
        route = route,
        navController = NavController(context)
    )
}

@Composable
fun DataExportDetailScreen(
    navController: NavController, // Remains for future use or can be removed if not needed by parent
    route: DataExportRoute,
    viewModel: DataExportViewModel = hiltViewModel(),
) {
    val (selectedIndex, setSelectedIndex) = rememberSaveable { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Share your flight",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        val formatOptions = listOf(
            "GPX" to "GPX – GPS Exchange Format",
            "IGC" to "IGC – Int. Gliding Commission Format",
            "CSV" to "CSV – Comma-Separated Values"
        )

        formatOptions.forEachIndexed { index, (_, description) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { setSelectedIndex(index) }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedIndex == index,
                    onClick = { setSelectedIndex(index) }
                )
                Text(
                    text = description
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = {
                    viewModel.onExport(
                        trackId = route.id,
                        format = formatOptions[selectedIndex].first
                    )
                }
            ) {
                Text(text = "Share")
            }
        }
    }
}

