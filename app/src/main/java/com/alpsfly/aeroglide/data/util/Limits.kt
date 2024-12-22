package com.alpsfly.aeroglide.data.util

class Limits {
    companion object {
        const val minPressure = 30000f // pascal
        const val maxPressure = 120000f // pascal
        const val minAltitude = -500f // meter
        const val maxAltitude = 15000f // meter
        const val minClimbrate = -60f // m/s
        const val maxClimbrate = 30f // m/s
        const val maxSpeed = 300f // km/h
        const val minSpeed = 0f // km/h
        private const val minVerticalAcceleration = -100f // ms2
        private const val maxVerticalAcceleration = 100f // ms2


        const val invalidAltitude = Float.NEGATIVE_INFINITY
        const val invalidSpeed = Float.NEGATIVE_INFINITY
        const val invalidPressure = Float.NEGATIVE_INFINITY
        const val invalidClimbrate = Float.NEGATIVE_INFINITY

        private fun isInvalid(value: Float) = value.isNaN() || value.isInfinite()

        fun checkAltitude(altitude: Float): Boolean {
            return !(isInvalid(altitude) || (altitude < minAltitude || altitude > maxAltitude))
        }

        fun checkPressure(pressure: Float): Boolean {
            return !(isInvalid(pressure) || (pressure < minPressure || pressure > maxPressure))
        }

        fun checkClimbrate(climbrate: Float): Boolean {
            return !(isInvalid(climbrate) || (climbrate < minClimbrate || climbrate > maxClimbrate))
        }

        fun checkSpeed(speed: Float): Boolean {
            return !(isInvalid(speed) || (speed < minSpeed || speed > maxSpeed))
        }

        fun checkVerticalAcceleration(accel: Float): Boolean {
            return !(isInvalid(accel) || (accel < minVerticalAcceleration || accel > maxVerticalAcceleration))
        }
    }
}