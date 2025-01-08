package com.alpsfly.aeroglide.data

import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import com.alpsfly.aeroglide.core.common.TimeProvider
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.data.SensorRepositoryImpl
import org.junit.Before
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SensorRepositoryUnitTest {

    private lateinit var sensorRepository: SensorRepository
    private val mockSensorManager = mock<SensorManager>()
    private val mockLocationManager = mock<LocationManager>()
    private val mockTimeProvider = mock<TimeProvider>()
    private val mockContext = mock<Context>()

    @Before
    fun setup() {
        `when`(mockTimeProvider.nanoTime()).thenReturn(1000000000L)
        sensorRepository = SensorRepositoryImpl(context = mockContext, mockSensorManager, mockLocationManager, mockTimeProvider)
    }
}

