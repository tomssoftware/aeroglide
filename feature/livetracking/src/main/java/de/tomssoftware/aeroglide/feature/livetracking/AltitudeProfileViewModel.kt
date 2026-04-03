package de.tomssoftware.aeroglide.feature.livetracking

import de.tomssoftware.aeroglide.core.data.DataRepository
import de.tomssoftware.aeroglide.core.data.SensorRepository
import de.tomssoftware.aeroglide.core.domain.usecase.state.AppStateManager
import de.tomssoftware.aeroglide.core.model.hardware.Altitude
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AltitudeProfileViewModel @Inject constructor(
    appStateManager: AppStateManager,
    sensorRepository: SensorRepository,
    dataRepository: DataRepository
) : ChartProfileViewModel<Altitude>(
    appStateManager = appStateManager,
    dataRepository = dataRepository,
    liveDataFlow = sensorRepository.altitudeFlowUi,
    valueExtractor = { it.altitude }
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun loadHistoricData(activityId: Long): Flow<List<Pair<Long, Float>>> {
        // Return a Flow that reacts to the activity.
        return dataRepository.getActivityFlow(activityId).flatMapLatest { activity ->
            if (activity == null) {
                // If activity is null, emit an empty list for the chart
                flowOf(emptyList<Pair<Long, Float>>())
            } else {
                // If activity exists, get the altitudes and map them.
                dataRepository.getTracksBetween(activity.begin, activity.end).map { tracks ->
                    val startTime = tracks.firstOrNull()?.timestamp ?: 0L
                    tracks.map {
                        val timeDeltaSeconds = (it.timestamp - startTime) / 1000
                        timeDeltaSeconds to it.altitude
                    }
                }
            }
        }
    }

    // --- PROVIDE SPECIFIC FORMATTERS ---

    val yAxisLabelFormatter = object : CartesianValueFormatter {
        override fun format(
            context: CartesianMeasuringContext,
            value: Double,
            verticalAxisPosition: Axis.Position.Vertical?
        ): CharSequence {
            return "${value.toInt()} m"
        }
    }

    val xAxisLabelFormatter = object : CartesianValueFormatter {
        override fun format(
            context: CartesianMeasuringContext,
            value: Double,
            verticalAxisPosition: Axis.Position.Vertical?
        ): CharSequence {
            // The 'value' is the total elapsed seconds from the start of the recording.
            val totalSeconds = value.toLong()

            // Calculate hours, minutes, and remaining seconds.
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return when {
                // If duration is one hour or more, format as "hh:mm".
                hours > 0 -> {
                    // Use String.format for easy padding with leading zeros.
                    "%02d:%02d".format(hours, minutes)
                }
                // Otherwise, format as "mm:ss".
                else -> {
                    "%02d:%02d".format(minutes, seconds)
                }
            }
        }
    }
}
