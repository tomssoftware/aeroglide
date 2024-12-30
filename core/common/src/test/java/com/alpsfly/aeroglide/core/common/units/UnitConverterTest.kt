package com.alpsfly.aeroglide.core.common.units

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UnitConverterTest {

    @Test
    fun `convert same unit should return same value`() {
        val amount = 10f
        val result = UnitConverter.convert(amount, UnitConverter.Unit.M, UnitConverter.Unit.M)
        assertEquals(amount, result, 0.001f)
    }

    @Test
    fun `convert meters to kilometers`() {
        val amount = 1000f
        val result = UnitConverter.convert(amount, UnitConverter.Unit.M, UnitConverter.Unit.KM)
        assertEquals(1f, result, 0.001f)
    }

    @Test
    fun `convert kilometers to meters`() {
        val amount = 1f
        val result = UnitConverter.convert(amount, UnitConverter.Unit.KM, UnitConverter.Unit.M)
        assertEquals(1000f, result, 0.001f)
    }

    @Test
    fun `convert miles to feet`() {
        val amount = 1f
        val result = UnitConverter.convert(amount, UnitConverter.Unit.MI, UnitConverter.Unit.FT)
        assertEquals(5280f, result, 1f)
    }

    @Test
    fun `convert feet to miles`() {
        val amount = 5280f
        val result = UnitConverter.convert(amount, UnitConverter.Unit.FT, UnitConverter.Unit.MI)
        assertEquals(1f, result, 0.001f)
    }

    @Test
    fun `convert meters per second to kilometers per hour`() {
        val amount = 1f
        val result = UnitConverter.convert(amount, UnitConverter.Unit.MS, UnitConverter.Unit.KMH)
        assertEquals(3.6f, result, 0.001f)
    }

    @Test
    fun `convert kilometers per hour to meters per second`() {
        val amount = 3.6f
        val result = UnitConverter.convert(amount, UnitConverter.Unit.KMH, UnitConverter.Unit.MS)
        assertEquals(1f, result, 0.001f)
    }

    @Test
    fun `convert miles per hour to feet per minute`() {
        val amount = 1f
        val result = UnitConverter.convert(amount, UnitConverter.Unit.MPH, UnitConverter.Unit.FPM)
        assertEquals(88f, result, 0.001f)
    }

    @Test
    fun `convert feet per minute to miles per hour`() {
        val amount = 88f
        val result = UnitConverter.convert(amount, UnitConverter.Unit.FPM, UnitConverter.Unit.MPH)
        assertEquals(1f, result, 0.001f)
    }

    @Test
    fun `convert incompatible units should throw exception`() {
        val amount = 10f
        assertThrows(UnitConverter.IncompatibleUnitTypesException::class.java) {
            UnitConverter.convert(amount, UnitConverter.Unit.M, UnitConverter.Unit.MS)
        }
    }

    @Test
    fun `convert incompatible units should throw exception 2`() {
        val amount = 10f
        assertThrows(UnitConverter.IncompatibleUnitTypesException::class.java) {
            UnitConverter.convert(amount, UnitConverter.Unit.KMH, UnitConverter.Unit.M)
        }
    }
}