package com.synoptic.logbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Focus chain: every input cell is linked top-to-bottom. The requested serial
// sub-paths are enforced inside this single order:
//   P1: DRY -> WET
//   P4: DEW -> RH -> VAPOUR
//   P2: WIND_SPEED -> WIND_DIR -> TOTAL_CLOUD -> VIS -> LOW_CT -> MED_CT ->
//       HIGH_CT -> LOW_OKTA -> MED_OKTA -> HIGH_OKTA
//   P3: SLP -> MSLP          (Past SLP follows MSLP, then the next section)
// Section 3 & 5 fields are skipped when they are hidden (non-main hours).
// ---------------------------------------------------------------------------
enum class FKey { DRY, WET, DEW, RH, VAPOUR, WIND_SPEED, WIND_DIR, TOTAL_CLOUD, VIS, LOW_CT, MED_CT, HIGH_CT, LOW_OKTA, MED_OKTA, HIGH_OKTA, SLP, MSLP, PAST_SLP, MAX, MAX_SET, MIN, MIN_SET, RAIN, GROUND, EVAP, WATER }

private val FOCUS_CHAIN = listOf(
    FKey.DRY, FKey.WET, FKey.DEW, FKey.RH, FKey.VAPOUR,
    FKey.WIND_SPEED, FKey.WIND_DIR, FKey.TOTAL_CLOUD, FKey.VIS,
    FKey.LOW_CT, FKey.MED_CT, FKey.HIGH_CT,
    FKey.LOW_OKTA, FKey.MED_OKTA, FKey.HIGH_OKTA,
    FKey.SLP, FKey.MSLP, FKey.PAST_SLP,
    FKey.MAX, FKey.MAX_SET, FKey.MIN, FKey.MIN_SET,
    FKey.RAIN, FKey.GROUND,
    FKey.EVAP, FKey.WATER
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ObservationViewModel = viewModel()
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            // System back press / gesture navigation handlers
            BackHandler(enabled = drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }
            BackHandler(enabled = vm.currentScreen.value == AppScreen.HISTORY) {
                vm.openForm()
            }

            // Light / Dark theme from the drawer toggle
            MaterialTheme(
                colorScheme = if (vm.isDarkTheme.value) darkColorScheme() else lightColorScheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                // Drawer header
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Synoptic Pad",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Weather observation logbook",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // History navigation target
                                NavigationDrawerItem(
                                    label = { Text("History") },
                                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                                    selected = vm.currentScreen.value == AppScreen.HISTORY,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        vm.openHistory()
                                    }
                                )
                                // Light / Dark theme toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Dark theme",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Switch(
                                        checked = vm.isDarkTheme.value,
                                        onCheckedChange = { vm.setDarkTheme(it) }
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                // Check for updates option in settings
                                NavigationDrawerItem(
                                    label = {
                                        Column {
                                            Text("Check for updates")
                                            Text(
                                                "Current: v${vm.currentAppVersion}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            vm.updateStatusMessage.value?.let { msg ->
                                                Text(
                                                    msg,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    icon = {
                                        if (vm.isCheckingUpdate.value) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(Icons.Filled.Refresh, contentDescription = "Check for updates")
                                        }
                                    },
                                    selected = false,
                                    onClick = {
                                        vm.checkForUpdates(isManual = true)
                                    }
                                )
                            }
                        }
                    ) {
                        when (vm.currentScreen.value) {
                            AppScreen.FORM -> LogbookScreen(vm) {
                                scope.launch { drawerState.open() }
                            }
                            AppScreen.HISTORY -> HistoryScreen(vm) {
                                scope.launch { drawerState.open() }
                            }
                        }
                    }

                    // Aggressive OTA Update Dialog
                    if (vm.showUpdateDialog.value && vm.updateInfo.value != null) {
                        val info = vm.updateInfo.value!!
                        val progress = vm.downloadProgress.value
                        val isDownloading = progress != null

                        AlertDialog(
                            onDismissRequest = {
                                if (!isDownloading) vm.showUpdateDialog.value = false
                            },
                            title = {
                                Text(
                                    text = "Update Available!",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "A new version (${info.version}) is available.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (info.changelog.isNotBlank()) {
                                        Text(
                                            info.changelog,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isDownloading) {
                                        Spacer(Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = progress ?: 0f,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            "Downloading: ${((progress ?: 0f) * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = { vm.startDownloadAndUpdate(context) },
                                    enabled = !isDownloading
                                ) {
                                    Text(
                                        "Update Now!",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { vm.showUpdateDialog.value = false },
                                    enabled = !isDownloading
                                ) {
                                    Text("Later")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogbookScreen(vm: ObservationViewModel, onMenuClick: () -> Unit) {
    // MVVM: all state + logic lives in ObservationViewModel; this composable
    // only observes it. Aliases keep the UI body readable.
    val showHourPicker = vm.showHourPicker
    val showWindPicker = vm.showWindPicker
    val showGroundPicker = vm.showGroundPicker
    val showLowOktaPicker = vm.showLowOktaPicker
    val showMedOktaPicker = vm.showMedOktaPicker
    val showHighOktaPicker = vm.showHighOktaPicker
    val showTotalOktaPicker = vm.showTotalOktaPicker

    val dryBulb = vm.dryBulb
    val wetBulb = vm.wetBulb
    val dewPoint = vm.dewPoint
    val rh = vm.rh
    val vapourPressure = vm.vapourPressure
    val windSpeed = vm.windSpeed
    val windDirection = vm.windDirection
    val totalCloud = vm.totalCloud
    val visibility = vm.visibility
    val lowCloudType = vm.lowCloudType
    val mediumCloudType = vm.mediumCloudType
    val highCloudType = vm.highCloudType
    val lowCloudAmount = vm.lowCloudAmount
    val mediumCloudAmount = vm.mediumCloudAmount
    val highCloudAmount = vm.highCloudAmount
    val slp = vm.slp
    val pastSlp = vm.pastSlp
    val mslp = vm.mslp
    val maxTemp = vm.maxTemp
    val maxTempAfterSet = vm.maxTempAfterSet
    val minTemp = vm.minTemp
    val minTempAfterSet = vm.minTempAfterSet
    val rain = vm.rain
    val ground = vm.ground
    val evaporation = vm.evaporation
    val waterTemp = vm.waterTemp

    val saveStatus = vm.saveStatus

    val records = vm.records.collectAsState().value
    val viewIndex = vm.viewIndex
    val editingId = vm.editingId

    val autoHour = vm.autoHour
    val isAuto = vm.isAuto
    val selectedHour = vm.selectedHour
    val isMainSynoptic = vm.isMainSynoptic
    val pressureChange = vm.pressureChange
    val posLabel = vm.posLabel
    val canGoOlder = vm.canGoOlder
    val canGoNewer = vm.canGoNewer

    // ---------- Focus chain (auto focus shifting) ----------
    val focusManager = LocalFocusManager.current
    val requesters = remember { FKey.values().associateWith { FocusRequester() } }
    val mainOnlyKeys = remember {
        setOf(FKey.MAX, FKey.MAX_SET, FKey.MIN, FKey.MIN_SET, FKey.EVAP, FKey.WATER)
    }
    val visibleChain = remember(isMainSynoptic) {
        FOCUS_CHAIN.filter { isMainSynoptic || it !in mainOnlyKeys }
    }
    val lastKey = visibleChain.last()

    /** Move focus to the next visible cell; open picker dialogs automatically. */
    fun advance(from: FKey) {
        val idx = visibleChain.indexOf(from)
        val nextKey = visibleChain.getOrNull(idx + 1)
        if (nextKey == null) {
            focusManager.clearFocus() // End of chain: close the keyboard
        } else {
            requesters[nextKey]?.requestFocus()
            when (nextKey) {
                FKey.WIND_DIR -> showWindPicker.value = true
                FKey.GROUND -> showGroundPicker.value = true
                FKey.LOW_OKTA -> showLowOktaPicker.value = true
                FKey.MED_OKTA -> showMedOktaPicker.value = true
                FKey.HIGH_OKTA -> showHighOktaPicker.value = true
                FKey.TOTAL_CLOUD -> showTotalOktaPicker.value = true
                else -> Unit
            }
        }
    }

    /** Focus link for a cell: requester + onNext (null onNext => ImeAction.Done). */
    fun link(key: FKey): Pair<FocusRequester?, (() -> Unit)?> =
        if (key == lastKey) requesters[key] to null
        else requesters[key] to { advance(key) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                    }
                    IconButton(
                        onClick = { vm.showDeleteConfirm.value = true },
                        enabled = editingId.value != null // only when a saved record is attached
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete this observation")
                    }
                }
            }
            SynopticHourCapsule(
                hourText = "$selectedHour UTC",
                isAuto = isAuto,
                onClick = { showHourPicker.value = true }
            )
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.goOlder() }, enabled = canGoOlder) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Older entry")
                    }
                    Text(
                        posLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { vm.goNewer() }, enabled = canGoNewer) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Newer entry")
                    }
                }
            }
        }
        // Subtle auto-save indicator lives in the header to save vertical space
        Text(
            saveStatus.value ?: "Auto-saves as you type",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // ---------------- SECTION 1 ----------------
        SectionCard(title = "SURFACE OBSERVATION") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DecimalField("Dry bulb", dryBulb, Modifier.weight(1f), link(FKey.DRY))
                DecimalField("Wet bulb", wetBulb, Modifier.weight(1f), link(FKey.WET))
                DecimalField("Dew pt", dewPoint, Modifier.weight(1f), link(FKey.DEW))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DecimalField("RH %", rh, Modifier.weight(1f), link(FKey.RH))
                DecimalField("Vapour", vapourPressure, Modifier.weight(1f), link(FKey.VAPOUR))
                VisibilityField(visibility, Modifier.weight(1f), link(FKey.VIS))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IntegerField("Wind kt", windSpeed, Modifier.weight(1f), link(FKey.WIND_SPEED))
                PickerField(
                    "Wind dir", windDirection, Modifier.weight(1f), link(FKey.WIND_DIR)
                ) {
                    showWindPicker.value = true
                }
                PickerField("Cloud amt", totalCloud, Modifier.weight(1f), link(FKey.TOTAL_CLOUD)) {
                    showTotalOktaPicker.value = true
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextFieldCell("Low cloud", lowCloudType, Modifier.weight(1f), link(FKey.LOW_CT))
                TextFieldCell("Med cloud", mediumCloudType, Modifier.weight(1f), link(FKey.MED_CT))
                TextFieldCell("High cloud", highCloudType, Modifier.weight(1f), link(FKey.HIGH_CT))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PickerField("Low okta", lowCloudAmount, Modifier.weight(1f), link(FKey.LOW_OKTA)) {
                    showLowOktaPicker.value = true
                }
                PickerField("Med okta", mediumCloudAmount, Modifier.weight(1f), link(FKey.MED_OKTA)) {
                    showMedOktaPicker.value = true
                }
                PickerField("High okta", highCloudAmount, Modifier.weight(1f), link(FKey.HIGH_OKTA)) {
                    showHighOktaPicker.value = true
                }
            }
        }

        // ---------------- SECTION 2 ----------------
        SectionCard(title = "PRESSURE (hPa)") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DecimalField("SLP", slp, Modifier.weight(1f), link(FKey.SLP))
                DecimalField("Past SLP", pastSlp, Modifier.weight(1f), link(FKey.PAST_SLP))
                OutlinedTextField(
                    value = pressureChange,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Diff") },
                    singleLine = true,
                    textStyle = CellTextStyle,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                DecimalField("MSLP", mslp, Modifier.weight(1f), link(FKey.MSLP))
            }
        }

        // ---------------- SECTION 3 (0300 & 1200 UTC only) ----------------
        if (isMainSynoptic) {
            SectionCard(title = "MAX / MIN TEMPS (°C)") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DecimalField("Max", maxTemp, Modifier.weight(1f), link(FKey.MAX))
                    DecimalField("Max set", maxTempAfterSet, Modifier.weight(1f), link(FKey.MAX_SET))
                    DecimalField("Min", minTemp, Modifier.weight(1f), link(FKey.MIN))
                    DecimalField("Min set", minTempAfterSet, Modifier.weight(1f), link(FKey.MIN_SET))
                }
            }
        }

        // ---------------- SECTION 4 ----------------
        SectionCard(title = "RAIN & GROUND") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DecimalField("Rain (mm)", rain, Modifier.weight(1f), link(FKey.RAIN))
                PickerField("Ground", ground, Modifier.weight(1f), link(FKey.GROUND)) {
                    showGroundPicker.value = true
                }
            }
        }

        // ---------------- SECTION 5 (0300 & 1200 UTC only) ----------------
        if (isMainSynoptic) {
            SectionCard(title = "EVAPORATION & WATER") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DecimalField("Evaporation (mm)", evaporation, Modifier.weight(1f), link(FKey.EVAP))
                    DecimalField("Water temp", waterTemp, Modifier.weight(1f), link(FKey.WATER))
                }
            }
        }
    }

    // ---------------- Delete confirmation ----------------
    if (vm.showDeleteConfirm.value) {
        val vi = viewIndex.value
        val v = vi?.let { records.getOrNull(it) }
        AlertDialog(
            onDismissRequest = { vm.showDeleteConfirm.value = false },
            title = { Text("Delete this observation?") },
            text = {
                Text(
                    if (v != null)
                        "${v.dateUtc} · ${v.synopticHourUtc} UTC will be permanently removed."
                    else "This observation will be permanently removed."
                )
            },
            confirmButton = {
                TextButton(onClick = { vm.deleteCurrentRecord() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showDeleteConfirm.value = false }) { Text("Cancel") }
            }
        )
    }

    // ---------------- Dialogs ----------------
    if (showHourPicker.value) {
        HourPickerDialog(
            autoHour = autoHour,
            selectedHour = selectedHour,
            isAuto = isAuto,
            selectedDate = vm.selectedDate,
            isAutoDate = vm.isAutoDate,
            onPickHour = { picked ->
                vm.selectHour(picked)
                showHourPicker.value = false
            },
            onPickDate = { pickedDate ->
                vm.setManualDate(pickedDate)
            },
            onDismiss = { showHourPicker.value = false }
        )
    }
    if (showWindPicker.value) {
        SelectionDialog(
            title = "Wind direction",
            options = WIND_DIRECTIONS,
            selected = windDirection.value,
            onSelect = {
                windDirection.value = it
                showWindPicker.value = false
                advance(FKey.WIND_DIR) // continue the focus chain after picking
            },
            onDismiss = { showWindPicker.value = false }
        )
    }
    if (showGroundPicker.value) {
        SelectionDialog(
            title = "Ground condition",
            options = GROUND_OPTIONS,
            selected = ground.value,
            onSelect = {
                ground.value = it
                showGroundPicker.value = false
                advance(FKey.GROUND) // continue the focus chain after picking
            },
            onDismiss = { showGroundPicker.value = false }
        )
    }
    if (showTotalOktaPicker.value) {
        SelectionDialog(
            title = "Total cloud amount (oktas)",
            options = OKTA_OPTIONS,
            selected = totalCloud.value,
            onSelect = {
                totalCloud.value = it
                showTotalOktaPicker.value = false
                advance(FKey.TOTAL_CLOUD)
            },
            onDismiss = { showTotalOktaPicker.value = false }
        )
    }
    if (showLowOktaPicker.value) {
        SelectionDialog(
            title = "Low cloud amount (oktas)",
            options = OKTA_OPTIONS,
            selected = lowCloudAmount.value,
            onSelect = {
                lowCloudAmount.value = it
                showLowOktaPicker.value = false
                advance(FKey.LOW_OKTA) // continue the focus chain after picking
            },
            onDismiss = { showLowOktaPicker.value = false }
        )
    }
    if (showMedOktaPicker.value) {
        SelectionDialog(
            title = "Medium cloud amount (oktas)",
            options = OKTA_OPTIONS,
            selected = mediumCloudAmount.value,
            onSelect = {
                mediumCloudAmount.value = it
                showMedOktaPicker.value = false
                advance(FKey.MED_OKTA)
            },
            onDismiss = { showMedOktaPicker.value = false }
        )
    }
    if (showHighOktaPicker.value) {
        SelectionDialog(
            title = "High cloud amount (oktas)",
            options = OKTA_OPTIONS,
            selected = highCloudAmount.value,
            onSelect = {
                highCloudAmount.value = it
                showHighOktaPicker.value = false
                advance(FKey.HIGH_OKTA)
            },
            onDismiss = { showHighOktaPicker.value = false }
        )
    }
}

// ---------------------------------------------------------------------------
// History screen (Milestone 7)
// ---------------------------------------------------------------------------
@Composable
private fun HistoryScreen(vm: ObservationViewModel, onMenuClick: () -> Unit) {
    val records = vm.records.collectAsState().value
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Header bar with hamburger + title + export + back
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Open menu")
            }
            Text(
                "History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (records.isNotEmpty()) {
                IconButton(onClick = { vm.exportCsv(context) }) {
                    Icon(Icons.Filled.Share, contentDescription = "Export to CSV")
                }
            }
            IconButton(onClick = { vm.openForm() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back to form")
            }
        }
        Text(
            "${records.size} saved observation(s) · tap one to open it in the form",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(4.dp))

        if (records.isEmpty()) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "No observations saved yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(records, key = { it.id }) { rec ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.openRecord(rec) }
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    rec.dateUtc,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${rec.synopticHourUtc} UTC",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                buildString {
                                    append("MSLP ")
                                    append(rec.mslp.ifBlank { "—" })
                                    append(" hPa · Wind ")
                                    append(rec.windSpeedKt.ifBlank { "–" })
                                    append(" kt @ ")
                                    append(rec.windDirectionCode.ifBlank { "–" })
                                    append(" · Temp ")
                                    append(rec.dryBulbC.ifBlank { "–" })
                                    append(" °C")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Building blocks
// ---------------------------------------------------------------------------
/** Shared input style for every cell: bold 15sp monospace for dense legibility. */
private val CellTextStyle = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily.Monospace
)

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

/** Free-text field (used for cloud types, e.g. Cu, Sc, St). */
@Composable
private fun TextFieldCell(
    label: String,
    state: MutableState<String>,
    modifier: Modifier = Modifier,
    focusLink: Pair<FocusRequester?, (() -> Unit)?>? = null
) {
    val fm = LocalFocusManager.current
    OutlinedTextField(
        value = state.value,
        onValueChange = { state.value = it },
        label = { Text(label) },
        singleLine = true,
        textStyle = CellTextStyle,
        keyboardOptions = KeyboardOptions(
            imeAction = if (focusLink?.second != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusLink?.second?.invoke() },
            onDone = { fm.clearFocus() }
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(focusLink?.first?.let { Modifier.focusRequester(it) } ?: Modifier)
    )
}

/** One-decimal numeric field, e.g. 36.2 (allows a leading minus for temps). */
@Composable
private fun DecimalField(
    label: String,
    state: MutableState<String>,
    modifier: Modifier = Modifier,
    focusLink: Pair<FocusRequester?, (() -> Unit)?>? = null
) {
    val fm = LocalFocusManager.current
    OutlinedTextField(
        value = state.value,
        onValueChange = { raw ->
            if (raw.isEmpty() || Regex("^-?\\d*(\\.\\d?)?$").matches(raw)) state.value = raw
        },
        label = { Text(label) },
        singleLine = true,
        textStyle = CellTextStyle,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (focusLink?.second != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusLink?.second?.invoke() },
            onDone = { fm.clearFocus() }
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(focusLink?.first?.let { Modifier.focusRequester(it) } ?: Modifier)
    )
}

/** Whole-number field (wind speed only). */
@Composable
private fun IntegerField(
    label: String,
    state: MutableState<String>,
    modifier: Modifier = Modifier,
    focusLink: Pair<FocusRequester?, (() -> Unit)?>? = null
) {
    val fm = LocalFocusManager.current
    OutlinedTextField(
        value = state.value,
        onValueChange = { raw ->
            // Accept digits and "-" anywhere (e.g. 242025-26); everything else is dropped.
            state.value = raw.filter { it.isDigit() || it == '-' }
        },
        label = { Text(label) },
        singleLine = true,
        textStyle = CellTextStyle,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            autoCorrect = false,
            imeAction = if (focusLink?.second != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusLink?.second?.invoke() },
            onDone = { fm.clearFocus() }
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(focusLink?.first?.let { Modifier.focusRequester(it) } ?: Modifier)
    )
}

/** Visibility: meteorological codes, two digits, 90–99 only. */
@Composable
private fun VisibilityField(
    state: MutableState<String>,
    modifier: Modifier = Modifier,
    focusLink: Pair<FocusRequester?, (() -> Unit)?>? = null
) {
    val fm = LocalFocusManager.current
    OutlinedTextField(
        value = state.value,
        onValueChange = { raw ->
            val digits = raw.filter { it.isDigit() }.take(2)
            state.value = when {
                digits.isEmpty() -> ""
                digits.length == 1 -> if (digits == "9") digits else state.value
                else -> if (digits.toInt() in 90..99) digits else state.value
            }
        },
        label = { Text("Visibility") },
        singleLine = true,
        textStyle = CellTextStyle,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (focusLink?.second != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusLink?.second?.invoke() },
            onDone = { fm.clearFocus() }
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(focusLink?.first?.let { Modifier.focusRequester(it) } ?: Modifier)
    )
}

/** Read-only field that opens a picker dialog when tapped anywhere on it. */
@Composable
private fun PickerField(
    label: String,
    state: MutableState<String>,
    modifier: Modifier = Modifier,
    focusLink: Pair<FocusRequester?, (() -> Unit)?>? = null,
    onClick: () -> Unit
) {
    val fm = LocalFocusManager.current
    Box(modifier) {
        OutlinedTextField(
            value = state.value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            singleLine = true,
            textStyle = CellTextStyle,
            keyboardOptions = KeyboardOptions(
                imeAction = if (focusLink?.second != null) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusLink?.second?.invoke() },
                onDone = { fm.clearFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(focusLink?.first?.let { Modifier.focusRequester(it) } ?: Modifier)
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { opt ->
                    TextButton(onClick = { onSelect(opt) }, modifier = Modifier.fillMaxWidth()) {
                        Text((if (opt == selected) "● " else "") + opt)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

// ---------------------------------------------------------------------------
// Top capsule
// ---------------------------------------------------------------------------
@Composable
private fun SynopticHourCapsule(
    hourText: String,
    isAuto: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                hourText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isAuto) "AUTO" else "MANUAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun HourPickerDialog(
    autoHour: String,
    selectedHour: String,
    isAuto: Boolean,
    selectedDate: String,
    isAutoDate: Boolean,
    onPickHour: (String?) -> Unit,
    onPickDate: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Observation time & date (UTC)") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Historical Date Selector Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Date (UTC)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    formatDisplayDate(selectedDate),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = {
                                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                    val d = selectedDate.take(2).toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)
                                    val m = (selectedDate.drop(2).take(2).toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
                                    val y = selectedDate.takeLast(4).toIntOrNull() ?: cal.get(Calendar.YEAR)

                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val picked = String.format(Locale.US, "%02d%02d%04d", dayOfMonth, month + 1, year)
                                            onPickDate(picked)
                                        },
                                        y, m, d
                                    ).show()
                                }
                            ) {
                                Text("Change Date")
                            }
                        }
                        if (!isAutoDate) {
                            TextButton(
                                onClick = { onPickDate(null) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Reset to Today", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Text(
                    "Synoptic Hour",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                TextButton(onClick = { onPickHour(null) }, modifier = Modifier.fillMaxWidth()) {
                    Text((if (isAuto) "● " else "") + "Auto — follow device clock (now: $autoHour UTC)")
                }
                SYNOPTIC_HOURS.forEach { h ->
                    TextButton(onClick = { onPickHour(h) }, modifier = Modifier.fillMaxWidth()) {
                        Text((if (!isAuto && h == selectedHour) "● " else "") + "$h UTC")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

fun formatDisplayDate(dateUtc: String): String {
    if (dateUtc.length != 8) return dateUtc
    val day = dateUtc.substring(0, 2)
    val month = dateUtc.substring(2, 4)
    val year = dateUtc.substring(4, 8)
    return "$day/$month/$year"
}

// ---------------------------------------------------------------------------
// UI-only constants (data constants + pure helpers live in ObservationViewModel.kt)
// ---------------------------------------------------------------------------