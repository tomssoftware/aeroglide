package com.alpsfly.aeroglide

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.lifecycle.coroutineScope
import androidx.navigation.compose.rememberNavController
import com.alpsfly.aeroglide.core.common.audio.BeepGeneratorImpl
import com.alpsfly.aeroglide.core.data.service.LocationService
import com.alpsfly.aeroglide.core.presentation.AeroGlideTopAppBar
import com.alpsfly.aeroglide.core.ui.R
import com.alpsfly.aeroglide.core.ui.theme.AeroGlideTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AeroGlideActivity : ComponentActivity() {

    private val aeroGlideViewModel: AeroGlideViewModel by viewModels()

    private lateinit var beepGenerator: BeepGeneratorImpl

    private val crashlytics = Firebase.crashlytics
    private val analytics = Firebase.analytics

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("CREATE MAIN ACTIVITY")

        crashlytics.setCustomKey("IS_RUNNING", true)

        val permission = (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        if (permission) {
            Timber.i("ACCESS_FINE_LOCATION PERMISSION GRANTED")
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                startService(this)
            }
            aeroGlideViewModel.startCalibration()
        } else {
            Timber.i("REQUEST ACCESS_FINE_LOCATION PERMISSION")
            val permissions = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
            requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }

        lifecycle.coroutineScope.launch {
            aeroGlideViewModel.isRecording.collect { isRecording ->
                if (isRecording) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        beepGenerator = BeepGeneratorImpl()

        setContent {
            AeroGlideTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                val isRecording by aeroGlideViewModel.isRecording.collectAsState()

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
                                    if (isRecording)
                                        aeroGlideViewModel.stopRecording()
                                    else
                                        aeroGlideViewModel.startRecording()
                                }) {
                                    if (isRecording) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.outline_stop_circle_24),
                                            contentDescription = "Mark as favorite"
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.outline_play_arrow_24),
                                            contentDescription = "Mark as favorite"
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    beepGenerator.setFrequency(500f) // Set frequency to 880 Hz
                                    beepGenerator.setDuration(1000L) // Set duration to 2 seconds
                                    beepGenerator.playBeep()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, deviceId: Int) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            Timber.i("PROCESSING LOCATION PERMISSION REQUEST RESULT")
            val permission = (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            if (permission) {
                Timber.i("ACCESS_FINE_LOCATION PERMISSION GRANTED")
                Intent(applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_START
                    startService(this)
                }
                aeroGlideViewModel.startCalibration()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("DESTROY MAIN ACTIVITY")
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            stopService(this)
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }
}
