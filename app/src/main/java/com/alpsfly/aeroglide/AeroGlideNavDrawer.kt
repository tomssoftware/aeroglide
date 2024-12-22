package com.alpsfly.aeroglide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.alpsfly.aeroglide.ui.screen.Screen

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
                        navController.navigate(route = Screen.Screen1.route)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item2") },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigate(route = Screen.Screen2.route)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item3") },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigate(route = Screen.Screen3.route)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Drawer Item4") },
                    selected = false,
                    onClick = {
                        onClick()
                        navController.navigate(route = Screen.DetailScreen.route)
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