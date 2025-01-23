package com.alpsfly.aeroglide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpsfly.aeroglide.core.Screen
import com.alpsfly.aeroglide.feature.activityhistory.navigateToActivityHistoryList

@Composable
fun AeroGlideNavDrawer(navController: NavHostController, drawerState: DrawerState, onClick: () -> Unit) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Drawer title1", modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item1") },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigate(route = Screen.VariometerScreen.route)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item2") },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigate(route = Screen.DeviceStatusScreen.route)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item3") },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigate(route = Screen.DiagnosisScreen.route)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item4") },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigateToActivityHistoryList(0L)
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