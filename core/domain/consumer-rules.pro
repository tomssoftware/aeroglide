# Add module specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Tinder StateMachine core
-keep class com.tinder.StateMachine { *; }
-keep class com.tinder.StateMachine$* { *; }

# Falls du eigene State/Events als sealed classes nutzt:
-keep class de.tomssoftware.aeroglide.core.domain.usecase.state.AppState.** { *; }
-keep class de.tomssoftware.aeroglide.core.domain.usecase.state.AppStateManager.** { *; }

# Wenn du Debugging/Logging nutzt:
-keepattributes InnerClasses, EnclosingMethod
