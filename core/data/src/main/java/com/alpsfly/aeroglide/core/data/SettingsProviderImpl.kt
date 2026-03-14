package com.alpsfly.aeroglide.core.data

import android.content.SharedPreferences
import com.alpsfly.aeroglide.core.common.getBooleanFlow
import com.alpsfly.aeroglide.core.common.getFloatFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AutoStartSettingsProviderImpl @Inject constructor(
    private val prefs: SharedPreferences
) : AutoStartSettingsProvider {

    // Speed is stored as km/h (Float) and converted to m/s on read.
    // Climbrate is stored and read directly as m/s (Float) – no scaling needed.

    override val velocityLimitTakeOff: Flow<Float>
        get() = prefs.getFloatFlow("spk_auto_start_speed", 20f).map { it / 3.6f }
    override val velocityLimitLanding: Flow<Float>
        get() = prefs.getFloatFlow("spk_auto_start_speed", 20f).map { it / 3.6f }
    override val climbrateLimitTakeOff: Flow<Float>
        get() = prefs.getFloatFlow("spk_auto_start_climbrate", 0.5f)
    override val climbrateLimitLanding: Flow<Float>
        // TODO: "spk_auto_start_climbrate_landing" is never written by SettingsViewModel,
        //       so this always returns the default value of -0.5f. Either add a dedicated
        //       UI control + write path in SettingsViewModel, or unify with
        //       "spk_auto_start_climbrate" if a single threshold suffices for both
        //       take-off and landing.
        get() = prefs.getFloatFlow("spk_auto_start_climbrate_landing", -0.5f)
    override val isAutoStartEnabled: Flow<Boolean>
        get() = prefs.getBooleanFlow("spk_auto_start_enabled", false)
}
