package com.alpsfly.aeroglide.core.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_point")
data class TrackPoint(
    @PrimaryKey
    @ColumnInfo(name = "timestamp") var timestamp: Long = 0L,

    // Fields from Location
    @ColumnInfo(name = "latitude") var latitude: Float = 0f,
    @ColumnInfo(name = "longitude") var longitude: Float = 0f,
    @ColumnInfo(name = "gps_altitude") var gpsAltitude: Float = 0f, // Renamed to avoid conflict
    @ColumnInfo(name = "bearing") var bearing: Float = 0f,
    @ColumnInfo(name = "speed") var speed: Float = 0f,
    @ColumnInfo(name = "geoid_correction") var geoidCorrection: Float = 0f,
    @ColumnInfo(name = "has_horizontal_accuracy") var hasHorizontalAccuracy: Boolean = false,
    @ColumnInfo(name = "horizontal_accuracy") var horizontalAccuracy: Float = 0f,
    @ColumnInfo(name = "has_vertical_accuracy") var hasVerticalAccuracy: Boolean = false,
    @ColumnInfo(name = "vertical_accuracy") var verticalAccuracy: Float = 0f,
    @ColumnInfo(name = "bearing_accuracy") var bearingAccuracy: Float = 0f,
    @ColumnInfo(name = "speed_accuracy") var speedAccuracy: Float = 0f,
    @ColumnInfo(name = "provider") var provider: String = "unknown",

    // Field from Altitude
    @ColumnInfo(name = "altitude") var altitude: Float = 0f,

    // Field from Pressure
    @ColumnInfo(name = "pressure") var pressure: Float = 0f,

    // Field from Climbrate
    @ColumnInfo(name = "climbrate") var climbrate: Float = 0f,

    // Field from GlideRatio
    @ColumnInfo(name = "glide_ratio") var glideRatio: Float = 0f,

    // Fields to track the synchronization state with Firebase
    @ColumnInfo(name = "firestore_id") var firestoreId: String? = null,
    @ColumnInfo(name = "sync_state") var syncState: SyncState = SyncState.LOCAL,
    @ColumnInfo(name = "last_synced_at") var lastSyncedAt: Long = 0,
    @ColumnInfo(name = "sync_error") var syncError: String? = null,
)

enum class SyncState {
    LOCAL, // The data is only present locally
    SYNCED, // The data is synchronized with Firebase
    PENDING // The data is waiting to be synchronized
}
