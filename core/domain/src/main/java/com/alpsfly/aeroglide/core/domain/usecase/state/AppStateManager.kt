package com.alpsfly.aeroglide.core.domain.usecase.state

import com.tinder.StateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class FromState {
    Initial,
    Idle,
    Calibrating,
    Ready,
    Recording,
    AutoStart
}

// The AppState sealed class is already perfectly defined with its 'fromState' property.
sealed class AppState {
    abstract val fromState: FromState

    data class Idle(override val fromState: FromState = FromState.Initial) : AppState()
    data class Calibrating(override val fromState: FromState) : AppState()
    data class Ready(override val fromState: FromState) : AppState()
    data class AutoStart(override val fromState: FromState) : AppState()
    data class Recording(val activityId: Long, override val fromState: FromState) : AppState()
}

@Singleton
class AppStateManager @Inject constructor() {

    private val _appState = MutableStateFlow<AppState>(AppState.Idle(FromState.Initial))
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val stateMachine = createStateMachine()

    // --- Public Commands are unchanged ---
    fun onToggleRecording(recordId: Long) = stateMachine.transition(Event.OnToggleRecording(recordId))
    fun onCalibrationStarted() = stateMachine.transition(Event.OnCalibrationStarted)
    fun onCalibrationFinished() = stateMachine.transition(Event.OnCalibrationFinished)
    fun onAutoStartEnabled(enable: Boolean) {
        if (enable) stateMachine.transition(Event.OnAutoStartEnabled)
        else stateMachine.transition(Event.OnAutoStartDisabled)
    }

    fun onPermissionRequest() = stateMachine.transition(Event.OnPermissionRequest)
    fun onPermissionGranted() = stateMachine.transition(Event.OnPermissionGranted)
    fun onPermissionDenied() = stateMachine.transition(Event.OnPermissionDenied)

    // --- Private Helpers ---

    private fun createStateMachine(): StateMachine<AppState, Event, Unit> {
        return StateMachine.create {
            initialState(AppState.Idle(FromState.Initial))

            // The onTransition block is correct. It logs and updates the public state.
            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
                _appState.value = validTransition.toState
                Timber.d("State Transition: ${validTransition.fromState::class.simpleName} -> ${validTransition.toState::class.simpleName}")
            }

            state<AppState.Idle> {
                on<Event.OnPermissionRequest> {
                    transitionTo(AppState.Idle(fromState = FromState.Idle))
                }
                on<Event.OnPermissionGranted> {
                    transitionTo(AppState.Calibrating(fromState = FromState.Idle))
                }
                on<Event.OnPermissionDenied> {
                    transitionTo(AppState.Idle(fromState = FromState.Idle))
                }
            }

            state<AppState.Calibrating> {
                on<Event.OnCalibrationFinished> {
                    transitionTo(AppState.Ready(fromState = FromState.Calibrating))
                }
            }

            state<AppState.Ready> {
                on<Event.OnToggleRecording> { event ->
                    transitionTo(AppState.Recording(event.recordId, fromState = FromState.Ready))
                }
                on<Event.OnCalibrationStarted> {
                    transitionTo(AppState.Calibrating(fromState = FromState.Ready))
                }
                on<Event.OnAutoStartEnabled> {
                    transitionTo(AppState.AutoStart(fromState = FromState.Ready))
                }
            }

            state<AppState.AutoStart> {
                on<Event.OnToggleRecording> { event ->
                    transitionTo(AppState.Recording(event.recordId, fromState = FromState.AutoStart))
                }
                on<Event.OnAutoStartDisabled> {
                    transitionTo(AppState.Ready(fromState = FromState.AutoStart))
                }
            }

            state<AppState.Recording> {
                on<Event.OnToggleRecording> {
                    // `this` is the `AppState.Recording` instance, which contains the activityId.
                    // The new `Ready` state now correctly knows its predecessor.
                    transitionTo(AppState.Ready(fromState = FromState.Recording))
                }
            }
        }
    }

    // Events are correct as simple signals.
    sealed class Event {
        data class OnToggleRecording(val recordId: Long) : Event()
        data object OnCalibrationFinished : Event()
        data object OnCalibrationStarted : Event()
        data object OnAutoStartEnabled : Event()
        data object OnAutoStartDisabled : Event()
        data object OnPermissionRequest : Event()
        data object OnPermissionGranted : Event()
        data object OnPermissionDenied : Event()
    }
}
