package com.synoptic.logbook

import android.content.Context
import android.content.SharedPreferences

data class ValidationLimits(
    val enabled: Boolean = true,
    val dbMin: Double = -15.0,
    val dbMax: Double = 50.0,
    val wbMin: Double = -15.0,
    val wbMax: Double = 35.0,
    val slpMin: Double = 800.0,
    val slpMax: Double = 880.0,
    val rainMax: Double = 300.0
) {
    companion object {
        private const val PREF_NAME = "synoptic_validation_limits"
        private const val KEY_ENABLED = "limits_enabled"
        private const val KEY_DB_MIN = "db_min"
        private const val KEY_DB_MAX = "db_max"
        private const val KEY_WB_MIN = "wb_min"
        private const val KEY_WB_MAX = "wb_max"
        private const val KEY_SLP_MIN = "slp_min"
        private const val KEY_SLP_MAX = "slp_max"
        private const val KEY_RAIN_MAX = "rain_max"

        val DEFAULT = ValidationLimits()

        fun load(context: Context): ValidationLimits {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return ValidationLimits(
                enabled = prefs.getBoolean(KEY_ENABLED, true),
                dbMin = prefs.getFloat(KEY_DB_MIN, -15.0f).toDouble(),
                dbMax = prefs.getFloat(KEY_DB_MAX, 50.0f).toDouble(),
                wbMin = prefs.getFloat(KEY_WB_MIN, -15.0f).toDouble(),
                wbMax = prefs.getFloat(KEY_WB_MAX, 35.0f).toDouble(),
                slpMin = prefs.getFloat(KEY_SLP_MIN, 800.0f).toDouble(),
                slpMax = prefs.getFloat(KEY_SLP_MAX, 880.0f).toDouble(),
                rainMax = prefs.getFloat(KEY_RAIN_MAX, 300.0f).toDouble()
            )
        }

        fun save(context: Context, limits: ValidationLimits) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_ENABLED, limits.enabled)
                .putFloat(KEY_DB_MIN, limits.dbMin.toFloat())
                .putFloat(KEY_DB_MAX, limits.dbMax.toFloat())
                .putFloat(KEY_WB_MIN, limits.wbMin.toFloat())
                .putFloat(KEY_WB_MAX, limits.wbMax.toFloat())
                .putFloat(KEY_SLP_MIN, limits.slpMin.toFloat())
                .putFloat(KEY_SLP_MAX, limits.slpMax.toFloat())
                .putFloat(KEY_RAIN_MAX, limits.rainMax.toFloat())
                .apply()
        }
    }
}
