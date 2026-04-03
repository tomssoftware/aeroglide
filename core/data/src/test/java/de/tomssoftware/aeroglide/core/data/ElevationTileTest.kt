package de.tomssoftware.aeroglide.core.data

import de.tomssoftware.aeroglide.core.data.elevation.ElevationTile
import org.junit.Assert
import org.junit.Test

class ElevationTileTest {

    /**
     * Creates a simple 3x3 test tile for predictable calculations.
     * The tile data represents the following grid of altitudes:
     *
     * (lat 48.0)  10m | 20m | 30m
     *             ---|-----|---
     * (lat 47.5)  40m | 50m | 60m
     *             ---|-----|---
     * (lat 47.0)  70m | 80m | 90m
     *             ---|-----|---
     *    (lon 9.0) (lon 9.5) (lon 10.0)
     */
    private fun createTestTile(): ElevationTile {
        val tileName = "N47E009" // Bottom-left corner is 47N, 9E
        val tileData = shortArrayOf(
            10, 20, 30, // Top row
            40, 50, 60, // Middle row
            70, 80, 90  // Bottom row
        )
        return ElevationTile(tileName, tileData)
    }

    /**
     * Creates a simple 3x3 test tile for negative coordinates.
     * The tile data represents the following grid of altitudes:
     *
     * (lat -46.0) 10m | 20m | 30m
     *             ---|-----|---
     * (lat -46.5) 40m | 50m | 60m
     *             ---|-----|---
     * (lat -47.0) 70m | 80m | 90m
     *             ---|-----|---
     *    (lon -9.0) (lon -8.5) (lon -8.0)
     */
    private fun createNegativeTestTile(): ElevationTile {
        val tileName = "S47W009" // Bottom-left corner is 47S, 9W
        val tileData = shortArrayOf(
            10, 20, 30,
            40, 50, 60,
            70, 80, 90
        )
        return ElevationTile(tileName, tileData)
    }

    @Test
    fun `computeElevation should return correct elevation for exact corner`() {
        val tile = createTestTile()
        // Test the bottom-left corner (should be 70m)
        val elevation = tile.computeElevation(latitude = 47.0, longitude = 9.0)
        Assert.assertEquals("Elevation at the exact bottom-left corner should be 70", 70, elevation?.toInt())
    }

    @Test
    fun `computeElevation should return correct elevation for top-right corner`() {
        val tile = createTestTile()
        // Test the top-right corner (should be 30m)
        val elevation = tile.computeElevation(latitude = 48.0, longitude = 10.0)
        Assert.assertEquals("Elevation at the exact top-right corner should be 30", 30, elevation?.toInt())
    }

    @Test
    fun `computeElevation should return correct elevation for center`() {
        val tile = createTestTile()
        // Test the exact center (should be 50m)
        val elevation = tile.computeElevation(latitude = 47.5, longitude = 9.5)
        Assert.assertEquals("Elevation at the exact center should be 50", 50, elevation?.toInt())
    }

    @Test
    fun `computeElevation should return correct elevation for negative tile top right`() {
        val tile = createNegativeTestTile()
        // Test the top-right corner of the negative tile (S46.0, W8.0)
        // Based on the grid in createNegativeTestTile, top-right is 30m
        val elevation = tile.computeElevation(latitude = -46.0, longitude = -8.0)
        Assert.assertEquals("Elevation at the top-right corner (negative coords) should be 30", 30, elevation?.toInt())
    }
}