package com.alpsfly.aeroglide.firebase

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // This tree should be a singleton
class CrashlyticsTree @Inject constructor() : Timber.Tree() {

    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.ERROR, Log.WARN -> {
                if (t != null) {
                    crashlytics.recordException(t)
                } else {
                    // Create a custom exception to make these searchable in Crashlytics
                    crashlytics.recordException(TimberLogException(message))
                }
                // Optionally log the message as well for context
                crashlytics.log("$tag: $message")
            }

            Log.INFO -> {
                crashlytics.log("$tag: $message")
            }

            Log.DEBUG, Log.VERBOSE -> {

            }
        }
    }
}

/** A custom exception to distinguish Timber logs from real crashes. */
private class TimberLogException(message: String) : Exception(message)
