package com.alpsfly.aeroglide.core.domain.usecase

/**
 * Represents the three mutually exclusive playback zones of the variometer tone engine.
 *
 * The zone is derived from the current climb rate and the user-configured thresholds:
 * - [Climb] when climb rate exceeds the climb threshold.
 * - [Sink]  when climb rate falls below the sink threshold.
 * - [Silence] in the dead-zone between the two thresholds.
 *
 * Each variant carries the pre-computed audio parameters so that callers need not
 * re-apply the LX-Navia mapping formula.
 */
sealed interface VarioToneState {

    /**
     * No tone should play. Climb rate is within the configured dead-zone.
     */
    data object Silence : VarioToneState

    /**
     * Pulsed beep tone for positive climb rate above the climb threshold.
     *
     * @param frequencyHz Tone frequency in Hz (range: 700–2200 Hz).
     *                    Increases linearly with climb rate.
     * @param beepMs      Duration of the audible beep per cycle in milliseconds (range: 60–500 ms).
     *                    Decreases as climb rate increases (faster beeps = more lift).
     * @param pauseMs     Duration of silence between beeps (equals [beepMs] → 50 % duty cycle).
     */
    data class Climb(
        val frequencyHz: Float,
        val beepMs: Long,
        val pauseMs: Long,
    ) : VarioToneState

    /**
     * Continuous tone for negative sink rate below the sink threshold.
     *
     * @param frequencyHz Tone frequency in Hz (range: 220–500 Hz).
     *                    Decreases linearly as sink rate worsens.
     */
    data class Sink(val frequencyHz: Float) : VarioToneState
}

