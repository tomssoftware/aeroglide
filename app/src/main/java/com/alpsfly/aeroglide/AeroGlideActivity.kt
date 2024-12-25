package com.alpsfly.aeroglide

import android.os.Bundle
import android.Manifest
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.alpsfly.aeroglide.core.domain.audio.BeepGeneratorImpl
import com.alpsfly.aeroglide.core.data.service.LocationService
import com.alpsfly.aeroglide.core.ui.theme.AeroGlideTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AeroGlideActivity : ComponentActivity() {

    private lateinit var beepGenerator: BeepGeneratorImpl

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("CREATE MAIN ACTIVITY")

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )

        beepGenerator = BeepGeneratorImpl()

        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }

        setContent {
            AeroGlideTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                val viewModel: AeroGlideViewModel = hiltViewModel()
                Scaffold(
                    modifier = Modifier,
                    topBar = {
                        AeroGlideTopAppBar(
                            title = "AeroGlide",
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            if (drawerState.isClosed)
                                                drawerState.open()
                                            else
                                                drawerState.close()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Main Menu"
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    if (viewModel.isRecording())
                                        viewModel.stopRecording()
                                    else
                                        viewModel.startRecording()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Mark as favorite"
                                    )
                                }
                                IconButton(onClick = {
                                    beepGenerator.setFrequency(500f) // Set frequency to 880 Hz
                                    beepGenerator.setDuration(1000L) // Set duration to 2 seconds
                                    beepGenerator.playBeep()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = "Edit notes"
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        AeroGlideNavDrawer(
                            navController = navController,
                            drawerState = drawerState,
                            onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed)
                                        drawerState.open()
                                    else
                                        drawerState.close()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
