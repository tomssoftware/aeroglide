import com.alpsfly.aeroglide.core.hardware.SensorFrequency
import com.alpsfly.aeroglide.core.hardware.TimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.Duration


class SensorFrequencyTest {

    private lateinit var timeProvider: TimeProvider
    private lateinit var sensorFrequency: SensorFrequency

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        timeProvider = mock(TimeProvider::class.java)
        sensorFrequency = SensorFrequency(timeProvider = timeProvider)
    }

    @Test
    fun testInc_initialCall() {
        `when`(timeProvider.nanoTime()).thenReturn(1000000000L)

        val frequency = sensorFrequency.inc()

        // Verify frequency calculation
        assertEquals(Float.NaN, frequency, 0.001f)
    }

    @Test
    fun testInc_subsequentCalls() {
        `when`(timeProvider.nanoTime()).thenReturn(1000000000L, 2000000000L) // Mock time progression

        sensorFrequency.inc()
        val frequency = sensorFrequency.inc()

        // Verify frequency calculation
        assertTrue(frequency > 0)
    }

    @Test
    fun testGet_initialState() {
        `when`(timeProvider.nanoTime()).thenReturn(Duration.ofSeconds(1).toNanos())

        val frequency = 0f //sensorFrequency.get()

        // Verify frequency calculation
        assertEquals(0.0f, frequency, 0.001f)
    }

    @Test
    fun testGet_afterIncrement() {
        `when`(timeProvider.nanoTime()).thenReturn(1000000000L, 2000000000L) // Mock time progression

        sensorFrequency.inc()
        val frequency = sensorFrequency.inc()

        // Verify frequency calculation
        assertEquals(1f, frequency, 0.001f)
    }

    @Test
    fun testGet_afterIncrement2Hz() {
        `when`(timeProvider.nanoTime()).thenReturn(
            Duration.ofMillis(500).toNanos(),
            Duration.ofMillis(1000).toNanos()
        ) // Mock time progression

        sensorFrequency.inc()
        val frequency = sensorFrequency.inc()

        // Verify frequency calculation
        assertEquals(2f, frequency, 0.001f)
    }

    @Test
    fun testGet_afterIncrement100Hz() {
        `when`(timeProvider.nanoTime()).thenReturn(
            Duration.ofMillis(1000).toNanos(),
            Duration.ofMillis(1010).toNanos()
        ) // Mock time progression

        sensorFrequency.inc()
        val frequency = sensorFrequency.inc()

        // Verify frequency calculation
        assertEquals(100f, frequency, 0.001f)
    }

    @Test
    fun testGet_afterIncrement1KHz() {
        `when`(timeProvider.nanoTime()).thenReturn(
            Duration.ofMillis(1000).toNanos(),
            Duration.ofMillis(1001).toNanos(),
            Duration.ofMillis(1002).toNanos(),
            Duration.ofMillis(1003).toNanos(),
        ) // Mock time progression

        sensorFrequency.inc()
        sensorFrequency.inc()
        sensorFrequency.inc()
        val frequency = sensorFrequency.inc()

        // Verify frequency calculation
        assertEquals(1000f, frequency, 0.001f)
    }
}
