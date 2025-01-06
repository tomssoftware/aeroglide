package com.alpsfly.aeroglide.feature.disclaimer.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.feature.disclaimer.viewmodel.DisclaimerViewModel


@Composable
fun WelcomeDialog(
    modifier: Modifier = Modifier,
    navController: NavController,
    onDismiss: () -> Unit,
    disclaimerViewModel: DisclaimerViewModel = hiltViewModel()
) {
    //val disclaimerUiState by disclaimerViewModel.disclaimerUiState.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                when (disclaimerUiState) {
//                    is DisclaimerUiState.Loading -> {
//                        Text(
//                            text = "Welcome to the App! Loading ...",
//                            style = MaterialTheme.typography.headlineSmall,
//                            modifier = Modifier.padding(bottom = 8.dp)
//                        )
//                    }
//
//                    is DisclaimerUiState.Success -> {
//                        Text(
//                            text = "Welcome to the App!",
//                            style = MaterialTheme.typography.headlineSmall,
//                            modifier = Modifier.padding(bottom = 8.dp)
//                        )
//                    }
//                }

                Text(
                    text = "Thank you for using our app. We hope you enjoy it!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = onDismiss) {
                    Text("Get Started")
                }
            }
        }
    }
}