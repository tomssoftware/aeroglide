package com.alpsfly.aeroglide.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface AppRepository {
    val activityId: StateFlow<Long>
    fun setActivityId(activityId: Long)
}

@Singleton
class AppRepositoryImpl @Inject constructor(

) : AppRepository {
    // Instantiate the state machine
    private val _activityId = MutableStateFlow(0L)
    override val activityId = _activityId.asStateFlow()

    override fun setActivityId(activityId: Long) {
        _activityId.value = activityId
    }
}