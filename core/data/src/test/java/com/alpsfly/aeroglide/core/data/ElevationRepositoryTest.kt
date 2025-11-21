package com.alpsfly.aeroglide.core.data

import android.content.Context
import com.alpsfly.aeroglide.core.data.elevation.ElevationTile
import com.alpsfly.aeroglide.core.data.elevation.ElevationTileReader
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

class ElevationRepositoryTest {

    // 1. Temporary Folder Rule
    // This creates a real temporary folder on your PC that is deleted after tests.
    // We use this to simulate the Android `filesDir`.
    @get:Rule
    val tempFolder = TemporaryFolder()

    // Mocks
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private val mockContext: Context = mock()
    private val mockTileReader: ElevationTileReader = mock()

    // Class under test
    private lateinit var repository: ElevationRepositoryImpl

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start() // Start the fake server

        okHttpClient = OkHttpClient.Builder().build()

        // 2. Configure the Context mock
        // When the repository asks for filesDir, return our temp folder.
        whenever(mockContext.filesDir).thenReturn(tempFolder.root)

        repository = ElevationRepositoryImpl(context = mockContext, tileReader = mockTileReader, okHttpClient = okHttpClient)
    }

    @Test
    fun `getTerrainElevation returns null if invalid coordinates`() = runTest {
        val result = repository.getTerrainElevation(91.0, 0.0, DemQuality.HIGH)
        assertNull(result)
    }

    @Test
    fun `getTerrainElevation uses cached tile if available`() = runTest {
        // Arrange
        val lat = 47.5
        val lon = 9.5
        val tileName = "N47E009"
        val expectedElevation: Short = 1234

        // Create a fake tile
        val mockTile: ElevationTile = mock {
            on { computeElevation(lat, lon) } doReturn expectedElevation
        }

        // Simulate the file existing on "disk"
        val demDir = File(tempFolder.root, "dem/dem1")
        demDir.mkdirs()
        val hgtFile = File(demDir, "$tileName.hgt")
        hgtFile.createNewFile() // Create an empty dummy file

        // Tell the reader to return our mock tile when this file is read
        whenever(mockTileReader.read(tileName, hgtFile.absolutePath)).thenReturn(mockTile)

        // Act
        val result = repository.getTerrainElevation(lat, lon, DemQuality.HIGH)

        // Assert
        assertEquals(expectedElevation, result)
    }
}
