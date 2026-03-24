package com.alpsfly.aeroglide.core.domain.usecase.state

import com.tinder.StateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppState {
    data object Idle : AppState()
    data object Calibrating : AppState()
    data class Ready(
        val autostart: Boolean = false,
        /** ID der zuletzt abgeschlossenen Aufnahme – gesetzt beim Übergang Recording → Ready. */
        val previousActivityId: Long? = null
    ) : AppState()
    data class Recording(
        val activityId: Long,
        /**
         * Mirrors the [Ready.autostart] flag that was active when recording began.
         * Carried through the Recording phase so that [Ready.autostart] can be restored
         * on every Recording → Ready transition (manual stop, auto-landing, etc.) without
         * losing the user's autostart preference.
         */
        val autostart: Boolean = false,
    ) : AppState()
}

@Singleton
class AppStateManager @Inject constructor() {

    private val _appState = MutableStateFlow<AppState>(AppState.Idle)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val stateMachine = createStateMachine()

    // --- Public Commands are unchanged ---
    fun toggleRecording(recordId: Long) = stateMachine.transition(Event.OnToggleRecording(recordId))
    fun onCalibrationStarted() = stateMachine.transition(Event.OnCalibrationStarted)
    fun onCalibrationFinished() = stateMachine.transition(Event.OnCalibrationFinished)

    fun onAutoStartEnabled(enable: Boolean) {
        if (enable)
            stateMachine.transition(Event.OnAutoStartEnabled)
        else
            stateMachine.transition(Event.OnAutoStartDisabled)
    }

    fun permissionRequest() = stateMachine.transition(Event.OnPermissionRequest)
    fun permissionGranted() = stateMachine.transition(Event.OnPermissionGranted)
    fun permissionDenied() = stateMachine.transition(Event.OnPermissionDenied)

    // --- Private Helpers ---

    private fun createStateMachine(): StateMachine<AppState, Event, Unit> {
        return StateMachine.create {
            initialState(AppState.Idle)

            // The onTransition block is correct. It logs and updates the public state.
            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
                _appState.value = validTransition.toState
                Timber.d("State Transition: ${validTransition.fromState::class.simpleName} -> ${validTransition.toState::class.simpleName}")
            }

            state<AppState.Idle> {
                on<Event.OnPermissionRequest> { transitionTo(AppState.Idle) }
                on<Event.OnPermissionGranted> { transitionTo(AppState.Calibrating) }
                on<Event.OnPermissionDenied>  { transitionTo(AppState.Idle) }
            }

            state<AppState.Calibrating> {
                on<Event.OnCalibrationFinished> {
                    transitionTo(AppState.Ready())
                }
            }

            state<AppState.Ready> {
                on<Event.OnToggleRecording> { event ->
                    // `this` ist AppState.Ready → autostart direkt verfügbar, kein Cast nötig.
                    // Das Flag wird in Recording mitgeführt und bei Recording → Ready zurückgespiegelt.
                    transitionTo(AppState.Recording(activityId = event.recordId, autostart = this.autostart))
                }
                on<Event.OnCalibrationStarted> {
                    transitionTo(AppState.Calibrating)
                }
                on<Event.OnAutoStartEnabled> {
                    transitionTo(AppState.Ready(autostart = true))
                }
                on<Event.OnAutoStartDisabled> {
                    transitionTo(AppState.Ready(autostart = false))
                }
            }

            state<AppState.Recording> {
                on<Event.OnToggleRecording> {
                    // `this` ist AppState.Recording → activityId und autostart direkt verfügbar.
                    // autostart wird zurückgespiegelt, damit der Ready-State das Flag korrekt trägt
                    // und AutoStart nach der Landung weiterhin aktiv bleibt.
                    transitionTo(AppState.Ready(autostart = this.autostart, previousActivityId = this.activityId))
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
