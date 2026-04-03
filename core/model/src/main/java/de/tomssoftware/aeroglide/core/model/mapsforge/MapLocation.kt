package de.tomssoftware.aeroglide.core.model.mapsforge

data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Float,
    val climbrate: Float,
    val grade: Float
)