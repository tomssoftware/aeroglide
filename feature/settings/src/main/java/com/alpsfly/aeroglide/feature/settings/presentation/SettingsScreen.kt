package com.alpsfly.aeroglide.feature.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.feature.settings.viewmodel.SettingsViewModel
import java.text.DecimalFormat

@Composable
fun PreferenceSlider(
    title: String,
    summary: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0 // Set to 0 for continuous slider
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = summary, style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun PreferenceSwitch(
    title: String,
    summary: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isEnabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = summary, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = isEnabled
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val autoStartSpeed by viewModel.autoStartSpeed.collectAsState()
    val autoStartClimbRate by viewModel.autoStartClimbRate.collectAsState()
    val varioClimbThreshold by viewModel.varioClimbThreshold.collectAsState()
    val varioSinkThreshold by viewModel.varioSinkThreshold.collectAsState()

    // Formatters for summary text
    val speedFormat = DecimalFormat("#,##0")
    val rateFormat = DecimalFormat("0.0")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(all = 8.dp)
        ) {
            item {
                PreferenceSwitch(
                    title = "Auto-Start",
                    summary = if (viewModel.autoStartEnabled.collectAsState().value) "Enabled" else "Disabled",
                    isChecked = viewModel.autoStartEnabled.collectAsState().value,
                    onCheckedChange = { viewModel.onAutoStartEnabled(it) },
                    isEnabled = true
                )
            }
            item {
                PreferenceSlider(
                    title = "Auto-Start Speed",
                    summary = "${speedFormat.format(autoStartSpeed)} km/h",
                    value = autoStartSpeed,
                    onValueChange = { viewModel.onAutoStartSpeedChange(it) },
                    valueRange = 0f..50f // Assuming a range
                )
            }
            item {
                PreferenceSlider(
                    title = "Auto-Start Climb Rate",
                    summary = "${rateFormat.format(autoStartClimbRate)} m/s",
                    value = autoStartClimbRate,
                    onValueChange = { viewModel.onAutoStartClimbRateChange(it) },
                    valueRange = 0.1f..5f // The UI sees the real value
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
            item {
                PreferenceSlider(
                    title = "Vario Climb Tone Threshold",
                    summary = "${rateFormat.format(varioClimbThreshold)} m/s",
                    value = varioClimbThreshold,
                    onValueChange = { viewModel.onVarioClimbThresholdChange(it) },
                    valueRange = 0.1f..2.0f
                )
            }
            item {
                PreferenceSlider(
                    title = "Vario Sink Tone Threshold",
                    summary = "${rateFormat.format(varioSinkThreshold)} m/s",
                    value = varioSinkThreshold,
                    onValueChange = { viewModel.onVarioSinkThresholdChange(it) },
                    valueRange = -5f..-0.1f
                )
            }
            // Add other preference items here...
            // e.g., PreferenceSwitch for accel sensor, etc.
        }
    }
}
