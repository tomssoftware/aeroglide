package com.alpsfly.aeroglide.core.data.util

const val Q_ACCELERATION = 0.9f
const val R_ALTITUDE = 0.1f

/** Standardstarthöhe (m) – als Initialwert für [IKalmanFilter.reset] nutzbar */
@Suppress("unused")
const val ALTITUDE_0 = 500f

interface IKalmanFilter {
    /**
     * Konfiguriert den Filter und setzt alle internen Zustände zurück.
     * @param qAccel  Prozessrauschen der Beschleunigung (höher = schnellere Reaktion, mehr Rauschen)
     * @param rAltitude Messrauschen der Höhe (höher = stärkere Glättung)
     * @param h       Starthöhe in Metern
     */
    fun configure(qAccel: Float, rAltitude: Float, h: Float)

    /**
     * Setzt Höhe, Steigrate und Kovarianzmatrix auf Initialwerte zurück.
     * @param h Starthöhe in Metern
     */
    fun reset(h: Float)

    /**
     * Korrekturschritt: aktualisiert den Zustand anhand einer neuen Höhenmessung.
     * @param h gemessene Höhe in Metern
     */
    fun update(h: Float)

    /**
     * Prädiktionsschritt: schätzt den nächsten Zustand anhand der Beschleunigung.
     * @param a  Beschleunigung in m/s²
     * @param dt Zeitdelta in Sekunden
     */
    fun predict(a: Float, dt: Float)

    /** Gefilterte Höhe in Metern */
    val altitude: Float

    /** Gefilterte Steigrate in m/s */
    val climbrate: Float
}

/**
 * 2-Zustands Kalman-Filter für Höhe und Steigrate.
 *
 * Basiert auf: "THE BEER-WARE LICENSE" (Revision 42)
 * Original von Robin Lilja <robin.lilja@gmail.com>, 23 Jul 2015
 * https://github.com/...
 *
 * Zustandsvektor: x = [h, v]ᵀ  (Höhe, Vertikalgeschwindigkeit)
 * Messvektor:     z = [h]       (nur Höhe wird gemessen)
 */
class KalmanFilter(private var qAccel: Float, private var rAltitude: Float) : IKalmanFilter {

    // Kovarianzmatrix P (2×2), symmetrisch: p[0][1] == p[1][0]
    private val p: Array<FloatArray> = arrayOf(
        floatArrayOf(0f, 0f),
        floatArrayOf(0f, 0f)
    )

    private var h = 0f // Höhe (m), z-Achse
    private var v = 0f // Steigrate (m/s)

    override val altitude: Float get() = h
    override val climbrate: Float get() = v

    override fun configure(qAccel: Float, rAltitude: Float, h: Float) {
        this.qAccel = qAccel
        this.rAltitude = rAltitude
        reset(h)
    }

    /**
     * Vollständiger Reset: setzt Höhe, Steigrate und Kovarianzmatrix zurück.
     * Ohne Reset der Kovarianz würde der Filter mit alten Unsicherheitswerten arbeiten.
     */
    override fun reset(h: Float) {
        this.h = h
        this.v = 0f
        // Kovarianzmatrix auf Null zurücksetzen (hohe initiale Sicherheit)
        p[0][0] = 0f; p[0][1] = 0f
        p[1][0] = 0f; p[1][1] = 0f
    }

    override fun predict(a: Float, dt: Float) {
        val dt2 = dt * dt
        val dt2div2 = dt2 / 2f
        val dt2div4 = dt2 / 4f

        // x = F·x + G·u  (Zustandsfortschreibung)
        h += v * dt + a * dt2div2
        v += a * dt

        // Q = Prozessrauschen-Matrix
        val q00 = dt2div4 * qAccel
        val q01 = dt2div2 * qAccel
        val q10 = dt2div2 * qAccel
        val q11 = dt2 * qAccel

        // P = F·P·Fᵀ + Q  (Kovarianzfortschreibung)
        p[0][0] = p[0][0] + (p[1][0] + p[0][1] + (p[1][1] + q00) * dt) * dt
        p[0][1] = p[0][1] + (p[1][1] + q01) * dt
        p[1][0] = p[1][0] + (p[1][1] + q10) * dt
        p[1][1] = p[1][1] + q11
    }

    override fun update(h: Float) {
        // y = z - H·x  (Innovationsresiduum)
        val y = h - this.h

        // S = H·P·Hᵀ + R  (Innovationskovarianz)
        val s = p[0][0] + rAltitude

        // Guard: Division durch (near-)Zero verhindern → NaN/Infinity im Filter vermeiden
        if (s < 1e-10f) return

        val si = 1.0f / s

        // K = P·Hᵀ·S⁻¹  (Kalman-Gain)
        val k0 = p[0][0] * si
        val k1 = p[1][0] * si

        // x = x + K·y  (Zustandskorrektur)
        this.h += k0 * y
        this.v += k1 * y

        // P = P - K·(H·P)  (Kovarianzkorrektur)
        // WICHTIG: Originale P-Werte vor dem Überschreiben sichern!
        // Fehler ohne Sicherung: p[1][0] und p[1][1] würden mit bereits
        // überschriebenen p[0][0]/p[0][1] berechnet → Kovarianzmatrix korrumpiert.
        val p00 = p[0][0]
        val p01 = p[0][1]
        p[0][0] = p00 - (k0 * p00)
        p[0][1] = p01 - (k0 * p01)
        p[1][0] = p[1][0] - (k1 * p00)  // Original p[0][0] verwenden
        p[1][1] = p[1][1] - (k1 * p01)  // Original p[0][1] verwenden
    }
}