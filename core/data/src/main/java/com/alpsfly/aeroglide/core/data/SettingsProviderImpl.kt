package com.alpsfly.aeroglide.core.data

import android.content.SharedPreferences
import com.alpsfly.aeroglide.core.common.getFloatFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AutoStartSettingsProviderImpl @Inject constructor(
    private val prefs: SharedPreferences
) : AutoStartSettingsProvider {
    override val velocityLimitTakeOff: Flow<Float>
        get() = prefs.getFloatFlow("spk_auto_start_speed", 2 * 1.38f)
    override val velocityLimitLanding: Flow<Float>
        get() = prefs.getFloatFlow("spk_auto_start_speed", 2 * 1.38f)
    override val climbrateLimitTakeOff: Flow<Float>
        get() = prefs.getFloatFlow("spk_auto_start_climbrate", 0.5f)
    override val climbrateLimitLanding: Flow<Float>
        get() = prefs.getFloatFlow("spk_auto_start_climbrate_landing", -0.5f)
}