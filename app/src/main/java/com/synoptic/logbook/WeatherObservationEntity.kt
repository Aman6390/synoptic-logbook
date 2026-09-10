package com.synoptic.logbook

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One observation. A record is strictly identified by the combination of
 * [dateUtc] (ddMMyyyy) and [synopticHourUtc] (e.g. "0900") — enforced by the
 * composite unique index below, so duplicate "twins" are impossible at the
 * database level as well as in the ViewModel guard.
 */
@Entity(
    tableName = "observations",
    indices = [
        Index(
            value = ["dateUtc", "synopticHourUtc"],
            name = "index_observations_dateUtc_synopticHourUtc",
            unique = true
        )
    ]
)
data class WeatherObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    // Header
    val dateUtc: String = "",
    val synopticHourUtc: String = "",

    // ---- Section 1 : left column ----
    val dryBulbC: String = "",
    val wetBulbC: String = "",
    val dewPointC: String = "",
    val relativeHumidityPct: String = "",
    val vapourPressure: String = "",

    // ---- Section 1 : right column ----
    val windSpeedKt: String = "",       // whole numbers only
    val windDirectionCode: String = "", // e.g. "E (09)"
    val totalCloudAmount: String = "",  // 0-8 or "/"
    val visibilityCode: String = "",    // 90-99

    // ---- Section 1 : type of cloud ----
    val lowCloudType: String = "",
    val mediumCloudType: String = "",
    val highCloudType: String = "",
    val lowCloudAmount: String = "",    // 0-8 or "/"
    val mediumCloudAmount: String = "",
    val highCloudAmount: String = "",

    // ---- Section 2 : pressure ----
    val slp: String = "",
    val pastSlp: String = "",
    val pressureChange: String = "",    // auto: slp - pastSlp (1 decimal)
    val mslp: String = "",

    // ---- Section 3 : max/min temps ----
    val maxTempC: String = "",
    val maxTempAfterSetC: String = "",
    val minTempC: String = "",
    val minTempAfterSetC: String = "",

    // ---- Section 4 : rain & ground ----
    val rainMm: String = "",
    val groundCondition: String = "",   // dropdown: Wet/Soft/Dry/Cracked/Flooded/Snow covered

    // ---- Section 5 : evaporation & water temp ----
    val evaporationMm: String = "",
    val waterTempC: String = ""
)