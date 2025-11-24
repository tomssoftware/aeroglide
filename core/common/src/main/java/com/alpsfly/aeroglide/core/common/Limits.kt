package com.alpsfly.aeroglide.core.common

import timber.log.Timber

class Limits {
    companion object {
        const val MIN_PRESSURE = 30000f // pascal
        const val MAX_PRESSURE = 120000f // pascal
        const val MIN_ALTITUDE = -500f // meter
        const val MAX_ALTITUDE = 15000f // meter
        const val MIN_CLIMBRATE = -60f // m/s
        const val MAX_CLIMBRATE = 30f // m/s
        const val MAX_SPEED = 300f // km/h
        const val MIN_SPEED = 0f // km/h
        private const val MIN_VERTICAL_ACCELERATION = -100f // ms2
        private const val MAX_VERTICAL_ACCELERATION = 100f // ms2


        const val INVALID_ALTITUDE = Float.NEGATIVE_INFINITY
        const val INVALID_SPEED = Float.NEGATIVE_INFINITY
        const val INVALID_PRESSURE = Float.NEGATIVE_INFINITY
        const val INVALID_CLIMBRATE = Float.NEGATIVE_INFINITY

        private fun isInvalid(value: Float) = value.isNaN() || value.isInfinite()

        fun isAltitudeOutOfRange(altitude: Float) = (altitude < MIN_ALTITUDE || altitude > MAX_ALTITUDE)
        fun isPressureOutOfRange(pressure: Float) = (pressure < MIN_PRESSURE || pressure > MAX_PRESSURE)
        fun isClimbrateOutOfRange(climbrate: Float) = (climbrate < MIN_CLIMBRATE || climbrate > MAX_CLIMBRATE)
        fun isSpeedOutOfRange(speed: Float) = (speed < MIN_SPEED || speed > MAX_SPEED)
        fun isVerticalAccelerationOutOfRange(accel: Float) =
            (accel < MIN_VERTICAL_ACCELERATION || accel > MAX_VERTICAL_ACCELERATION)

        fun checkAltitude(altitude: Float): Boolean {
            val result = (isInvalid(altitude) || isAltitudeOutOfRange(altitude))
            if (result) {
                Timber.w("Altitude out of range: $altitude")
            }
            return !result
        }


        fun checkPressure(pressure: Float): Boolean {
            val result = (isInvalid(pressure) || isPressureOutOfRange(pressure))
            if (result) {
                Timber.w("Pressure out of range: $pressure")
            }
            return !result
        }

        fun checkClimbrate(climbrate: Float): Boolean {
            val result = (isInvalid(climbrate) || isClimbrateOutOfRange(climbrate))
            if (result) {
                Timber.w("Climbrate out of range: $climbrate")
            }
            return !result
        }

        fun checkSpeed(speed: Float): Boolean {
            val result = (isInvalid(speed) || isSpeedOutOfRange(speed))
            if (result) {
                Timber.w("Speed out of range: $speed")
            }
            return !result
        }

        fun checkVerticalAcceleration(accel: Float): Boolean {
            val result = (isInvalid(accel) || isVerticalAccelerationOutOfRange(accel))
            if (result) {
                Timber.w("Vertical acceleration out of range: $accel")
            }
            return !result
        }
    }
}