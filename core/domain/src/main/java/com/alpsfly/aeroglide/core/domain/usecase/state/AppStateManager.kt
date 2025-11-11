package com.alpsfly.aeroglide.core.domain.usecase.state

import com.tinder.StateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// You can move AppState here to be owned by the state manager
sealed class AppState {
    data object Idle : AppState()
    data object Calibrating : AppState()
    data object Ready : AppState()
    data object AutoStart : AppState()
    data object Recording : AppState()
}

@Singleton
class AppStateManager @Inject constructor() {
    private val _appState = MutableStateFlow<AppState>(AppState.Idle)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val stateMachine = createStateMachine()

    // --- Public Commands to change state ---
    fun onToggleRecording() = stateMachine.transition(Event.OnToggleRecording)
    fun onCalibrationStarted() = stateMachine.transition(Event.OnCalibrationStarted)
    fun onCalibrationFinished() = stateMachine.transition(Event.OnCalibrationFinished)
    fun onAutoStartEnabled(enable: Boolean) {
        if (enable) stateMachine.transition(Event.OnAutoStartEnabled)
        else stateMachine.transition(Event.OnAutoStartDisabled)
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
                    transitionTo(
                        recordingReturnState,
                        SideEffect.StopRecording(recordingReturnState)
                    )
                }
            }

            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
                when (val sideEffect = validTransition.sideEffect) {
                    SideEffect.CalibrationStarted -> {
                        Timber.i("SIDE EFFECT: ${sideEffect.toString().uppercase()}")
                        _appState.value = AppState.Calibrating
                    }

                    SideEffect.CalibrationFinished -> {
                        Timber.i("SIDE EFFECT: ${sideEffect.toString().uppercase()}")
                        _appState.value = AppState.Ready
                    }

                    SideEffect.StartRecording -> {
                        Timber.i("SIDE EFFECT: ${sideEffect.toString().uppercase()}")
                        _appState.value = AppState.Recording
                    }

                    is SideEffect.StopRecording -> {
                        Timber.i("SIDE EFFECT: ${sideEffect.toString().uppercase()}")
                        _appState.value = sideEffect.state
                    }

                    SideEffect.AutoStartEnabled -> {
                        Timber.i("SIDE EFFECT: ${sideEffect.toString().uppercase()}")
                        _appState.value = AppState.AutoStart
                    }

                    SideEffect.AutoStartDisabled -> {
                        Timber.i("SIDE EFFECT: ${sideEffect.toString().uppercase()}")
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
