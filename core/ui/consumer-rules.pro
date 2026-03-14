# Add module specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Room Entity Keep Rules
-keepclassmembers class com.alpsfly.aeroglide.core.model.database.** { *; }