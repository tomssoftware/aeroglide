package com.alpsfly.aeroglide.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface AppRepository {
    val isRecording: StateFlow<Boolean>
    fun startRecording()
    fun stopRecording()
}

@Singleton
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppRepository {

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording
    override fun startRecording() {
        _isRecording.value = true
    }

    override fun stopRecording() {
        _isRecording.value = false
    }
}