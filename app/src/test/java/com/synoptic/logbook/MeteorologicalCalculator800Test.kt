package com.synoptic.logbook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.io.FileInputStream

class MeteorologicalCalculator800Test {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            val tablesDir = File("src/main/assets/tables")
            MeteorologicalCalculator800.loadTablesFromStreams { name ->
                FileInputStream(File(tablesDir, name))
            }
        }
    }

    @Test
    fun testStandardPsychrometricLookup() {
        // Test vector from IMD manual: DB 10.8, WB 4.4
        val result = MeteorologicalCalculator800.calculatePsychrometrics(10.8, 4.4)
        assertNotNull(result)
        assertEquals(-4.2, result.dewPoint ?: 0.0, 0.01)
        assertEquals(33.0, result.rh ?: 0.0, 0.01)
        assertEquals(4.3, result.vp ?: 0.0, 0.01)
    }

    @Test
    fun testHighPsychrometricLookup() {
        // High table vector: DB 25.0, WB 22.0
        val result = MeteorologicalCalculator800.calculatePsychrometrics(25.0, 22.0)
        assertNotNull(result)
        assertEquals(20.7, result.dewPoint ?: 0.0, 0.01)
        assertEquals(77.0, result.rh ?: 0.0, 0.01)
        assertEquals(24.41, result.vp ?: 0.0, 0.01)
    }

    @Test
    fun testWetBulbGreaterThanDryBulb() {
        val result = MeteorologicalCalculator800.calculatePsychrometrics(20.0, 21.0)
        assertEquals("Wet bulb more than dry bulb!", result.error)
    }

    @Test
    fun testMslpCalculation() {
        // DB 10.8, SLP 820.0 -> 1284.0
        val mslp1 = MeteorologicalCalculator800.calculateMslp(10.8, 820.0)
        assertEquals(1284.0, mslp1 ?: 0.0, 0.01)

        // DB 10.8, SLP 821.3 -> 1297.7
        val mslp2 = MeteorologicalCalculator800.calculateMslp(10.8, 821.3)
        assertEquals(1297.7, mslp2 ?: 0.0, 0.01)

        // DB 25.0, SLP 821.3 -> 1283.7
        val mslp3 = MeteorologicalCalculator800.calculateMslp(25.0, 821.3)
        assertEquals(1283.7, mslp3 ?: 0.0, 0.01)
    }
}
