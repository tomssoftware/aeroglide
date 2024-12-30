package com.alpsfly.aeroglide.core.data.util

const val Q_ACCELERATION = 0.9f
const val R_ALTITUDE = 0.1f
const val ALTITUDE_0 = 500f

interface IKalmanFilter {
    fun configure(qAccel: Float, rAltitude: Float, h: Float)
    fun reset(h: Float)

    fun update(h: Float)
    fun predict(a: Float, dt: Float)

    val altitude: Float
    val climbrate: Float
}

/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <robin.lilja@gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return. - Robin Lilja
 *
 * @file altitude_kf.cpp
 * @author Robin Lilja
 * @date 23 Jul 2015
 */
class KalmanFilter(private var qAccel: Float, private var rAltitude: Float) : IKalmanFilter {
    private val q: Array<FloatArray> = arrayOf(
        floatArrayOf(0f, 0f),
        floatArrayOf(0f, 0f)
    )
    private val p: Array<FloatArray> = arrayOf(
        floatArrayOf(0f, 0f),
        floatArrayOf(0f, 0f)
    )
    private val k = floatArrayOf(0f, 0f)
    private var h = 0f // altitude z-axis
    private var v = 0f // velocity
    override val altitude: Float
        get() = h
    override val climbrate: Float
        get() = v

    override fun configure(qAccel: Float, rAltitude: Float, h: Float) {
        this.qAccel = qAccel
        this.rAltitude = rAltitude

        this.h = h
        this.v = 0.0f
    }

    override fun reset(h: Float) {
        this.h = h
    }

    override fun predict(a: Float, dt: Float) {
        val dt2 = dt * dt
        val dt2div2 = dt2 / 2f
        val dt2div4 = dt2 / 4f
        // x = F.x + G.u
        h += v * dt + a * dt2div2
        v += a * dt

        q[0][0] = dt2div4 * qAccel
        q[0][1] = dt2div2 * qAccel
        q[1][0] = dt2div2 * qAccel
        q[1][1] = dt2 * qAccel
        // P = F.P.F' + Q
        p[0][0] = p[0][0] + (p[1][0] + p[0][1] + (p[1][1] + q[0][0]) * dt) * dt
        p[0][1] = p[0][1] + (p[1][1] + q[0][1]) * dt
        p[1][0] = p[1][0] + (p[1][1] + q[1][0]) * dt
        p[1][1] = p[1][1] + q[1][1]
        //Timber.v("Climbrate: $v m/s")
    }

    override fun update(h: Float) {
        //  y = z - H.x
        val y = h - this.h
        // S = H.P.H' + R
        val s = p[0][0] + rAltitude
        val si = 1.0f / s
        // K = P.H'.S^(-1)
        k[0] = p[0][0] * si
        k[1] = p[1][0] * si
        // x = x + K.y
        this.h += k[0] * y
        this.v += k[1] * y
        // P = P - K.(H.P)
        p[0][0] = p[0][0] - (k[0] * p[0][0])
        p[0][1] = p[0][1] - (k[0] * p[0][1])
        p[1][0] = p[1][0] - (k[1] * p[0][0])
        p[1][1] = p[1][1] - (k[1] * p[0][1])
        //Timber.v("covariance: ${P[0][0]} ${P[0][1]}, ${P[1][0]}, ${P[1][1]}")
    }
}