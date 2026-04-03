package de.tomssoftware.aeroglide.core.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.tomssoftware.aeroglide.core.model.hardware.SensorType

@Entity(tableName = "calibration")
data class Calibration(
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long = 0,
    @ColumnInfo(name = "is_calibrated") var isCalibrated: Boolean = false,
    @ColumnInfo(name = "sensor_type") var sensorType: SensorType = SensorType.Unknown,
    @ColumnInfo(name = "latitude") var latitude: Float = 0f,
    @ColumnInfo(name = "longitude") var longitude: Float = 0f,
    @ColumnInfo(name = "altitude0") var altitude0: Float = 0f,
    @ColumnInfo(name = "pressure0") var pressure0: Float = 0f,
    @ColumnInfo(name = "has_horizontal_accuracy") var hasHorizontalAccuracy: Boolean = false,
    @ColumnInfo(name = "horizontal_accuracy") var horizontalAccuracy: Float = 0f,
    @ColumnInfo(name = "has_vertical_accuracy") var hasVerticalAccuracy: Boolean = false,
    @ColumnInfo(name = "vertical_accuracy") var verticalAccuracy: Float = 0f,
)