package com.alpsfly.aeroglide.core.data

import android.content.Context
import com.tinder.StateMachine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppState {
    data object Idle : AppState()
    data object Calibrating : AppState()
    data object Ready : AppState()
    data object AutoStart : AppState()
    data object Recording : AppState()
}

interface AppRepository {
    val activityId: StateFlow<Long>
    val appState: StateFlow<AppState>
    fun setActivityId(activityId: Long)
    var onToggleRecording: () -> Unit
    fun doEnableAutoStart(enabled: Boolean)
    fun doStartCalibration()
    fun doStopCalibration()
}

@Singleton
class AppRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,

    ) : AppRepository {
    // Instantiate the state machine
    private val stateMachine = createStateMachine()
    private val _activityId = MutableStateFlow(0L)
    override val activityId = _activityId.asStateFlow()
    private val _appState: MutableStateFlow<AppState> = MutableStateFlow(AppState.Idle)
    override val appState = _appState.asStateFlow()

    override var onToggleRecording: () -> Unit = {
        stateMachine.transition(Event.OnToggleRecording)
    }

    override fun doStartCalibration() {
        stateMachine.transition(Event.OnCalibrationStarted)
    }

    override fun doStopCalibration() {
        stateMachine.transition(Event.OnCalibrationFinished)
    }

    override fun doEnableAutoStart(enabled: Boolean) {
        if (enabled) {
            stateMachine.transition(Event.OnAutoStartEnabled)
        } else {
            stateMachine.transition(Event.OnAutoStartDisabled)
        }
    }

    override fun setActivityId(activityId: Long) {
        _activityId.value = activityId
    }

    /* application state machine */
    private fun createStateMachine(): StateMachine<AppState, Event, SideEffect> {
        return StateMachine.create {
            lateinit var recordingReturnState: AppState
            initialState(AppState.Idle)

            state<AppState.Idle> {
                on<Event.OnCalibrationStarted> {
                    transitionTo(AppState.Calibrating, SideEffect.CalibrationStarted)
                }
            }

            state<AppState.Calibrating> {
                on<Event.OnCalibrationFinished> {
                    transitionTo(AppState.Ready, SideEffect.CalibrationFinished)
                }
            }

            state<AppState.Ready> {
                on<Event.OnToggleRecording> {
                    recordingReturnState = AppState.Ready
                    transitionTo(AppState.Recording, SideEffect.StartRecording)
                }

                on<Event.OnAutoStartEnabled> {
                    transitionTo(AppState.AutoStart, SideEffect.AutoStartEnabled)
                }
            }

            state<AppState.AutoStart> {
                on<Event.OnToggleRecording> {
                    recordingReturnState = AppState.AutoStart
                    transitionTo(AppState.Recording, SideEffect.StartRecording)
                }

                on<Event.OnAutoStartDisabled> {
                    transitionTo(AppState.Ready, SideEffect.AutoStartDisabled)
                }
            }

            state<AppState.Recording> {
                on<Event.OnToggleRecording> {
                    transitionTo(recordingReturnState, SideEffect.StopRecording(recordingReturnState))
                }
            }

            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
                when (val sideEffect = validTransition.sideEffect) {
                    SideEffect.CalibrationStarted -> {
                        _appState.value = AppState.Calibrating
                    }

                    SideEffect.CalibrationFinished -> {
                        _appState.value = AppState.Ready
                    }

                    SideEffect.StartRecording -> {
                        _appState.value = AppState.Recording
                    }

                    is SideEffect.StopRecording -> {
                        _appState.value = sideEffect.state
                    }

                    SideEffect.AutoStartEnabled -> {
                        _appState.value = AppState.AutoStart
                    }

                    SideEffect.AutoStartDisabled -> {
                        _appState.value = AppState.Ready
                    }

                    null -> {
                        Timber.d("No side effect implemented")
                    }
                }
            }
        }
    }

    companion object {

        sealed class Event {
            data object OnToggleRecording : Event()
            data object OnCalibrationFinished : Event()
            data object OnCalibrationStarted : Event()
            data object OnAutoStartEnabled : Event()
            data object OnAutoStartDisabled : Event()
        }

        sealed class SideEffect {
            data object CalibrationStarted : SideEffect()
            data object CalibrationFinished : SideEffect()
            data object StartRecording : SideEffect()
            data class StopRecording(val state: AppState) : SideEffect()
            data object AutoStartEnabled : SideEffect()
            data object AutoStartDisabled : SideEffect()
        }
    }
}