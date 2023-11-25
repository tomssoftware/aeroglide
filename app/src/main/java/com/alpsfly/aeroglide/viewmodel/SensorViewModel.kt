package com.alpsfly.aeroglide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.data.repository.SensorRepository
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository
) : ViewModel() {
    val accelDataSource = sensorRepository.accelDataSource.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val queue = ArrayDeque<FloatEntry>()
    val chartEntryModelProducer = ChartEntryModelProducer(queue)
    val avgPressureFlow = sensorRepository.avgPressureFlow.map {
        if (queue.size == 10) {
            queue.removeFirst()
        }
        queue.addLast(FloatEntry(queue.size.toFloat(), it))
        Timber.d("Size ${queue.size}")
        chartEntryModelProducer.setEntries(queue)
        it
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )
}