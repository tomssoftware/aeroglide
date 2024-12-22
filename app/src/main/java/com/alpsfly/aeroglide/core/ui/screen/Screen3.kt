package com.alpsfly.aeroglide.core.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.viewmodel.SensorViewModel

@Composable
fun Screen3(
    navController: NavController,
    sensorViewModel: SensorViewModel = hiltViewModel()
) {
    val data = listOf(
        Triple("Altitude", "1234", "m"),
        Triple("Speed", "567", "km/h"),
        Triple("Temperature", "25", "°C"),
        Triple("Pressure", "1013", "hPa"),
        Triple("Humidity", "60", "%"),
        Triple("Wind", "15", "m/s")
    )
    DataGrid(data = data, modifier = Modifier)
}

@Composable
fun DataGrid(
    data: List<Triple<String, String, String>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        data.chunked(3).forEach { rowData ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowData.forEach { (caption, value, unit) ->
                    DataCell(
                        caption = caption,
                        value = value,
                        unit = unit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DataCell(
    caption: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = caption,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = unit,
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.Gray
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDataGrid() {
    val data = listOf(
        Triple("Altitude", "1234", "m"),
        Triple("Speed", "567", "km/h"),
        Triple("Temperature", "25", "°C"),
        Triple("Pressure", "1013", "hPa"),
        Triple("Humidity", "60", "%"),
        Triple("Wind", "15", "m/s")
    )
    DataGrid(data = data, modifier = Modifier)
}