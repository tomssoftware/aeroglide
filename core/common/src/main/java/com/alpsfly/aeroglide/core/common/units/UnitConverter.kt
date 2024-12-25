package com.alpsfly.aeroglide.core.common.units

object UnitConverter {
    @Throws(IncompatibleUnitTypesException::class)
    fun convert(amount: Float, from: Unit, to: Unit): Float {

        if (from.type != to.type) {
            throw IncompatibleUnitTypesException()
        }
        val based: Float = if (from.isBase) {
            amount
        } else {
            from.to.convert(amount)
        }
        val result: Float = if (to.isBase) {
            based
        } else {
            to.from.convert(based)
        }

        return result
    }

    enum class UnitType(val label: String) {
        NONE("None"),
        LENGTH("Length"),
        SPEED("Speed")
    }

    enum class UnitSystem(val label: String) {
        IMPERIAL("Imperial"),
        METRIC("Metric")
    }

    enum class Unit {

        // BASE UNITS
        M(UnitType.LENGTH, "m", UnitSystem.METRIC, "m"),
        MS(UnitType.SPEED, "m/s", UnitSystem.METRIC, "m/s"),
        // LENGTH
        KM(UnitType.LENGTH, "km", UnitSystem.METRIC, "km", 1000.0f, 0.001f),
        MI(UnitType.LENGTH, "mi", UnitSystem.IMPERIAL,"mi", 1609.34f, 0.000621371f),
        FT(UnitType.LENGTH, "ft", UnitSystem.IMPERIAL, "ft", 0.3048f, 3.28084f),
        // SPEED
        KMH(UnitType.SPEED, "km/h", UnitSystem.METRIC, "km/h", 0.2777778f, 3.6f),
        MPH(UnitType.SPEED, "mi/h", UnitSystem.METRIC, "mi/h", 0.44704f, 2.236936f),
        FPM(UnitType.SPEED, "ft/min", UnitSystem.METRIC, "ft/min", 0.005080f, 196.8504f);

        var type: UnitType = UnitType.NONE
            private set
        var symbol: String = "None"
            private set
        var system: UnitSystem = UnitSystem.METRIC
            private set
        var label: String = "None"
            private set
        var to: Converter = MultiplicationConverter(0f)
            private set
        var from: Converter = MultiplicationConverter(0f)
            private set
        var isBase: Boolean = false
            private set

        constructor(
            type: UnitType,
            unit: String,
            system: UnitSystem,
            label: String,
            multiToRef: Float,
            multiFromRef: Float
        ) {
            this.type = type
            symbol = unit
            this.system = system
            this.label = label
            to = MultiplicationConverter(multiToRef)
            from = MultiplicationConverter(multiFromRef)
            isBase = false
        }

        constructor(
            type: UnitType,
            unit: String,
            system: UnitSystem,
            label: String,
            reference: Unit,
            converterTo: Converter,
            converterFrom: Converter
        ) {
            this.type = type
            symbol = unit
            this.system = system
            this.label = label
            to = converterTo
            from = converterFrom
            isBase = false
        }

        constructor(type: UnitType, unit: String, system: UnitSystem, label: String) {
            this.type = type
            symbol = unit
            this.system = system
            this.label = label
            to = UnitConverter()
            from = UnitConverter()
            isBase = true
        }
    }

    // CONVERTERS
    interface Converter {
        fun convert(amount: Float): Float
    }

    private class MultiplicationConverter(private val fac: Float) : Converter {
        override fun convert(amount: Float): Float {
            return amount * fac
        }
    }

    private class UnitConverter : Converter {
        override fun convert(amount: Float): Float {
            return amount * 1f
        }
    }

    //EXCEPTION
    class IncompatibleUnitTypesException : Exception()
}
