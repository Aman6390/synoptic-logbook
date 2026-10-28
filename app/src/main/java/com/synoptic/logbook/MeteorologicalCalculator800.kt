package com.synoptic.logbook

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Port of the 800 mb IMD Meteorological Engine from Aman6390/Dew-Point-Calculator-800mb.
 * Performs exact psychrometric table lookups and barometric reduction for MSLP.
 */
object MeteorologicalCalculator800 {

    private val dpTable = mutableMapOf<String, MutableMap<String, Double>>()
    private val rhTable = mutableMapOf<String, MutableMap<String, Double>>()
    private val dpHighTable = mutableMapOf<String, MutableMap<String, Double>>()
    private val rhHighTable = mutableMapOf<String, MutableMap<String, Double>>()
    private val vpTable = mutableMapOf<String, MutableMap<String, Double>>()

    data class BarRow(val lower: Double, val upper: Double, val values: Map<String, Double>)
    private val barRows = mutableListOf<BarRow>()
    private val corrections = mutableMapOf<String, MutableMap<String, Double>>()

    private var stdDbMin = -15.0
    private var stdDbMax = 40.0
    private var stdWbMin = -15.0
    private var stdWbMax = 20.0

    private var hiDbMin = 20.5
    private var hiDbMax = 44.5
    private var hiWbMin = 20.5
    private var hiWbMax = 30.0

    var isLoaded = false
        private set

    data class PsychroResult(
        val dewPoint: Double?,
        val rh: Double?,
        val vp: Double?,
        val error: String? = null
    )

    /**
     * Initializes and parses tables from Android assets.
     */
    @Synchronized
    fun loadTables(context: Context) {
        if (isLoaded) return
        loadTablesFromStreams { fileName ->
            context.assets.open("tables/$fileName")
        }
    }

    /**
     * Generic loader allowing unit test execution from arbitrary input streams.
     */
    @Synchronized
    fun loadTablesFromStreams(openStream: (String) -> InputStream) {
        dpTable.clear()
        rhTable.clear()
        dpHighTable.clear()
        rhHighTable.clear()
        vpTable.clear()
        barRows.clear()
        corrections.clear()

        // 1. Standard tables (header = 1, index_col = 0)
        val stdCols = loadStandard2DTable(openStream("dew_point_table.csv"), dpTable)
        loadStandard2DTable(openStream("rh_table.csv"), rhTable)

        // 2. High tables (header = 0, index_col = 0)
        val highCols = loadHigh2DTable(openStream("dew_point_high.csv"), dpHighTable)
        loadHigh2DTable(openStream("rh_high.csv"), rhHighTable)

        // 3. Vapor Pressure table
        loadVpTable(openStream("vp_table.csv"))

        // 4. Bar Reduction table
        loadBarReductionTable(openStream("bar_reduction_table.csv"))

        // 5. Correction table
        loadCorrectionTable(openStream("correction_table.csv"))

        // Dynamic boundaries matching Python df.index and df.columns
        if (dpTable.isNotEmpty()) {
            val dbKeys = dpTable.keys.mapNotNull { it.toDoubleOrNull() }
            if (dbKeys.isNotEmpty()) {
                stdDbMin = dbKeys.minOrNull() ?: -15.0
                stdDbMax = dbKeys.maxOrNull() ?: 40.0
            }
        }
        if (stdCols.isNotEmpty()) {
            stdWbMin = stdCols.minOrNull() ?: -15.0
            stdWbMax = stdCols.maxOrNull() ?: 20.0
        }

        if (dpHighTable.isNotEmpty()) {
            val dbKeys = dpHighTable.keys.mapNotNull { it.toDoubleOrNull() }
            if (dbKeys.isNotEmpty()) {
                hiDbMin = dbKeys.minOrNull() ?: 20.5
                hiDbMax = dbKeys.maxOrNull() ?: 44.5
            }
        }
        if (highCols.isNotEmpty()) {
            hiWbMin = highCols.minOrNull() ?: 20.5
            hiWbMax = highCols.maxOrNull() ?: 30.0
        }

        isLoaded = true
    }

    private fun formatAxis(raw: String): String? {
        val d = raw.trim().toDoubleOrNull() ?: return null
        return String.format(Locale.US, "%.1f", d)
    }

    private fun loadStandard2DTable(
        stream: InputStream,
        target: MutableMap<String, MutableMap<String, Double>>
    ): List<Double> {
        val reader = BufferedReader(InputStreamReader(stream))
        val lines = reader.readLines()
        if (lines.size < 2) return emptyList()

        // Line 0 is description ("Dry Bulb,wet bult,,,,...")
        // Line 1 contains column headers (wet bulb values)
        val headerParts = lines[1].split(",")
        val colKeys = mutableMapOf<Int, String>()
        val parsedCols = mutableListOf<Double>()
        for (i in 1 until headerParts.size) {
            val axis = formatAxis(headerParts[i])
            if (axis != null) {
                colKeys[i] = axis
                axis.toDoubleOrNull()?.let { parsedCols.add(it) }
            }
        }

        for (r in 2 until lines.size) {
            val parts = lines[r].split(",")
            val rowKey = formatAxis(parts[0]) ?: continue
            val rowMap = target.getOrPut(rowKey) { mutableMapOf() }
            for (c in 1 until parts.size) {
                val colKey = colKeys[c] ?: continue
                val cell = parts[c].trim()
                if (cell.isNotEmpty()) {
                    val value = cell.toDoubleOrNull()
                    if (value != null) {
                        rowMap[colKey] = value
                    }
                }
            }
        }
        return parsedCols
    }

    private fun loadHigh2DTable(
        stream: InputStream,
        target: MutableMap<String, MutableMap<String, Double>>
    ): List<Double> {
        val reader = BufferedReader(InputStreamReader(stream))
        val lines = reader.readLines()
        if (lines.isEmpty()) return emptyList()

        // Line 0 contains column headers (first item is "Dry Bulb")
        val headerParts = lines[0].split(",")
        val colKeys = mutableMapOf<Int, String>()
        val parsedCols = mutableListOf<Double>()
        for (i in 1 until headerParts.size) {
            val axis = formatAxis(headerParts[i])
            if (axis != null) {
                colKeys[i] = axis
                axis.toDoubleOrNull()?.let { parsedCols.add(it) }
            }
        }

        for (r in 1 until lines.size) {
            val parts = lines[r].split(",")
            val rowKey = formatAxis(parts[0]) ?: continue
            val rowMap = target.getOrPut(rowKey) { mutableMapOf() }
            for (c in 1 until parts.size) {
                val colKey = colKeys[c] ?: continue
                val cell = parts[c].trim()
                if (cell.isNotEmpty()) {
                    val value = cell.toDoubleOrNull()
                    if (value != null) {
                        rowMap[colKey] = value
                    }
                }
            }
        }
        return parsedCols
    }

    private fun loadVpTable(stream: InputStream) {
        val reader = BufferedReader(InputStreamReader(stream))
        val lines = reader.readLines()
        if (lines.isEmpty()) return

        // Columns are 0.0, 0.1, 0.2 ... 0.9
        val headerParts = lines[0].split(",")
        val colKeys = mutableMapOf<Int, String>()
        for (i in 1 until headerParts.size) {
            colKeys[i] = "0.${i - 1}"
        }

        for (r in 1 until lines.size) {
            val parts = lines[r].split(",")
            val rawKey = parts[0].trim()
            val rowKey = when {
                rawKey == "-0" -> "-0"
                rawKey == "0" -> "0"
                else -> {
                    val d = rawKey.toDoubleOrNull()
                    if (d != null && d % 1.0 == 0.0) d.toInt().toString() else rawKey
                }
            }
            val rowMap = vpTable.getOrPut(rowKey) { mutableMapOf() }
            for (c in 1 until parts.size) {
                val colKey = colKeys[c] ?: continue
                val cell = parts[c].trim()
                if (cell.isNotEmpty()) {
                    val value = cell.toDoubleOrNull()
                    if (value != null) {
                        rowMap[colKey] = value
                    }
                }
            }
        }
    }

    private fun loadBarReductionTable(stream: InputStream) {
        val reader = BufferedReader(InputStreamReader(stream))
        val lines = reader.readLines()
        if (lines.isEmpty()) return

        val headerParts = lines[0].split(",").map { it.trim() }
        for (r in 1 until lines.size) {
            val parts = lines[r].split(",").map { it.trim() }
            val rangeText = parts[0]
            val rangeSub = rangeText.split(" - ")
            if (rangeSub.size != 2) continue
            val lower = rangeSub[0].trim().toDoubleOrNull() ?: continue
            val upper = rangeSub[1].trim().toDoubleOrNull() ?: continue

            val valMap = mutableMapOf<String, Double>()
            for (c in 1 until parts.size) {
                val col = headerParts.getOrNull(c) ?: continue
                val v = parts[c].toDoubleOrNull() ?: continue
                valMap[col] = v
            }
            barRows.add(BarRow(lower, upper, valMap))
        }
    }

    private fun loadCorrectionTable(stream: InputStream) {
        val reader = BufferedReader(InputStreamReader(stream))
        val lines = reader.readLines()
        if (lines.isEmpty()) return

        val headerParts = lines[0].split(",").map { it.trim() }
        for (r in 1 until lines.size) {
            val parts = lines[r].split(",").map { it.trim() }
            val slpKey = parts[0].toDoubleOrNull() ?: continue
            val rowKey = String.format(Locale.US, "%.1f", slpKey)
            val valMap = corrections.getOrPut(rowKey) { mutableMapOf() }
            for (c in 1 until parts.size) {
                val col = headerParts.getOrNull(c) ?: continue
                val v = parts[c].toDoubleOrNull() ?: continue
                valMap[col] = v
            }
        }
    }

    /**
     * Rounds to the nearest 0.5 without banker's rounding.
     */
    fun roundToNearestHalf(value: Double): Double {
        return floor(value * 2.0 + 0.5) / 2.0
    }

    /**
     * Floors odd decimals to the nearest even 0.2 increment (e.g. 17.9 -> 17.8, 15.7 -> 15.6, -14.7 -> -14.6).
     */
    fun roundToNearestPointTwo(value: Double): Double {
        val valStr = String.format(Locale.US, "%.1f", value)
        val parts = valStr.split('.')
        val integerPart = parts[0]
        var dec = parts[1].toInt()
        if (dec % 2 != 0) {
            dec -= 1
        }
        return "$integerPart.$dec".toDouble()
    }

    /**
     * Preprocesses temperatures according to the 800 mb psychrometric rules.
     */
    fun preprocessTemperatures(actualDb: Double, actualWb: Double): Triple<Double, Double, String> {
        return if (actualWb <= 20.2) {
            var lookupWb = roundToNearestPointTwo(actualWb)
            val lookupDb = roundToNearestPointTwo(actualDb)
            if (lookupWb == 20.2) {
                lookupWb = 20.0
            }
            Triple(lookupDb, lookupWb, "standard")
        } else {
            val lookupWb = roundToNearestHalf(actualWb)
            val lookupDb = roundToNearestHalf(actualDb)
            Triple(lookupDb, lookupWb, "high")
        }
    }

    /**
     * Calculates Dew Point, Relative Humidity %, and Vapour Pressure.
     */
    fun calculatePsychrometrics(actualDb: Double, actualWb: Double): PsychroResult {
        if (actualWb > actualDb) {
            return PsychroResult(null, null, null, "Wet bulb more than dry bulb!")
        }

        val (lookupDb, lookupWb, tableType) = preprocessTemperatures(actualDb, actualWb)

        if (tableType == "standard") {
            if (lookupDb < stdDbMin || lookupDb > stdDbMax || lookupWb < stdWbMin || lookupWb > stdWbMax) {
                return PsychroResult(null, null, null, "Temperature out of range!")
            }
        } else {
            if (lookupDb < hiDbMin || lookupDb > hiDbMax || lookupWb < hiWbMin || lookupWb > hiWbMax) {
                return PsychroResult(null, null, null, "Temperature out of range!")
            }
        }

        val targetDpTable = if (tableType == "standard") dpTable else dpHighTable
        val targetRhTable = if (tableType == "standard") rhTable else rhHighTable

        val rowKey = String.format(Locale.US, "%.1f", lookupDb)
        val colKey = String.format(Locale.US, "%.1f", lookupWb)

        val dp = targetDpTable[rowKey]?.get(colKey)
        val rh = targetRhTable[rowKey]?.get(colKey)

        if (dp == null || rh == null) {
            return PsychroResult(null, null, null, "Temperature out of range!")
        }

        val vp = getVpExact(dp)
        return PsychroResult(dp, rh, vp, null)
    }

    /**
     * Vapor Pressure calculation strictly using string manipulation lookup.
     */
    fun getVpExact(dewPoint: Double): Double? {
        if (dewPoint < -60.0 || dewPoint > 50.9) return null

        val dpStr = String.format(Locale.US, "%.1f", dewPoint)
        val parts = dpStr.split(".")
        var rowStr = parts[0]
        val colStr = "0.${parts[1]}"

        if (dewPoint < 0.0 && rowStr == "0") {
            rowStr = "-0"
        }

        val row = vpTable[rowStr] ?: vpTable[rowStr.toIntOrNull()?.toString()]
        return row?.get(colStr)
    }

    /**
     * MSLP calculation strictly matching IMD 800mb bar reduction & correction tables.
     */
    fun calculateMslp(originalDb: Double, originalSlp: Double): Double? {
        val row = barRows.firstOrNull { originalDb >= it.lower && originalDb <= it.upper } ?: return null

        val nearestEvenSlp = floor(originalSlp / 2.0) * 2.0
        val col = nearestEvenSlp.toInt().toString()

        val baseMslp = row.values[col] ?: return null
        val nextCol = (nearestEvenSlp.toInt() + 2).toString()
        val nextMslp = row.values[nextCol] ?: return null

        val mslpDiff = (nextMslp - baseMslp).roundToInt()
        val pressureDiff = ((originalSlp - nearestEvenSlp) * 10.0).roundToInt() / 10.0

        if (pressureDiff == 0.0) {
            return baseMslp
        }

        val pressureDiffKey = String.format(Locale.US, "%.1f", pressureDiff)
        val diffCol = mslpDiff.toString()

        val correction = corrections[pressureDiffKey]?.get(diffCol) ?: return null
        return baseMslp + correction
    }
}
