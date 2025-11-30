package com.alpsfly.aeroglide

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.alpsfly.aeroglide.core.common.audio.BeepGeneratorImpl
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.presentation.AeroGlideTopAppBar
import com.alpsfly.aeroglide.core.ui.R
import com.alpsfly.aeroglide.theme.AeroGlideTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AeroGlideActivity : ComponentActivity() {
    private val aeroGlideViewModel: AeroGlideViewModel by viewModels()
    private lateinit var beepGenerator: BeepGeneratorImpl

    @Inject
    lateinit var prefs: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("CREATE MAIN ACTIVITY")

        val permission = (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        if (permission) {
            Timber.i("ACCESS_FINE_LOCATION PERMISSION GRANTED")
            aeroGlideViewModel.onPermissionGranted()
        } else {
            Timber.i("REQUEST ACCESS_FINE_LOCATION PERMISSION")
            aeroGlideViewModel.onPermissionRequest()
            val permissions = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
            requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }

        lifecycle.coroutineScope.launch {
            aeroGlideViewModel.appState.collect { state ->
                if (state is AppState.Recording) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        beepGenerator = BeepGeneratorImpl()

        // Enable edge-to-edge display. This MUST be called before setContent.
        enableEdgeToEdge()

        setContent {
            AeroGlideScreen(
                navController = rememberNavController(),
                aeroGlideViewModel = aeroGlideViewModel
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            Timber.i("PROCESSING LOCATION PERMISSION REQUEST RESULT")
            val permission = (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            if (permission) {
                Timber.i("REQUESTED ACCESS_FINE_LOCATION PERMISSION GRANTED")
                aeroGlideViewModel.onPermissionGranted()
            } else {
                Timber.i("REQUESTED ACCESS_FINE_LOCATION PERMISSION DENIED")
                aeroGlideViewModel.onPermissionDenied()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("DESTROY MAIN ACTIVITY")
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AeroGlideScreen(
    navController: NavController,
    aeroGlideViewModel: AeroGlideViewModel = hiltViewModel()
) {
    val darkTheme = isSystemInDarkTheme()
    AeroGlideTheme(
        darkTheme = darkTheme,
        dynamicColor = false
    ) {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val appState by aeroGlideViewModel.appState.collectAsStateWithLifecycle(initialValue = AppState.Idle())

        Scaffold(
            modifier = Modifier,
            topBar = {
                AeroGlideTopAppBar(
                    title = "AeroGlide",
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
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
                        IconButton(
                            enabled = appState is AppState.Ready || appState is AppState.Recording,
                            onClick = { aeroGlideViewModel.onToggleRecording() }
                        ) {
                            if (appState is AppState.Recording) {
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
                        IconButton(
                            enabled = appState is AppState.Ready,
                            onClick = { aeroGlideViewModel.onReCalibrate() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.replay_24px),
                                contentDescription = "Re-calibrate"
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(id = R.drawable.volume_off_24px),
                                contentDescription = "Volume off"
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
