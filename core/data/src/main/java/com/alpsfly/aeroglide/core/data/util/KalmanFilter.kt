package com.alpsfly.aeroglide.core.data.util

import timber.log.Timber

/**
 * Prozessrauschen der Beschleunigung.
 * Höher = schnellere Reaktion auf Beschleunigungsänderungen, aber mehr Rauschen.
 * Empirisch ermittelt für den eingesetzten Barometer-Sensor.
 */
const val Q_ACCELERATION = 0.9f

/**
 * Messrauschen der Höhe in Metern².
 * Höher = stärkere Glättung, aber langsamere Reaktion auf Höhenänderungen.
 * Empirisch ermittelt für den eingesetzten Barometer-Sensor.
 */
const val R_ALTITUDE = 0.1f

/** Hohe initiale Kovarianz-Diagonale → Filter konvergiert schnell auf die erste Messung. */
private const val P_INITIAL = 1000f

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
     *
     * P wird auf [P_INITIAL] gesetzt (hohe initiale Unsicherheit), damit der Filter
     * bei der ersten Messung sofort einen Kalman-Gain > 0 hat und konvergiert.
     * P = 0 würde bedeuten: Gain = 0 → erste Messung wird komplett ignoriert.
     */
    override fun reset(h: Float) {
        this.h = h
        this.v = 0f
        // Hohe initiale Unsicherheit → Kalman-Gain bei erster update()-Messung > 0
        p[0][0] = P_INITIAL; p[0][1] = 0f
        p[1][0] = 0f;        p[1][1] = P_INITIAL
    }

    override fun predict(a: Float, dt: Float) {
        // Guard: ungültige Eingaben schützen den Filter vor dauerhafter NaN/Infinity-Vergiftung
        if (!a.isFinite() || !dt.isFinite() || dt <= 0f) {
            Timber.w("KalmanFilter.predict: ungültige Eingabe ignoriert (a=$a, dt=$dt)")
            return
        }

        val dt2 = dt * dt
        val dt2div2 = dt2 / 2f
        val dt2div4 = dt2 / 4f

        // x = F·x + G·u  (Zustandsfortschreibung)
        h += v * dt + a * dt2div2
        v += a * dt

        // Q = Prozessrauschen-Matrix (symmetrisch: q01 == q10)
        val q00  = dt2div4 * qAccel
        val q0110 = dt2div2 * qAccel  // q01 == q10, da Q symmetrisch
        val q11  = dt2 * qAccel

        // P = F·P·Fᵀ + Q  (Kovarianzfortschreibung)
        p[0][0] = p[0][0] + (p[1][0] + p[0][1] + (p[1][1] + q00)   * dt) * dt
        p[0][1] = p[0][1] + (p[1][1] + q0110) * dt
        p[1][0] = p[1][0] + (p[1][1] + q0110) * dt
        p[1][1] = p[1][1] + q11
    }

    override fun update(h: Float) {
        // Guard: ungültige Eingabe schützt den Filter vor NaN/Infinity-Vergiftung
        if (!h.isFinite()) {
            Timber.w("KalmanFilter.update: ungültige Höhenmessung ignoriert (h=$h)")
            return
        }

        // y = z - H·x  (Innovationsresiduum)
        val y = h - this.h

        // S = H·P·Hᵀ + R  (Innovationskovarianz)
        val s = p[0][0] + rAltitude

        // Guard: Division durch (near-)Zero verhindern → tritt auf wenn Kovarianzmatrix korrumpiert
        if (s < 1e-10f) {
            Timber.w("KalmanFilter.update: Kovarianzmatrix korrumpiert (s=$s, p00=${p[0][0]}), Update übersprungen")
            return
        }

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