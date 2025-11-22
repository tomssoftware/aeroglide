package com.alpsfly.aeroglide.feature.livetracking


import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AltitudeProfileViewModel @Inject constructor(
    appStateManager: AppStateManager,
    sensorRepository: SensorRepository,
    private val dataRepository: DataRepository
) : ChartProfileViewModel<Altitude>(
    appStateManager,
    sensorRepository.altitudeFlowUi,
    { it.altitude }
) {
    override suspend fun loadHistoricData(activityId: Long): Flow<List<Pair<Long, Float>>> {
        val activity = dataRepository.getActivity(activityId)
        val begin = activity?.begin ?: 0L
        val end = activity?.end ?: 0L
        return dataRepository.getAltitudesBetween(begin, end).map { altitudes ->
            val startTime = altitudes.firstOrNull()?.timestamp ?: 0L
            altitudes.map {
                val timeDeltaSeconds = (it.timestamp - startTime) / 1000
                timeDeltaSeconds to it.altitude
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
