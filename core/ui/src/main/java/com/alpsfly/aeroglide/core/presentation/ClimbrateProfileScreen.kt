package com.alpsfly.aeroglide.core.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.viewmodel.ClimbrateProfileViewModel


@Composable
fun ClimbrateProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    climbrateProfileViewModel: ClimbrateProfileViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            LineChartScreen(
                modelProducer = climbrateProfileViewModel.climbrateModelProducer,
                modifier = Modifier
            )
        }
    }
}