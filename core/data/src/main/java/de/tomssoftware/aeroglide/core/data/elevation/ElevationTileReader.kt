package de.tomssoftware.aeroglide.core.data.elevation

import java.io.File

class ElevationTileReader {
    fun read(tileName: String, fileName: String): ElevationTile {
        /* read the file data */
        val file = File(fileName)
        return if (file.exists()) {
            val byteArray = file.readBytes()
            val tileData = ShortArray(byteArray.size / 2) {
                ((byteArray[(it * 2) + 1].toInt() and 0xFF) or (byteArray[(it * 2)].toInt() shl 8)).toShort()
            }
            ElevationTile(tileName, tileData)
        } else {
            /* return empty short array if hgt file does not exists */
            ElevationTile(tileName, shortArrayOf())
        }
    }
}