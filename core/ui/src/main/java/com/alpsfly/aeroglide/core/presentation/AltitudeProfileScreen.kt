package com.alpsfly.aeroglide.core.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.viewmodel.AltitudeProfileViewModel

@Composable
fun AltitudeProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    altitudeProfileViewModel: AltitudeProfileViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
    ) {
        LineChartScreen(altitudeProfileViewModel.altitudeModelProducer, Modifier)
    }
}
