# Tinder StateMachine core
-keep class com.tinder.StateMachine { *; }
-keep class com.tinder.StateMachine$* { *; }

# Falls du eigene State/Events als sealed classes nutzt:
-keep class com.alpsfly.aeroglide.core.domain.usecase.state.AppState.** { *; }
-keep class com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager.** { *; }

# Wenn du Debugging/Logging nutzt:
-keepattributes InnerClasses, EnclosingMethod
