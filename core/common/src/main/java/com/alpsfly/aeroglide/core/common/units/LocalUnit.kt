package com.alpsfly.aeroglide.core.common.units

class LocalUnit internal constructor( // todo: make template
    private val value: Float, // value
    private val unit: UnitConverter.Unit,
    private val digits: Number = 0,
    private val symbol: String = "",
    private val showSymbol: Boolean = true
) {
    companion object {
        fun of(unit: UnitConverter.Unit): LocalUnit {
            return LocalUnit(0f, unit)
        }
        fun of(length: Float, unit: UnitConverter.Unit): LocalUnit {
            return LocalUnit(length, unit)
        }
    }

    fun toUnit(to: UnitConverter.Unit): LocalUnit {
        return LocalUnit(UnitConverter.convert(value, unit, to), to, digits, to.symbol, showSymbol)
    }

    fun withDigits(count: Number): LocalUnit {
        return LocalUnit(value, unit, count, symbol)
    }

    fun withSymbol(flag: Boolean): LocalUnit {
        return when (flag) {
            true -> LocalUnit(value, unit, digits, symbol, true)
            false -> LocalUnit(value, unit, digits, symbol, false)
        }
    }

    fun toValue() = value

    override fun toString() = when (showSymbol) {
        true -> String.format("%.${digits}f %s", value, symbol).trim()
        false -> String.format("%.${digits}f", value).trim()
    }

    fun toLocalValue(): Float {
        return when(isMetric()) {
            true -> toUnit(this.unit).value // optimize possible
            false -> {
                when (unit) {
                    UnitConverter.Unit.M -> toUnit(UnitConverter.Unit.FT).value
                    UnitConverter.Unit.KM -> toUnit(UnitConverter.Unit.MI).value
                    UnitConverter.Unit.MS -> toUnit(UnitConverter.Unit.FPM).value
                    UnitConverter.Unit.KMH -> toUnit(UnitConverter.Unit.MPH).value
                    else -> value
                }
            }
        }
    }

    fun toLocalSymbol(): String {
        return when(isMetric()) {
            true -> toUnit(this.unit).symbol
            false -> {
                when (unit) {
                    UnitConverter.Unit.M -> toUnit(UnitConverter.Unit.FT).symbol
                    UnitConverter.Unit.KM -> toUnit(UnitConverter.Unit.MI).symbol
                    UnitConverter.Unit.MS -> toUnit(UnitConverter.Unit.FPM).symbol
                    UnitConverter.Unit.KMH -> toUnit(UnitConverter.Unit.MPH).symbol
                    else -> symbol
                }
            }
        }
    }

    fun toLocalString(): String {
        return when(isMetric()) {
            true -> toUnit(this.unit).toString()
            false -> {
                when (unit) {
                    UnitConverter.Unit.M -> toUnit(UnitConverter.Unit.FT).toString()
                    UnitConverter.Unit.KM -> toUnit(UnitConverter.Unit.MI).toString()
                    UnitConverter.Unit.MS -> toUnit(UnitConverter.Unit.FPM).toString()
                    UnitConverter.Unit.KMH -> toUnit(UnitConverter.Unit.MPH).toString()
                    else -> symbol
                }
            }
        }
    }
}
