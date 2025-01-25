package com.alpsfly.aeroglide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpsfly.aeroglide.core.Screen
import com.alpsfly.aeroglide.feature.activityhistory.navigateToActivityHistoryList
import com.alpsfly.aeroglide.core.ui.R as uiR


import androidx.compose.ui.res.painterResource

@Composable
fun AeroGlideNavDrawer(navController: NavHostController, drawerState: DrawerState, onClick: () -> Unit) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(stringResource(R.string.app_name), modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = {
                        Text(text = stringResource(uiR.string.sid_vario))
                    },
                    icon = {
                        Icon(painter = painterResource(id = uiR.drawable.swap_vertical_circle_24px), contentDescription = null)
                    },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigate(route = Screen.VariometerScreen.route)
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Text(text = stringResource(uiR.string.sid_sensor_status))
                    },
                    icon = {
                        Icon(painter = painterResource(id = uiR.drawable.sensors_24px), contentDescription = null)
                    },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigate(route = Screen.DeviceStatusScreen.route)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = stringResource(uiR.string.sid_flights)) },
                    icon = {
                        Icon(painter = painterResource(id = uiR.drawable.list_alt_24px), contentDescription = null)
                    },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigateToActivityHistoryList(0L)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Debug Screen") },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigate(route = Screen.DiagnosisScreen.route)
                    }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AeroGlideNavGraph(navController = navController)
        }
    }
}