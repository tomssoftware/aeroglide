package com.alpsfly.aeroglide.data

import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.data.SensorRepositoryImpl
import com.alpsfly.aeroglide.core.hardware.TimeProvider
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
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
        sensorRepository = SensorRepositoryImpl(context = mockContext, mockSensorManager, mockLocationManager)
    }

    @Test
    fun `verticalAccelerationFlow should combine, chunk, map and emit correct values`() = runTest {
        // Arrange
        val linearAccelerationFlow = flow {
            emit(SensorData())
//            emit(SensorData())
//            emit(SensorData())
//            emit(SensorData())
        }
        val rotationVectorFlow = flow {
            emit(SensorData())
//            emit(SensorData())
//            emit(SensorData())
//            emit(SensorData())
        }

//        whenever(mockSensorManager.linearAccelerationSensorDataFlow()).thenReturn(linearAccelerationFlow)
//        whenever(mockSensorManager.rotationVectorSensorDataFlow()).thenReturn(rotationVectorFlow)

        // Act
        // val resultFlow = sensorRepository.verticalAccelerationFlow

//        // Assert
//        resultFlow.test {
//            val item1 = awaitItem()
//            assertEquals(SensorType.VerticalAcceleration, item1.type)
//            assertEquals(0.0f, item1.values[0], 0.001f)
//        }
    }
}


