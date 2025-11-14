package com.alpsfly.aeroglide.core.domain.usecase.state

import com.tinder.StateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// The AppState sealed class is already perfectly defined with its 'fromState' property.
sealed class AppState {
    abstract val fromState: AppState?

    data class Idle(override val fromState: AppState? = null) : AppState()
    data class Calibrating(override val fromState: AppState) : AppState()
    data class Ready(override val fromState: AppState) : AppState()
    data class AutoStart(override val fromState: AppState) : AppState()
    data class Recording(val activityId: Long, override val fromState: AppState) : AppState()
}

@Singleton
class AppStateManager @Inject constructor() {

    private val _appState = MutableStateFlow<AppState>(AppState.Idle(null))
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

    private fun createStateMachine(): StateMachine<AppState, Event, Unit> {
        return StateMachine.create {
            initialState(AppState.Idle(null))

            // The onTransition block is correct. It logs and updates the public state.
            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
                _appState.value = validTransition.toState
                Timber.d("State Transition: ${validTransition.fromState::class.simpleName} -> ${validTransition.toState::class.simpleName}")
            }

            state<AppState.Idle> {
                on<Event.OnCalibrationStarted> {
                    // `this` inside a `state<Type>` block refers to the instance of `Type`.
                    transitionTo(AppState.Calibrating(fromState = this))
                }
            }

            state<AppState.Calibrating> {
                on<Event.OnCalibrationFinished> {
                    // `this` is the `AppState.Calibrating` instance we are transitioning FROM.
                    transitionTo(AppState.Ready(fromState = this))
                }
            }

            state<AppState.Ready> {
                on<Event.OnToggleRecording> { event ->
                    transitionTo(AppState.Recording(event.recordId, fromState = this))
                }
                on<Event.OnAutoStartEnabled> {
                    transitionTo(AppState.AutoStart(fromState = this))
                }
            }

            state<AppState.AutoStart> {
                on<Event.OnToggleRecording> { event ->
                    transitionTo(AppState.Recording(event.recordId, fromState = this))
                }
                on<Event.OnAutoStartDisabled> {
                    transitionTo(AppState.Ready(fromState = this))
                }
            }

            state<AppState.Recording> {
                on<Event.OnToggleRecording> {
                    // `this` is the `AppState.Recording` instance, which contains the activityId.
                    // The new `Ready` state now correctly knows its predecessor.
                    transitionTo(AppState.Ready(fromState = this))
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
    }
}
