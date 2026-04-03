package de.tomssoftware.aeroglide.core.data

import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import de.tomssoftware.aeroglide.core.common.TimeProvider
import org.junit.Before
import org.mockito.Mockito.mock

class SensorRepositoryUnitTest {

    private lateinit var sensorRepository: SensorRepository
    private val mockSensorManager = mock<SensorManager>()
    private val mockLocationManager = mock<LocationManager>()
    private val mockTimeProvider = mock<TimeProvider>()
    private val mockContext = mock<Context>()

    @Before
    fun setup() {
    }
}

