package de.tomssoftware.aeroglide.feature.mapmanager

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tomssoftware.aeroglide.core.ui.R

data class DownloadItem(
    val id: String,
    val region: String,
    val size: String,
    val stateIconRes: Int = R.drawable.settings_24px,
    val typeIconRes: Int = R.drawable.settings_24px
)

@Composable
fun MapManagerScreen(navController: NavController) {
    val dummyItems = listOf(
        DownloadItem(
            id = "1",
            region = "Alps",
            size = "1.2 GB",
            stateIconRes = R.drawable.settings_24px,
            typeIconRes = R.drawable.settings_24px
        ),
        DownloadItem(
            id = "2",
            region = "Pyrenees",
            size = "800 MB",
            stateIconRes = R.drawable.settings_24px,
            typeIconRes = R.drawable.settings_24px
        ),
        DownloadItem(
            id = "3",
            region = "Dolomites",
            size = "500 MB",
            stateIconRes = R.drawable.settings_24px,
            typeIconRes = R.drawable.settings_24px
        )
    )
    DownloadScreen(items = dummyItems)
}

@Composable
fun DownloadScreen(items: List<DownloadItem>, onItemClick: (DownloadItem) -> Unit = {}, onDelete: (DownloadItem) -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // List of download cards
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    DownloadCard(item = item, onClick = { onItemClick(item) }, onDelete = { onDelete(item) })
                }
            }

            // Bottom navigation bar
            BottomNavBar()
        }
    }
}

@Composable
fun DownloadCard(item: DownloadItem, onClick: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {

            Image(
                painter = painterResource(id = item.typeIconRes),
                contentDescription = null,
                modifier = Modifier.height(56.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.Start) {
                    Text(text = "Region:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 8.dp))
                    Text(text = item.region, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.padding(top = 4.dp)) {
                    Text(text = "Size:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 8.dp))
                    Text(text = item.size, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Delete icon
            Icon(
                painter = painterResource(id = R.drawable.settings_24px), contentDescription = "Delete", modifier = Modifier
                    .clickable { onDelete() }
                    .padding(8.dp))

            // State icon
            Image(
                painter = painterResource(id = item.stateIconRes),
                contentDescription = null,
                modifier = Modifier.height(56.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun BottomNavBar(selectedIndex: Int = 0, onSelect: (Int) -> Unit = {}) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        NavigationBarItem(selected = selectedIndex == 0, onClick = { onSelect(0) }, icon = {
            Icon(imageVector = ImageVector.vectorResource(id = R.drawable.settings_24px), contentDescription = "Maps")
        }, label = { Text(text = "Maps") })

        NavigationBarItem(selected = selectedIndex == 1, onClick = { onSelect(1) }, icon = {
            Icon(imageVector = ImageVector.vectorResource(id = R.drawable.settings_24px), contentDescription = "Thermals")
        }, label = { Text(text = "Thermals") })

        NavigationBarItem(selected = selectedIndex == 2, onClick = { onSelect(2) }, icon = {
            Icon(imageVector = ImageVector.vectorResource(id = R.drawable.settings_24px), contentDescription = "Heightmodell")
        }, label = { Text(text = "Heightmodell") })
    }
}
