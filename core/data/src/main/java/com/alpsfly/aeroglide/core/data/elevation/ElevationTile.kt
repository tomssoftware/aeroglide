package com.alpsfly.aeroglide.core.data.elevation

import com.alpsfly.aeroglide.core.data.tile.Tile
import timber.log.Timber
import kotlin.math.sqrt

class ElevationTile(
    val tileName: String,
    private val data: ShortArray
) : Tile() {
    private val samplesPerSide: Int
    private val latOrigin: Int
    private val lonOrigin: Int

    init {
        require(tileName.matches(Regex("[NS]\\d{2}[EW]\\d{3}"))) {
            "Invalid tile name format: $tileName"
        }

        latOrigin = parseLat(tileName.substring(0, 3))
        lonOrigin = parseLon(tileName.substring(3))

        samplesPerSide = sqrt(data.size.toDouble()).toInt()
        require(samplesPerSide * samplesPerSide == data.size) {
            "Data size must be a square grid"
        }
    }

    fun computeElevation(latitude: Double, longitude: Double): Short? {
        if (latitude !in latOrigin.toDouble()..(latOrigin + 1.0) ||
            longitude !in lonOrigin.toDouble()..(lonOrigin + 1.0)
        ) {
            return null
        }

        val latRel = (latitude - latOrigin) * (samplesPerSide - 1)
        val lonRel = (longitude - lonOrigin) * (samplesPerSide - 1)

        val row = (samplesPerSide - 1 - latRel).toInt().coerceIn(0, samplesPerSide - 1)
        val col = lonRel.toInt().coerceIn(0, samplesPerSide - 1)

        val index = row * samplesPerSide + col
        Timber.d("Elevation at $latitude, $longitude is ${data.getOrNull(index)}")
        return data.getOrNull(index)
    }

    fun computeElevationBilinear(latitude: Double, longitude: Double): Short? {
        return 0
    }

    private fun parseLat(s: String): Int {
        val sign = if (s[0] == 'N') 1 else -1
        return sign * s.substring(1).toInt()
    }

    private fun parseLon(s: String): Int {
        val sign = if (s[0] == 'E') 1 else -1
        return sign * s.substring(1).toInt()
    }
}