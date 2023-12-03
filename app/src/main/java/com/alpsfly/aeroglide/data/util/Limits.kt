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
            if (isInvalid(altitude) || (altitude < minAltitude || altitude > maxAltitude)) {
                return false
            }
            return true
        }

        fun checkPressure(pressure: Float): Boolean {
            if (isInvalid(pressure) || (pressure < minPressure || pressure > maxPressure)) {
                return false
            }
            return true
        }

        fun checkClimbrate(climbrate: Float): Boolean {
            if (isInvalid(climbrate) || (climbrate < minClimbrate || climbrate > maxClimbrate)) {
                return false
            }
            return true
        }

        fun checkSpeed(speed: Float): Boolean {
            if (isInvalid(speed) || (speed < minSpeed || speed > maxSpeed)) {
                return false
            }
            return true
        }

        fun checkVerticalAcceleration(accel: Float): Boolean {
            if (isInvalid(accel) || (accel < minVerticalAcceleration || accel > maxVerticalAcceleration)
            ) {
                return false
            }
            return true
        }
    }
}