package com.alpsfly.aeroglide.core.common.units

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.Locale

@RunWith(MockitoJUnitRunner::class)
class LocalUnitTest {

    // Save the original locale to restore it after tests
    private val originalLocale = Locale.getDefault()

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `create LocalUnit with of(unit) should have default value 0`() {
        val localUnit = LocalUnit.of(UnitConverter.Unit.M)
        assertEquals(0f, localUnit.toValue(), 0.001f)
    }

    @Test
    fun `create LocalUnit with of(value, unit) should have correct value`() {
        val localUnit = LocalUnit.of(10f, UnitConverter.Unit.KM)
        assertEquals(10f, localUnit.toValue(), 0.001f)
    }

    @Test
    fun `toUnit should convert to correct unit`() {
        val localUnit = LocalUnit.of(1000f, UnitConverter.Unit.M)
        val convertedUnit = localUnit.toUnit(UnitConverter.Unit.KM)
        assertEquals(1f, convertedUnit.toValue(), 0.001f)
        //assertEquals(UnitConverter.Unit.KM.symbol, convertedUnit.symbol)
    }

    @Test
    fun `withDigits should set correct digits`() {
        Locale.setDefault(Locale.GERMANY)
        val localUnit = LocalUnit.of(12.345f, UnitConverter.Unit.M).withDigits(2)
        assertEquals("12,35", localUnit.toString())
    }

//    @Test
//    fun `withSymbol should show symbol when true`() {
//        val localUnit = LocalUnit.of(10f, UnitConverter.Unit.M).withSymbol(true)
//        assertEquals("10 m", localUnit.toString())
//    }

    @Test
    fun `withSymbol should not show symbol when false`() {
        val localUnit = LocalUnit.of(10f, UnitConverter.Unit.M).withSymbol(false)
        assertEquals("10", localUnit.toString())
    }

    //    @Test
//    fun `toString should format with default digits and symbol`() {
//        val localUnit = LocalUnit.of(12.345f, UnitConverter.Unit.M)
//        assertEquals("12,345 m", localUnit.toString())
//    }
//
//    @Test
//    fun `toString should format with specified digits and symbol`() {
//        val localUnit = LocalUnit.of(12.345f, UnitConverter.Unit.M).withDigits(2)
//        assertEquals("12,35 m", localUnit.toString())
//    }
//
//    @Test
//    fun `toString should format with specified digits and without symbol`() {
//        val localUnit = LocalUnit.of(12.345f, UnitConverter.Unit.M).withDigits(2).withSymbol(false)
//        assertEquals("12.35", localUnit.toString())
//    }
//
//    @Test
//    fun `toLocalValue should return same value for metric units`() {
//        val localUnit = LocalUnit.of(10f, UnitConverter.Unit.M)
//        assertEquals(10f, localUnit.toLocalValue(), 0.001f)
//    }
//
    @Test
    fun `toLocalValue should convert to feet for meters`() {
        Locale.setDefault(Locale.US)
        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.M)
        assertEquals(3.28084f, localUnit.toLocalValue(), 0.001f)
    }

    @Test
    fun `toLocalValue should convert to miles for kilometers`() {
        Locale.setDefault(Locale.US)
        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.KM)
        assertEquals(0.621371f, localUnit.toLocalValue(), 0.001f)
    }


    @Test
    fun `toLocalValue should convert to feet per minute for meters per second`() {
        Locale.setDefault(Locale.US)
        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.MS)
        assertEquals(196.8504f, localUnit.toLocalValue(), 0.001f)
    }

    //    @Test
//    fun `toLocalValue should convert to miles per hour for kilometers per hour`() {
//        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.KMH)
//        assertEquals(0.621371f, localUnit.toLocalValue(), 0.001f)
//    }
//
//    @Test
//    fun `toLocalSymbol should return same symbol for metric units`() {
//        val localUnit = LocalUnit.of(10f, UnitConverter.Unit.M)
//        assertEquals("m", localUnit.toLocalSymbol())
//    }
//
//    @Test
//    fun `toLocalSymbol should return feet symbol for meters`() {
//        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.M)
//        assertEquals("ft", localUnit.toLocalSymbol())
//    }
//
//    @Test
//    fun `toLocalSymbol should return miles symbol for kilometers`() {
//        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.KM)
//        assertEquals("mi", localUnit.toLocalSymbol())
//    }
//
//    @Test
//    fun `toLocalSymbol should return feet per minute symbol for meters per second`() {
//        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.MS)
//        assertEquals("ft/min", localUnit.toLocalSymbol())
//    }
//
//    @Test
//    fun `toLocalSymbol should return miles per hour symbol for kilometers per hour`() {
//        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.KMH)
//        assertEquals("mi/h", localUnit.toLocalSymbol())
//    }
//
//    @Test
//    fun `toLocalString should return same string for metric units`() {
//        val localUnit = LocalUnit.of(10f, UnitConverter.Unit.M)
//        assertEquals("10.000 m", localUnit.toLocalString())
//    }
//
//    @Test
//    fun `toLocalString should return feet string for meters`() {
//        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.M)
//        assertEquals("3.281 ft", localUnit.toLocalString())
//    }
//
    @Test
    fun `toLocalString should return miles string for kilometers`() {
        Locale.setDefault(Locale.US)
        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.KM)
        assertEquals("0.621 mi", localUnit.withDigits(3).toLocalString())
    }
//
//    @Test
//    fun `toLocalString should return feet per minute string for meters per second`() {
//        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.MS)
//        assertEquals("196.850 ft/min", localUnit.toLocalString())
//    }
//
//    @Test
//    fun `toLocalString should return miles per hour string for kilometers per hour`() {
//        val localUnit = LocalUnit.of(1f, UnitConverter.Unit.KMH)
//        assertEquals("0.621 mi/h", localUnit.toLocalString())
//    }
}

