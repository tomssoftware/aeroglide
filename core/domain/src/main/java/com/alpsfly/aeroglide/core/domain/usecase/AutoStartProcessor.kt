package com.alpsfly.aeroglide.core.domain.usecase

import androidx.annotation.VisibleForTesting
import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.AutoStartSettingsProvider
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.tinder.StateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Singleton
class AutoStartProcessor @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val settingsProvider: AutoStartSettingsProvider,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) {
    private val autoStartDetector = AutoStartDetector()
    private var collectorJob: Job? = null

    // Callback properties for the UseCase to implement
    var onTakeOffDetected: () -> Unit = {}
    var onLandingDetected: () -> Unit = {}

    init {
        // The processor now just calls the simple callbacks. It has no knowledge of app state.
        autoStartDetector.onTakeOff = {
            Timber.i("AutoStartProcessor: Take-off detected, invoking callback.")
            onTakeOffDetected()
        }
        autoStartDetector.onLanded = {
            Timber.i("AutoStartProcessor: Landing detected, invoking callback.")
            onLandingDetected()
        }
    }

    fun start() {
        if (collectorJob?.isActive == true) return
        Timber.i("AutoStartProcessor: Starting.")

        // The processor listens to setting changes and updates the detector.
        settingsProvider.velocityLimitTakeOff
            .onEach { autoStartDetector.velocityFlying = it }
            .launchIn(applicationScope)

        settingsProvider.velocityLimitLanding
            .onEach { autoStartDetector.velocityLanded = it }
            .launchIn(applicationScope)

        // The processor collects sensor data and feeds it to the detector.
        collectorJob = combine(
            sensorRepository.locationFlowUi,
            sensorRepository.climbrateFlowUi
        ) { location, climbrate ->
            autoStartDetector.velocity = location.speed
            autoStartDetector.climbrate = climbrate.climbrate
            autoStartDetector.detect()
        }.launchIn(applicationScope)
    }

    fun stop() {
        if (collectorJob?.isActive != true) return
        Timber.i("AutoStartProcessor: Stopping.")
        collectorJob?.cancel()
        collectorJob = null
        autoStartDetector.reset()
    }
}

class AutoStartDetector(
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {

    // Input
    var climbrate: Float = Float.MIN_VALUE
    var velocity: Float = Float.MIN_VALUE

    // Callbacks
    lateinit var onTakeOff: () -> Unit
    lateinit var onLanded: () -> Unit

    // Limits
    var velocityFlying = 2 * 1.38f // 2 * 5 km/h
    var velocityLanded = 2 * 1.38f // 2 * 5 km/h
    val climbrateTakeOff = 0.5f // ms
    val climbrateLanding = -0.5f // ms

    private fun isTakeOffCondition() = velocity >= velocityFlying && climbrate >= climbrateTakeOff
    private fun isFlyingCondition() = velocity >= velocityFlying
    private fun isLandingCondition() = velocity >= velocityFlying && climbrate <= climbrateLanding
    private fun isLandedCondition() =
        velocity <= velocityLanded && (climbrate in climbrateLanding..climbrateTakeOff)

    // Time-based Durations for Guards
    @VisibleForTesting
    internal val takeOffDuration = 5.seconds

    @VisibleForTesting
    internal val flyingDuration = 3.seconds

    @VisibleForTesting
    internal val landingDuration = 3.seconds

    @VisibleForTesting
    internal val landedDuration = 5.seconds

    @VisibleForTesting
    internal val resetDuration = 30.seconds

    // These will accumulate time when conditions are met
    private var timeInTakeOffCondition = Duration.ZERO
    private var timeInFlyingCondition = Duration.ZERO
    private var timeInLandingCondition = Duration.ZERO
    private var timeInLandedCondition = Duration.ZERO
    private var timeInResetCondition = Duration.ZERO

    private fun doTakeOff() = timeInTakeOffCondition >= takeOffDuration
    private fun doFlying() = timeInFlyingCondition >= flyingDuration
    private fun doLanding() = timeInLandingCondition >= landingDuration
    private fun doLanded() = timeInLandedCondition >= landedDuration
    private fun doReset() = timeInResetCondition >= resetDuration

    private var lastUpdateTime: Long = 0

    fun detect() {
        if (climbrate == Float.MIN_VALUE || velocity == Float.MIN_VALUE) {
            return // Not enough data yet
        }

        val currentTime = timeProvider()
        // If this is the first update, just set the time and exit
        if (lastUpdateTime == 0L) {
            lastUpdateTime = currentTime
            return
        }

        // Calculate elapsed time in seconds since last detection
        val elapsedTime = (currentTime - lastUpdateTime).milliseconds
        lastUpdateTime = currentTime

        stateMachine.transition(Event.OnUpdate(elapsedTime))
    }

    fun reset() {
        timeInTakeOffCondition = Duration.ZERO
        timeInFlyingCondition = Duration.ZERO
        timeInLandingCondition = Duration.ZERO
        timeInLandedCondition = Duration.ZERO
        lastUpdateTime = 0L
        stateMachine.transition(Event.OnReset)
    }

    /* auto start state machine */
    private val stateMachine = StateMachine.create<State, Event, SideEffect> {
        initialState(State.WaitForTakeOff)

        state<State.WaitForTakeOff> {
            on<Event.OnUpdate> { event ->
                // This 'if' statement is your Guard Condition
                if (doTakeOff()) {
                    transitionTo(State.TakeOff, SideEffect.TakeOff)
                } else {
                    // If the guard is not met, stay in this state and continue monitoring
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        state<State.TakeOff> {
            on<Event.OnUpdate> { event ->
                // Guard: Has enough time passed in the "flying" condition?
                if (doFlying()) {
                    transitionTo(State.Flying)
                } else {
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        state<State.Flying> {
            on<Event.OnUpdate> { event ->
                // Guard: Has enough time passed in the "landing" condition?
                if (doLanding()) {
                    transitionTo(State.Landing)
                } else {
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        state<State.Landing> {
            on<Event.OnUpdate> { event ->
                // Guard: Has the craft been "landed" for long enough?
                if (doFlying()) {
                    transitionTo(State.Flying)
                } else if (doLanded()) {
                    transitionTo(State.Landed, SideEffect.Landed)
                } else {
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        state<State.Landed> {
            on<Event.OnUpdate> { event ->
                // Guard: Has it been long enough since landing to reset the machine?
                if (doReset()) {
                    transitionTo(State.WaitForTakeOff) // Reset the machine
                } else {
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition

            // Reset time accumulators when leaving a state
            if (validTransition.fromState != validTransition.toState) {
                timeInTakeOffCondition = Duration.ZERO
                timeInFlyingCondition = Duration.ZERO
                timeInLandingCondition = Duration.ZERO
                timeInLandedCondition = Duration.ZERO
                timeInResetCondition = Duration.ZERO
            }

            when (val sideEffect = validTransition.sideEffect) {
                is SideEffect.TakeOff -> {
                    onTakeOff()
                }

                is SideEffect.Landed -> {
                    onLanded()
                }

                is SideEffect.Monitor -> {
                    // This is where we check conditions and accumulate time for our guards
                    when (validTransition.fromState) {
                        State.WaitForTakeOff -> {
                            if (isTakeOffCondition()) {
                                timeInTakeOffCondition += sideEffect.elapsedTime
                            } else {
                                timeInTakeOffCondition = Duration.ZERO // Reset if condition fails
                            }
                        }

                        State.TakeOff -> {
                            if (isFlyingCondition()) {
                                timeInFlyingCondition += sideEffect.elapsedTime
                            } else {
                                timeInFlyingCondition = Duration.ZERO
                            }
                        }

                        State.Flying -> {
                            if (isLandingCondition()) {
                                timeInLandingCondition += sideEffect.elapsedTime
                            } else {
                                timeInLandingCondition = Duration.ZERO
                            }
                        }

                        State.Landing -> {
                            if (isLandedCondition()) {
                                timeInLandedCondition += sideEffect.elapsedTime
                            } else if (isFlyingCondition()) {
                                timeInFlyingCondition += sideEffect.elapsedTime
                            } else {
                                timeInLandedCondition = Duration.ZERO
                                timeInFlyingCondition = Duration.ZERO
                            }
                        }

                        State.Landed -> {
                            if (isLandedCondition()) {
                                timeInResetCondition += sideEffect.elapsedTime
                            } else {
                                timeInResetCondition = Duration.ZERO
                            }
                        }
                    }
                }

                null -> {}
            }
        }
    }

    companion object {

        /* state machine */
        sealed class State {
            data object WaitForTakeOff : State()
            data object TakeOff : State()
            data object Flying : State()
            data object Landing : State()
            data object Landed : State()
        }

        sealed class Event {
            data class OnUpdate(val elapsedTime: Duration) : Event()
            data object OnReset : Event()
        }

        sealed class SideEffect {
            data class Monitor(val elapsedTime: Duration) : SideEffect()
            data object TakeOff : SideEffect()
            data object Landed : SideEffect()
        }
    }
}