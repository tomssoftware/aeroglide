package de.tomssoftware.aeroglide.core.domain.usecase.state

import com.tinder.StateMachine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppState {
    data object Idle : AppState()
    data object Calibrating : AppState()
    data class Ready(
        /** ID der zuletzt abgeschlossenen Aufnahme – gesetzt beim Übergang Recording → Ready. */
        val previousActivityId: Long? = null
    ) : AppState()
    data class Recording(
        val activityId: Long,
    ) : AppState()
}

sealed class AppSideEffect {
    /** Wird ausgelöst wenn die State Machine in [AppState.Calibrating] eintritt. */
    data object CalibrationStarted : AppSideEffect()
    /** Wird ausgelöst beim Übergang [AppState.Calibrating] → [AppState.Ready]. */
    data object CalibrationCompleted : AppSideEffect()
    /** Wird ausgelöst beim Übergang [AppState.Ready] → [AppState.Recording]. */
    data class RecordingStarted(val activityId: Long) : AppSideEffect()
    /** Wird ausgelöst beim Übergang [AppState.Recording] → [AppState.Ready]. */
    data class RecordingStopped(val previousActivityId: Long?) : AppSideEffect()
}

@Singleton
class AppStateManager @Inject constructor() {

    private val _appState = MutableStateFlow<AppState>(AppState.Idle)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<AppSideEffect>(extraBufferCapacity = 8)
    val sideEffects: SharedFlow<AppSideEffect> = _sideEffects.asSharedFlow()

    private val stateMachine = createStateMachine()

    // --- Public Commands ---
    fun toggleRecording(recordId: Long) = stateMachine.transition(Event.OnToggleRecording(recordId))
    fun onCalibrationStarted() = stateMachine.transition(Event.OnCalibrationStarted)
    fun onCalibrationFinished() = stateMachine.transition(Event.OnCalibrationFinished)

    fun permissionRequest() = stateMachine.transition(Event.OnPermissionRequest)
    fun permissionGranted() = stateMachine.transition(Event.OnPermissionGranted)
    fun permissionDenied() = stateMachine.transition(Event.OnPermissionDenied)

    // --- Private Helpers ---

    private fun createStateMachine(): StateMachine<AppState, Event, AppSideEffect> {
        return StateMachine.create {
            initialState(AppState.Idle)

            onTransition {
                val valid = it as? StateMachine.Transition.Valid ?: return@onTransition
                _appState.value = valid.toState
                Timber.i("onTransition: ${valid.fromState::class.simpleName} -> ${valid.toState::class.simpleName}")
                valid.sideEffect?.let { effect -> _sideEffects.tryEmit(effect) }
            }

            state<AppState.Idle> {
                on<Event.OnPermissionRequest> { transitionTo(AppState.Idle) }
                on<Event.OnPermissionGranted> { transitionTo(AppState.Calibrating, AppSideEffect.CalibrationStarted) }
                on<Event.OnPermissionDenied>  { transitionTo(AppState.Idle) }
            }

            state<AppState.Calibrating> {
                on<Event.OnCalibrationFinished> {
                    transitionTo(AppState.Ready(), AppSideEffect.CalibrationCompleted)
                }
            }

            state<AppState.Ready> {
                on<Event.OnToggleRecording> { event ->
                    transitionTo(
                        AppState.Recording(activityId = event.recordId),
                        AppSideEffect.RecordingStarted(event.recordId)
                    )
                }
                on<Event.OnCalibrationStarted> {
                    transitionTo(AppState.Calibrating, AppSideEffect.CalibrationStarted)
                }
            }

            state<AppState.Recording> {
                on<Event.OnToggleRecording> {
                    transitionTo(
                        AppState.Ready(previousActivityId = this.activityId),
                        AppSideEffect.RecordingStopped(this.activityId)
                    )
                }
            }
        }
    }

    // Events are correct as simple signals.
    sealed class Event {
        data class OnToggleRecording(val recordId: Long) : Event()
        data object OnCalibrationFinished : Event()
        data object OnCalibrationStarted : Event()
        data object OnPermissionRequest : Event()
        data object OnPermissionGranted : Event()
        data object OnPermissionDenied : Event()
    }
}
