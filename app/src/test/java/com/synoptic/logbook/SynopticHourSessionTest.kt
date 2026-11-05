package com.synoptic.logbook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class SynopticHourSessionTest {

    private fun utcMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(year, month - 1, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun testSynopticHourRounding() {
        // 02:55 UTC should belong to 0300 UTC synoptic window
        val t0255 = utcMillis(2026, 11, 5, 2, 55)
        assertEquals("0300", synopticHourFor(t0255))

        // 03:00 UTC exactly belongs to 0300 UTC
        val t0300 = utcMillis(2026, 11, 5, 3, 0)
        assertEquals("0300", synopticHourFor(t0300))

        // 04:29 UTC is still within 0300 UTC window (half window is 90 mins -> 01:30 to 04:30)
        val t0429 = utcMillis(2026, 11, 5, 4, 29)
        assertEquals("0300", synopticHourFor(t0429))

        // 04:30 UTC crosses into 0600 UTC window
        val t0430 = utcMillis(2026, 11, 5, 4, 30)
        assertEquals("0600", synopticHourFor(t0430))

        // 06:00 UTC belongs to 0600 UTC
        val t0600 = utcMillis(2026, 11, 5, 6, 0)
        assertEquals("0600", synopticHourFor(t0600))
    }

    @Test
    fun testMidnightDateRollover() {
        // 23:45 UTC on Nov 5 rolls over to Nov 6 0000 UTC
        val t2345 = utcMillis(2026, 11, 5, 23, 45)
        assertEquals("0000", synopticHourFor(t2345))
        assertEquals("06112026", observationDateUtc(t2345))

        // 22:20 UTC on Nov 5 belongs to Nov 5 2100 UTC
        val t2220 = utcMillis(2026, 11, 5, 22, 20)
        assertEquals("2100", synopticHourFor(t2220))
        assertEquals("05112026", observationDateUtc(t2220))
    }

    @Test
    fun testDifferenceFormatting() {
        assertEquals("+1.2", formatDifference("820.0", "818.8"))
        assertEquals("-0.6", formatDifference("821.4", "822.0"))
        assertEquals("0.0", formatDifference("820.0", "820.0"))
        assertEquals("", formatDifference("", "820.0"))
        assertEquals("", formatDifference("820.0", ""))
    }
}
