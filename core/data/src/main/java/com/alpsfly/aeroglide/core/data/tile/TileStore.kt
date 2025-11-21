package com.alpsfly.aeroglide.core.data.tile

class TileStore<T : Tile> {

    private val tilesMap = mutableMapOf<String, T>()

    val tiles: Map<String, T>
        get() = tilesMap

    fun addTile(key: String, tile: T) {
        tilesMap[key] = tile
    }

    fun getTile(key: String): T? {
        if (tilesMap[key] != null) {
            return tilesMap[key]
        }
        return null
    }

    fun isTileNull(key: String): Boolean {
        return (tilesMap[key] == null)
    }
}
