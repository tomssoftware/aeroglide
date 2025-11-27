package com.alpsfly.aeroglide.core.common

class Limits {
    companion object {
        const val MIN_PRESSURE = 500 // hPa
        const val MAX_PRESSURE = 1100 // hPa
        const val MIN_ALTITUDE = -500f // meter
        const val MAX_ALTITUDE = 15000f // meter
        const val MIN_CLIMBRATE = -60f // m/s
        const val MAX_CLIMBRATE = 30f // m/s
        const val MAX_SPEED = 300f // km/h
        const val MIN_SPEED = 0f // km/h
        private const val MIN_VERTICAL_ACCELERATION = -100f // ms2
        private const val MAX_VERTICAL_ACCELERATION = 100f // ms2

        private fun isInvalid(value: Float) = value.isNaN() || value.isInfinite()

        fun isAltitudeOutOfRange(altitude: Float) = (altitude < MIN_ALTITUDE || altitude > MAX_ALTITUDE)
        fun isPressureOutOfRange(pressure: Float) = (pressure < MIN_PRESSURE || pressure > MAX_PRESSURE)
        fun isClimbrateOutOfRange(climbrate: Float) = (climbrate < MIN_CLIMBRATE || climbrate > MAX_CLIMBRATE)
        fun isSpeedOutOfRange(speed: Float) = (speed < MIN_SPEED || speed > MAX_SPEED)
        fun isVerticalAccelerationOutOfRange(accel: Float) =
            (accel < MIN_VERTICAL_ACCELERATION || accel > MAX_VERTICAL_ACCELERATION)

        fun checkAltitude(altitude: Float): Boolean {
            val result = (isInvalid(altitude) || isAltitudeOutOfRange(altitude))
            return !result
        }

        fun checkPressure(pressure: Float): Boolean {
            val result = (isInvalid(pressure) || isPressureOutOfRange(pressure))
            return !result
        }

        fun checkClimbrate(climbrate: Float): Boolean {
            val result = (isInvalid(climbrate) || isClimbrateOutOfRange(climbrate))
            return !result
        }

        fun checkSpeed(speed: Float): Boolean {
            val result = (isInvalid(speed) || isSpeedOutOfRange(speed))
            return !result
        }

        fun checkVerticalAcceleration(accel: Float): Boolean {
            val result = (isInvalid(accel) || isVerticalAccelerationOutOfRange(accel))
            return !result
        }
    }
}