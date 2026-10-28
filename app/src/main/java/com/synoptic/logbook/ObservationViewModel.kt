package com.synoptic.logbook

import android.app.Application
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Shared constants + pure helpers
// ---------------------------------------------------------------------------
val SYNOPTIC_HOURS = listOf("0000", "0300", "0600", "0900", "1200", "1500", "1800", "2100")

val WIND_DIRECTIONS = listOf(
    "Calm (00)", "N (36)", "NNE (02)", "NE (05)", "ENE (07)", "E (09)", "ESE (11)",
    "SE (14)", "SSE (16)", "S (18)", "SSW (20)", "SW (23)", "WSW (25)", "W (27)",
    "WNW (29)", "NW (32)", "NNW (34)"
)

val GROUND_OPTIONS = listOf("Wet", "Soft", "Dry", "Cracked", "Flooded", "Snow covered")

/** Standard Okta codes for the cloud-amount dropdowns (0–8 or obscured "/"). */
val OKTA_OPTIONS = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "/")

/**
 * Rounds the current UTC time to the NEAREST 3-hourly synoptic hour,
 * e.g. 11:03 -> "1200", 08:50 -> "0900", 23:45 -> next day's "0000".
 * Implemented by shifting the clock forward half a window (90 min) and
 * rounding down, which also keeps the observation DATE on the correct day.
 */
fun synopticHourFor(millis: Long): String {
    val shifted = millis + HALF_WINDOW_MS
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = shifted
    val hour = (cal.get(Calendar.HOUR_OF_DAY) / 3) * 3
    return String.format(Locale.US, "%02d00", hour)
}

/** The UTC date (ddMMyyyy) of the observation window the current time belongs to. */
fun observationDateUtc(millis: Long): String = todayUtc(millis + HALF_WINDOW_MS)

/** Half of a 3-hour synoptic window: the "nearest" rounding offset. */
private const val HALF_WINDOW_MS = 90L * 60L * 1000L

fun todayUtc(millis: Long): String {
    // Date_month_year identification format: ddMMyyyy (e.g. 10092026)
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    return String.format(
        Locale.US, "%02d%02d%04d",
        cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)
    )
}

/** a - b formatted to exactly 1 decimal place with meteorological tendency sign (e.g. "+1.2", "-0.8", "0.0"). Empty while either input is missing. */
fun formatDifference(a: String, b: String): String {
    val x = a.trim().toDoubleOrNull() ?: return ""
    val y = b.trim().toDoubleOrNull() ?: return ""
    val diff = x - y
    return if (diff > 0.0) String.format(Locale.US, "+%.1f", diff)
           else String.format(Locale.US, "%.1f", diff)
}

// ---------------------------------------------------------------------------
// ViewModel: owns ALL sheet state + persistence logic. The UI only observes.
// ---------------------------------------------------------------------------

/** Simple state-based router between the two screens. */
enum class AppScreen { FORM, HISTORY }

class ObservationViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getInstance(app).observationDao()

    // ---------- Top capsule: synoptic hour & date ----------
    val manualHour = mutableStateOf<String?>(null)
    val manualDate = mutableStateOf<String?>(null)
    val showHourPicker = mutableStateOf(false)
    val nowMillis = mutableStateOf(System.currentTimeMillis())

    // ---------- Section 1 : left column ----------
    val dryBulb = mutableStateOf("")
    val wetBulb = mutableStateOf("")
    val dewPoint = mutableStateOf("")
    val rh = mutableStateOf("")
    val vapourPressure = mutableStateOf("")

    // ---------- Section 1 : right column ----------
    val windSpeed = mutableStateOf("")
    val windDirection = mutableStateOf("")
    val totalCloud = mutableStateOf("")
    val visibility = mutableStateOf("")

    // ---------- Section 1 : type of cloud ----------
    val lowCloudType = mutableStateOf("")
    val mediumCloudType = mutableStateOf("")
    val highCloudType = mutableStateOf("")
    val lowCloudAmount = mutableStateOf("")
    val mediumCloudAmount = mutableStateOf("")
    val highCloudAmount = mutableStateOf("")

    // ---------- Section 2 : pressure ----------
    val slp = mutableStateOf("")
    val pastSlp = mutableStateOf("")
    val mslp = mutableStateOf("")

    // ---------- Section 3 : max/min temps ----------
    val maxTemp = mutableStateOf("")
    val maxTempAfterSet = mutableStateOf("")
    val minTemp = mutableStateOf("")
    val minTempAfterSet = mutableStateOf("")

    // ---------- Section 4 : rain & ground ----------
    val rain = mutableStateOf("")
    val ground = mutableStateOf("")

    // ---------- Section 5 : evaporation & water temp ----------
    val evaporation = mutableStateOf("")
    val waterTemp = mutableStateOf("")

    val showWindPicker = mutableStateOf(false)
    val showGroundPicker = mutableStateOf(false)
    val showLowOktaPicker = mutableStateOf(false)
    val showMedOktaPicker = mutableStateOf(false)
    val showHighOktaPicker = mutableStateOf(false)
    val showTotalOktaPicker = mutableStateOf(false)
    val saving = mutableStateOf(false)
    val saveStatus = mutableStateOf<String?>(null) // subtle "Saved" indicator

    // ---------- Record navigation ----------
    val records: StateFlow<List<WeatherObservationEntity>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val viewIndex = mutableStateOf<Int?>(null)      // null = blank "NEW" sheet
    val editingId = mutableStateOf<Long?>(null)     // id of the record being edited
    val editingDate = mutableStateOf<String?>(null) // original date of that record

    // ---------- Derived state (recomputed on each read/composition) ----------
    /** AUTO snap: rounds the live UTC clock down to the closest 3-hourly synoptic hour. */
    val autoHour: String get() = synopticHourFor(nowMillis.value)
    val isAuto: Boolean get() = manualHour.value == null
    val selectedHour: String get() = manualHour.value ?: autoHour
    val isAutoDate: Boolean get() = manualDate.value == null
    val selectedDate: String get() = manualDate.value ?: editingDate.value ?: observationDateUtc(nowMillis.value)

    var lastSavedSignature: String? = null

    fun currentSignature(): String {
        val fields = listOf(
            dryBulb, wetBulb, dewPoint, rh, vapourPressure,
            windSpeed, windDirection, totalCloud, visibility,
            lowCloudType, mediumCloudType, highCloudType,
            lowCloudAmount, mediumCloudAmount, highCloudAmount,
            slp, pastSlp, mslp, maxTemp, maxTempAfterSet,
            minTemp, minTempAfterSet, rain, ground, evaporation, waterTemp
        )
        return fields.joinToString("\u0000") { it.value } + "\u0000" + selectedHour + "\u0000" + selectedDate
    }

    // Main synoptic hours: the only slots where max/min temps and
    // evaporation/water readings are taken (Sections 3 & 5).
    val isMainSynoptic: Boolean get() = selectedHour == "0300" || selectedHour == "1200"

    /** The ONLY auto box: slp - pastSlp. */
    val pressureChange: String get() = formatDifference(slp.value, pastSlp.value)

    val viewing: WeatherObservationEntity?
        get() = viewIndex.value?.let { records.value.getOrNull(it) }

    val posLabel: String
        get() {
            val idx = viewIndex.value
            val v = viewing
            return if (idx == null || v == null) "NEW"
            else "${records.value.size - idx}/${records.value.size} · ${v.synopticHourUtc}"
        }

    val canGoOlder: Boolean
        get() {
            val idx = viewIndex.value
            val list = records.value
            return list.isNotEmpty() && (idx == null || idx < list.lastIndex)
        }

    val canGoNewer: Boolean
        get() = viewIndex.value != null

    // ---------- Navigation + theme (Milestone 7) ----------
    val currentScreen = mutableStateOf(AppScreen.FORM)
    val isDarkTheme = mutableStateOf(false)

    fun openHistory() { currentScreen.value = AppScreen.HISTORY }

    fun openForm() { currentScreen.value = AppScreen.FORM }

    val showDeleteConfirm = mutableStateOf(false)

    /**
     * Deletes the record currently attached to the sheet (or the one being viewed)
     * and resets the UI to a blank "NEW" sheet. Safe: guarded by a confirm dialog.
     */
    fun deleteCurrentRecord() {
        val id = editingId.value ?: return
        viewModelScope.launch {
            dao.deleteById(id)
            showDeleteConfirm.value = false
            viewIndex.value = null
            clearSheet()
            saveStatus.value = null
        }
    }

    /** From the History list: load the tapped record into the sheet and return to the form. */
    fun openRecord(e: WeatherObservationEntity) {
        viewIndex.value = records.value.indexOfFirst { it.id == e.id }.takeIf { it >= 0 }
        loadRecord(e)
        currentScreen.value = AppScreen.FORM
    }

    // ---------- Over-The-Air (OTA) Updates ----------
    val currentAppVersion: String = BuildConfig.VERSION_NAME
    val updateInfo = mutableStateOf<UpdateInfo?>(null)
    val isCheckingUpdate = mutableStateOf(false)
    val downloadProgress = mutableStateOf<Float?>(null)
    val showUpdateDialog = mutableStateOf(false)
    val updateStatusMessage = mutableStateOf<String?>(null)

    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            isCheckingUpdate.value = true
            updateStatusMessage.value = null
            try {
                val info = UpdateManager.checkLatestRelease(currentAppVersion)
                if (info != null) {
                    updateInfo.value = info
                    showUpdateDialog.value = true
                } else if (isManual) {
                    updateStatusMessage.value = "You are on the latest version ($currentAppVersion)"
                }
            } catch (e: Exception) {
                if (isManual) {
                    updateStatusMessage.value = "Failed to check for updates: ${e.localizedMessage}"
                }
            } finally {
                isCheckingUpdate.value = false
            }
        }
    }

    fun startDownloadAndUpdate(context: Context) {
        val info = updateInfo.value ?: return
        viewModelScope.launch {
            downloadProgress.value = 0f
            val apkFile = UpdateManager.downloadApk(
                context = context,
                apkUrl = info.apkUrl,
                fileName = info.apkFileName,
                onProgress = { progress ->
                    downloadProgress.value = progress
                }
            )
            downloadProgress.value = null
            if (apkFile != null) {
                showUpdateDialog.value = false
                UpdateManager.installApk(context, apkFile)
            } else {
                updateStatusMessage.value = "Download failed. Please check internet connection."
            }
        }
    }

    init {
        // Aggressive update check on app launch
        checkForUpdates(isManual = false)

        // Clock ticker: while the hour is AUTO, keep nowMillis fresh so the
        // auto-snap hour follows the device clock (every 30 s).
        viewModelScope.launch {
            while (true) {
                if (manualHour.value == null) {
                    nowMillis.value = System.currentTimeMillis()
                }
                delay(30_000)
            }
        }

        // ---------- Auto-save (debounced 500 ms) ----------
        // Any change to any field (or the synoptic hour) schedules a save. Rapid
        // keystrokes restart the 500 ms wait via collectLatest, so the DB is
        // written once typing pauses — not on every keystroke.
        viewModelScope.launch {
            snapshotFlow {
                val sig = currentSignature()
                val fields = listOf(
                    dryBulb, wetBulb, dewPoint, rh, vapourPressure,
                    windSpeed, windDirection, totalCloud, visibility,
                    lowCloudType, mediumCloudType, highCloudType,
                    lowCloudAmount, mediumCloudAmount, highCloudAmount,
                    slp, pastSlp, mslp, maxTemp, maxTempAfterSet,
                    minTemp, minTempAfterSet, rain, ground, evaporation, waterTemp
                )
                sig to fields.any { it.value.isNotBlank() }
            }
                .drop(1) // ignore the values captured at startup
                .collectLatest { (signature, hasData) ->
                    if (!hasData) return@collectLatest // never persist a blank sheet
                    if (signature == lastSavedSignature) return@collectLatest // skip if unchanged
                    delay(500)                        // debounce
                    doSave()
                }
        }
    }

    // ---------- Record navigation ----------
    fun loadRecord(e: WeatherObservationEntity) {
        dryBulb.value = e.dryBulbC
        wetBulb.value = e.wetBulbC
        dewPoint.value = e.dewPointC
        rh.value = e.relativeHumidityPct
        vapourPressure.value = e.vapourPressure
        windSpeed.value = e.windSpeedKt
        windDirection.value = e.windDirectionCode
        totalCloud.value = e.totalCloudAmount
        visibility.value = e.visibilityCode
        lowCloudType.value = e.lowCloudType
        mediumCloudType.value = e.mediumCloudType
        highCloudType.value = e.highCloudType
        lowCloudAmount.value = e.lowCloudAmount
        mediumCloudAmount.value = e.mediumCloudAmount
        highCloudAmount.value = e.highCloudAmount
        slp.value = e.slp
        pastSlp.value = e.pastSlp
        mslp.value = e.mslp
        maxTemp.value = e.maxTempC
        maxTempAfterSet.value = e.maxTempAfterSetC
        minTemp.value = e.minTempC
        minTempAfterSet.value = e.minTempAfterSetC
        rain.value = e.rainMm
        ground.value = e.groundCondition
        evaporation.value = e.evaporationMm
        waterTemp.value = e.waterTempC
        manualHour.value = e.synopticHourUtc
        manualDate.value = e.dateUtc
        editingId.value = e.id
        editingDate.value = e.dateUtc
        lastSavedSignature = currentSignature()
    }

    fun clearSheet() {
        listOf(
            dryBulb, wetBulb, dewPoint, rh, vapourPressure,
            windSpeed, windDirection, totalCloud, visibility,
            lowCloudType, mediumCloudType, highCloudType,
            lowCloudAmount, mediumCloudAmount, highCloudAmount,
            slp, pastSlp, mslp,
            maxTemp, maxTempAfterSet, minTemp, minTempAfterSet,
            rain, ground, evaporation, waterTemp
        ).forEach { it.value = "" }
        manualHour.value = null
        manualDate.value = null
        editingId.value = null
        editingDate.value = null
        lastSavedSignature = null
        saveStatus.value = null
        nowMillis.value = System.currentTimeMillis()
    }

    fun selectHour(hour: String?) {
        manualHour.value = hour
        if (hour == null) nowMillis.value = System.currentTimeMillis()
        viewModelScope.launch {
            val targetDate = selectedDate
            val existing = dao.findByDateAndHour(targetDate, selectedHour)
            if (existing != null) {
                viewIndex.value = records.value.indexOfFirst { it.id == existing.id }.takeIf { it >= 0 }
                loadRecord(existing)
            } else if (editingId.value != null) {
                editingId.value = null
                editingDate.value = targetDate
                lastSavedSignature = null
            }
        }
    }

    fun setManualDate(dateUtc: String?) {
        manualDate.value = dateUtc
        viewModelScope.launch {
            val targetDate = selectedDate
            val existing = dao.findByDateAndHour(targetDate, selectedHour)
            if (existing != null) {
                viewIndex.value = records.value.indexOfFirst { it.id == existing.id }.takeIf { it >= 0 }
                loadRecord(existing)
            } else if (editingId.value != null) {
                editingId.value = null
                editingDate.value = targetDate
                lastSavedSignature = null
            }
        }
    }

    fun goOlder() {
        val next = (viewIndex.value ?: -1) + 1
        val rec = records.value.getOrNull(next) ?: return
        viewIndex.value = next
        loadRecord(rec)
    }

    fun goNewer() {
        val cur = viewIndex.value ?: return
        if (cur == 0) {
            viewIndex.value = null
            clearSheet()
        } else {
            val rec = records.value.getOrNull(cur - 1) ?: return
            viewIndex.value = cur - 1
            loadRecord(rec)
        }
    }

    // ---------- Persistence ----------
    fun doSave() {
        viewModelScope.launch {
            saving.value = true
            saveStatus.value = "Saving…"
            try {
                val sheetDate = selectedDate
                val existing = dao.findByDateAndHour(sheetDate, selectedHour)
                val targetId = when {
                    existing != null -> existing.id
                    editingDate.value == sheetDate && manualHour.value == selectedHour -> editingId.value ?: 0L
                    else -> 0L
                }

                // Hidden-temps guard: sections 3 & 5 only apply at 0300/1200 UTC.
                val maxTempV = if (isMainSynoptic) maxTemp.value else ""
                val maxTempAfterSetV = if (isMainSynoptic) maxTempAfterSet.value else ""
                val minTempV = if (isMainSynoptic) minTemp.value else ""
                val minTempAfterSetV = if (isMainSynoptic) minTempAfterSet.value else ""
                val evaporationV = if (isMainSynoptic) evaporation.value else ""
                val waterTempV = if (isMainSynoptic) waterTemp.value else ""

                val savedId = dao.insert(
                    WeatherObservationEntity(
                        id = targetId,
                        dateUtc = sheetDate,
                        synopticHourUtc = selectedHour,
                        dryBulbC = dryBulb.value,
                        wetBulbC = wetBulb.value,
                        dewPointC = dewPoint.value,
                        relativeHumidityPct = rh.value,
                        vapourPressure = vapourPressure.value,
                        windSpeedKt = windSpeed.value,
                        windDirectionCode = windDirection.value,
                        totalCloudAmount = totalCloud.value,
                        visibilityCode = visibility.value,
                        lowCloudType = lowCloudType.value,
                        mediumCloudType = mediumCloudType.value,
                        highCloudType = highCloudType.value,
                        lowCloudAmount = lowCloudAmount.value,
                        mediumCloudAmount = mediumCloudAmount.value,
                        highCloudAmount = highCloudAmount.value,
                        slp = slp.value,
                        pastSlp = pastSlp.value,
                        pressureChange = pressureChange,
                        mslp = mslp.value,
                        maxTempC = maxTempV,
                        maxTempAfterSetC = maxTempAfterSetV,
                        minTempC = minTempV,
                        minTempAfterSetC = minTempAfterSetV,
                        rainMm = rain.value,
                        groundCondition = ground.value,
                        evaporationMm = evaporationV,
                        waterTempC = waterTempV
                    )
                )
                editingId.value = savedId
                editingDate.value = sheetDate
                lastSavedSignature = currentSignature()

                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                val t = String.format(
                    Locale.US, "%02d:%02d",
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)
                )
                saveStatus.value = "Saved $t UTC"
            } finally {
                saving.value = false
            }
        }
    }
}