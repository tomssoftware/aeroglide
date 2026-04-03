package de.tomssoftware.aeroglide.core.data

import kotlinx.coroutines.flow.Flow

interface AutoStartSettingsProvider {
    val velocityLimitTakeOff: Flow<Float>
    val velocityLimitLanding: Flow<Float>
    val climbrateLimitTakeOff: Flow<Float>
    val climbrateLimitLanding: Flow<Float>
    val isAutoStartEnabled: Flow<Boolean>
}