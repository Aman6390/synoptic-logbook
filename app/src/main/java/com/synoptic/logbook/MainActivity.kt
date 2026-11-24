package com.synoptic.logbook

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Google Glass HUD & Apple Design System Palette
// ---------------------------------------------------------------------------
val GlassPitchBlack = Color(0xFF000000)
val GlassHudSurface = Color(0xFF0E141D)
val GlassHudSurfaceVariant = Color(0xFF151F2C)
val GlassHudBorder = Color(0xFF1D2E42)
val GlassHudCyan = Color(0xFF00E5FF)
val GlassHudCyanContainer = Color(0xFF003846)
val GlassHudCyanOnContainer = Color(0xFF80F2FF)
val GlassHudTextPrimary = Color(0xFFFFFFFF)
val GlassHudTextSecondary = Color(0xFF8E9BAE)
val GlassHudGreen = Color(0xFF00E676)
val GlassHudAmber = Color(0xFFFFAB00)
val GlassHudNeonRed = Color(0xFFFF3D71)

val AppleSnowBackground = Color(0xFFF2F4F8)
val AppleSnowSurface = Color(0xFFFFFFFF)
val AppleSnowSurfaceVariant = Color(0xFFE8EDF4)
val AppleSnowBorder = Color(0xFFD5DCE6)
val AppleSnowTextPrimary = Color(0xFF141923)
val AppleSnowTextSecondary = Color(0xFF657084)
val AppleSystemBlue = Color(0xFF007AFF)

val IconLockOpen: ImageVector by lazy {
    ImageVector.Builder(
        name = "LockOpen",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(12f, 17f)
        curveTo(10.9f, 17f, 10f, 16.1f, 10f, 15f)
        reflectiveCurveTo(10.9f, 13f, 12f, 13f)
        reflectiveCurveTo(14f, 13.9f, 14f, 15f)
        reflectiveCurveTo(13.1f, 17f, 12f, 17f)
        close()
        moveTo(18f, 8f)
        horizontalLineToRelative(-1f)
        verticalLineTo(6f)
        curveTo(17f, 3.24f, 14.76f, 1f, 12f, 1f)
        curveTo(9.56f, 1f, 7.55f, 2.76f, 7.09f, 5.06f)
        lineToRelative(1.96f, 0.44f)
        curveTo(9.3f, 3.94f, 10.53f, 3f, 12f, 3f)
        curveTo(13.66f, 3f, 15f, 4.34f, 15f, 6f)
        verticalLineToRelative(2f)
        horizontalLineTo(6f)
        curveTo(4.9f, 8f, 4f, 8.9f, 4f, 10f)
        verticalLineToRelative(10f)
        curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
        horizontalLineToRelative(12f)
        curveTo(17.1f, 22f, 18f, 21.1f, 18f, 20f)
        verticalLineTo(10f)
        curveTo(18f, 8.9f, 17.1f, 8f, 16f, 8f)
        close()
    }.build()
}

val IconEditNote: ImageVector by lazy {
    ImageVector.Builder(
        name = "EditNote",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(3f, 10f)
        horizontalLineToRelative(11f)
        verticalLineToRelative(2f)
        horizontalLineTo(3f)
        close()
        moveTo(3f, 6f)
        horizontalLineToRelative(11f)
        verticalLineToRelative(2f)
        horizontalLineTo(3f)
        close()
        moveTo(3f, 14f)
        horizontalLineToRelative(7f)
        verticalLineToRelative(2f)
        horizontalLineTo(3f)
        close()
        moveTo(20.71f, 14.04f)
        curveTo(21.1f, 13.65f, 21.1f, 13.02f, 20.71f, 12.63f)
        lineToRelative(-1.34f, -1.34f)
        curveTo(18.97f, 10.9f, 18.34f, 10.9f, 17.95f, 11.29f)
        lineToRelative(-1.14f, 1.14f)
        lineToRelative(2.76f, 2.76f)
        lineToRelative(1.14f, -1.15f)
        close()
        moveTo(12f, 19.88f)
        verticalLineTo(22f)
        horizontalLineToRelative(2.12f)
        lineToRelative(6.12f, -6.12f)
        lineToRelative(-2.12f, -2.12f)
        lineTo(12f, 19.88f)
        close()
    }.build()
}

private val GlassDarkColorScheme = darkColorScheme(
    primary = GlassHudCyan,
    onPrimary = Color.Black,
    primaryContainer = GlassHudCyanContainer,
    onPrimaryContainer = GlassHudCyanOnContainer,
    background = GlassPitchBlack,
    onBackground = GlassHudTextPrimary,
    surface = GlassHudSurface,
    onSurface = GlassHudTextPrimary,
    surfaceVariant = GlassHudSurfaceVariant,
    onSurfaceVariant = GlassHudTextSecondary,
    outline = Color(0xFF00E5FF).copy(alpha = 0.28f),
    error = GlassHudNeonRed,
    onError = Color.Black
)

private val AppleLightColorScheme = lightColorScheme(
    primary = AppleSystemBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1EFFF),
    onPrimaryContainer = Color(0xFF004EA3),
    background = AppleSnowBackground,
    onBackground = AppleSnowTextPrimary,
    surface = AppleSnowSurface,
    onSurface = AppleSnowTextPrimary,
    surfaceVariant = AppleSnowSurfaceVariant,
    onSurfaceVariant = AppleSnowTextSecondary,
    outline = AppleSnowBorder,
    error = GlassHudNeonRed,
    onError = Color.White
)

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

val LocalIsScreenLocked = compositionLocalOf { false }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ObservationViewModel = viewModel()
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val activity = LocalContext.current as? Activity

            // Keep screen on while one-tap screen lock is active
            DisposableEffect(vm.isScreenLocked.value) {
                if (vm.isScreenLocked.value) {
                    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            // Lifecycle sync: on resume, check if synoptic window rolled over; on pause, flush unsaved edits
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> vm.onResume()
                        Lifecycle.Event.ON_PAUSE -> vm.flushSave()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            // System back press / gesture navigation handlers
            BackHandler(enabled = vm.showSideNotepad.value) {
                vm.showSideNotepad.value = false
            }
            BackHandler(enabled = drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }
            BackHandler(enabled = vm.currentScreen.value == AppScreen.HISTORY) {
                vm.openForm()
            }

            // Google Glass HUD & Apple Design Theme from the drawer toggle
            MaterialTheme(
                colorScheme = if (vm.isDarkTheme.value) GlassDarkColorScheme else AppleLightColorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = MaterialTheme.colorScheme.surface,
                                drawerContentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                // Inset Drawer header
                                Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                                    Text(
                                        "Synoptic Pad",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = (-0.5).sp
                                        )
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "Surface Meteorological Logbook",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 0.2.sp
                                        ),
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
                                // Light / Dark (Glass HUD) theme toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Google Glass HUD",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Switch(
                                        checked = vm.isDarkTheme.value,
                                        onCheckedChange = { vm.setDarkTheme(it) }
                                    )
                                }

                                NavigationDrawerItem(
                                    label = { Text("Validation limits") },
                                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Validation limits") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        vm.showLimitsDialog.value = true
                                    }
                                )

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
                            shape = RoundedCornerShape(20.dp),
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

                    // Validation & Limits Dialog
                    if (vm.showLimitsDialog.value) {
                        ValidationLimitsDialog(
                            limits = vm.validationLimits.value,
                            onSave = { newLimits ->
                                vm.updateLimits(newLimits)
                                vm.showLimitsDialog.value = false
                            },
                            onDismiss = { vm.showLimitsDialog.value = false }
                        )
                    }

                    // Add Custom Input Box Dialog
                    if (vm.showAddCustomFieldDialog.value) {
                        AddCustomFieldDialog(
                            onAdd = { label, type, unit ->
                                vm.addCustomFieldTemplate(label, type, unit)
                                vm.showAddCustomFieldDialog.value = false
                            },
                            onDismiss = { vm.showAddCustomFieldDialog.value = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogbookScreen(vm: ObservationViewModel, onMenuClick: () -> Unit) {
    val isGlassDark = MaterialTheme.colorScheme.background == GlassPitchBlack

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

    CompositionLocalProvider(LocalIsScreenLocked provides vm.isScreenLocked.value) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Top Action Bar: Left (Menu, Delete), Center (Synoptic Hour Capsule), Right (Mini Notepad, Lock Pill)
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
                                enabled = editingId.value != null && !vm.isScreenLocked.value
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete this observation")
                            }
                        }
                    }
                    SynopticHourCapsule(
                        hourText = "$selectedHour UTC",
                        isAuto = isAuto,
                        onClick = { if (!vm.isScreenLocked.value) showHourPicker.value = true }
                    )
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Mini Notepad Button with active Cyan dot indicator
                            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                                IconButton(
                                    onClick = { vm.showSideNotepad.value = true },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        IconEditNote,
                                        contentDescription = "Open Mini Notepad",
                                        tint = if (vm.showSideNotepad.value) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (vm.hasAttachedNotes) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 4.dp, end = 4.dp)
                                            .size(6.dp)
                                            .background(Color(0xFF00E5FF), shape = RoundedCornerShape(50))
                                    )
                                }
                            }
                            // Dedicated Prominent One-Tap Screen Lock Pill Button
                            Surface(
                                onClick = { vm.toggleScreenLock() },
                                shape = RoundedCornerShape(16.dp),
                                color = if (vm.isScreenLocked.value) Color(0xFF00E5FF).copy(alpha = 0.22f) else if (isGlassDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(
                                    1.dp,
                                    if (vm.isScreenLocked.value) Color(0xFF00E5FF) else if (isGlassDark) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        if (vm.isScreenLocked.value) Icons.Filled.Lock else IconLockOpen,
                                        contentDescription = if (vm.isScreenLocked.value) "Unlock screen" else "Lock screen",
                                        tint = if (vm.isScreenLocked.value) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (vm.isScreenLocked.value) "LOCKED" else "LOCK",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = if (vm.isScreenLocked.value) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Sub-Header Row: Left (Record Navigation Pill), Right (Auto-save status)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isGlassDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (isGlassDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                        ) {
                            IconButton(
                                onClick = { vm.goOlder() },
                                enabled = canGoOlder && !vm.isScreenLocked.value,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowLeft,
                                    contentDescription = "Older entry",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = posLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { vm.goNewer() },
                                enabled = canGoNewer && !vm.isScreenLocked.value,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowRight,
                                    contentDescription = "Newer entry",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = saveStatus.value ?: "Auto-saves as you type",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Locked Banner Indicator
                if (vm.isScreenLocked.value) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "SCREEN LOCKED • TOUCH FROZEN • TAP PADLOCK TO UNLOCK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                ),
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }

                // Note preview snippet chip if notes attached and not locked
                if (vm.hasAttachedNotes && !vm.isScreenLocked.value) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.showSideNotepad.value = true },
                        color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                IconEditNote,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            val previewSnippet = if (vm.remarks.value.isNotBlank()) {
                                "Note: " + vm.remarks.value.replace("\n", " ").take(40) + (if (vm.remarks.value.length > 40) "…" else "")
                            } else {
                                "${vm.customFieldValues.count { it.value.isNotBlank() }} custom reading(s) attached"
                            }
                            Text(
                                previewSnippet,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF),
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "EDIT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }

        // ---------------- SECTION 1 ----------------
        SectionCard(title = "SURFACE OBSERVATION") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DecimalField("Dry bulb", dryBulb, Modifier.weight(1f), link(FKey.DRY), isError = vm.dryBulbError)
                DecimalField("Wet bulb", wetBulb, Modifier.weight(1f), link(FKey.WET), isError = vm.wetBulbError)
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
                DecimalField("SLP", slp, Modifier.weight(1f), link(FKey.SLP), isError = vm.slpError)
                DecimalField("Past SLP", pastSlp, Modifier.weight(1f), link(FKey.PAST_SLP), isError = vm.pastSlpError)
                val diffColor = when {
                    pressureChange.startsWith("+") -> GlassHudGreen
                    pressureChange.startsWith("-") -> GlassHudAmber
                    else -> MaterialTheme.colorScheme.onSurface
                }
                OutlinedTextField(
                    value = pressureChange,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Diff", maxLines = 1) },
                    singleLine = true,
                    textStyle = CellTextStyle.copy(color = diffColor),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                DecimalField("MSLP", mslp, Modifier.weight(1f), link(FKey.MSLP))
            }
        }

        // ---------------- SECTION 3 (0300 & 1200 UTC only) ----------------
        AnimatedVisibility(
            visible = isMainSynoptic,
            enter = fadeIn(spring(dampingRatio = 0.78f, stiffness = 350f)) +
                    expandVertically(spring(dampingRatio = 0.78f, stiffness = 350f)),
            exit = fadeOut(spring(dampingRatio = 0.78f, stiffness = 350f)) +
                   shrinkVertically(spring(dampingRatio = 0.78f, stiffness = 350f))
        ) {
            SectionCard(title = "MAX / MIN TEMPS (°C)") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DecimalField("Max", maxTemp, Modifier.weight(1f), link(FKey.MAX), isError = vm.maxTempError)
                    DecimalField("Max set", maxTempAfterSet, Modifier.weight(1f), link(FKey.MAX_SET), isError = vm.maxTempSetError)
                    DecimalField("Min", minTemp, Modifier.weight(1f), link(FKey.MIN), isError = vm.minTempError)
                    DecimalField("Min set", minTempAfterSet, Modifier.weight(1f), link(FKey.MIN_SET), isError = vm.minTempSetError)
                }
            }
        }

        // ---------------- SECTION 4 ----------------
        SectionCard(title = "RAIN & GROUND") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DecimalField("Rain (mm)", rain, Modifier.weight(1f), link(FKey.RAIN), isError = vm.rainError)
                PickerField("Ground", ground, Modifier.weight(1f), link(FKey.GROUND)) {
                    showGroundPicker.value = true
                }
            }
        }

        // ---------------- SECTION 5 (0300 & 1200 UTC only) ----------------
        AnimatedVisibility(
            visible = isMainSynoptic,
            enter = fadeIn(spring(dampingRatio = 0.78f, stiffness = 350f)) +
                    expandVertically(spring(dampingRatio = 0.78f, stiffness = 350f)),
            exit = fadeOut(spring(dampingRatio = 0.78f, stiffness = 350f)) +
                   shrinkVertically(spring(dampingRatio = 0.78f, stiffness = 350f))
        ) {
            SectionCard(title = "EVAPORATION & WATER") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DecimalField("Evaporation (mm)", evaporation, Modifier.weight(1f), link(FKey.EVAP))
                    DecimalField("Water temp", waterTemp, Modifier.weight(1f), link(FKey.WATER))
                }
            }
        }
    }

    // Side Notepad slide-over panel
    AnimatedVisibility(
        visible = vm.showSideNotepad.value,
        enter = fadeIn() + slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)
        ),
        exit = fadeOut() + slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)
        )
    ) {
        SideNotepadPanel(
            vm = vm,
            onClose = { vm.showSideNotepad.value = false }
        )
    }
}
}

    // ---------------- Delete confirmation ----------------
    if (vm.showDeleteConfirm.value) {
        val vi = viewIndex.value
        val v = vi?.let { records.getOrNull(it) }
        AlertDialog(
            onDismissRequest = { vm.showDeleteConfirm.value = false },
            shape = RoundedCornerShape(20.dp),
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records, key = { it.id }) { rec ->
                    val isGlassDark = MaterialTheme.colorScheme.background == GlassPitchBlack
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.openRecord(rec) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isGlassDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        rec.dateUtc,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.2).sp
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            "${rec.synopticHourUtc} UTC",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
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
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = (-0.1).sp
                                ),
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
// Building blocks: Apple HIG Proportional Tabular & Inset Grouped System
// ---------------------------------------------------------------------------
/** Shared input typography: clean proportional system font with optical weights and tight tracking. */
private val CellTextStyle = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = FontFamily.Default,
    letterSpacing = (-0.2).sp
)

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val isGlassDark = MaterialTheme.colorScheme.background == GlassPitchBlack
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isGlassDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                (if (isGlassDark) "●  " else "") + title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
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
    val isLocked = LocalIsScreenLocked.current
    val fm = LocalFocusManager.current
    OutlinedTextField(
        value = state.value,
        onValueChange = { state.value = it },
        readOnly = isLocked,
        label = { Text(label, maxLines = 1) },
        singleLine = true,
        textStyle = CellTextStyle,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
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
    focusLink: Pair<FocusRequester?, (() -> Unit)?>? = null,
    isError: Boolean = false
) {
    val isLocked = LocalIsScreenLocked.current
    val fm = LocalFocusManager.current
    OutlinedTextField(
        value = state.value,
        onValueChange = { raw ->
            if (raw.isEmpty() || Regex("^-?\\d*(\\.\\d?)?$").matches(raw)) state.value = raw
        },
        readOnly = isLocked,
        label = { Text(label, maxLines = 1) },
        singleLine = true,
        textStyle = CellTextStyle,
        isError = isError,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
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
    focusLink: Pair<FocusRequester?, (() -> Unit)?>? = null,
    isError: Boolean = false
) {
    val isLocked = LocalIsScreenLocked.current
    val fm = LocalFocusManager.current
    OutlinedTextField(
        value = state.value,
        onValueChange = { raw ->
            state.value = raw.filter { it.isDigit() || it == '-' }
        },
        readOnly = isLocked,
        label = { Text(label, maxLines = 1) },
        singleLine = true,
        textStyle = CellTextStyle,
        isError = isError,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
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
    val isLocked = LocalIsScreenLocked.current
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
        readOnly = isLocked,
        label = { Text("Visibility", maxLines = 1) },
        singleLine = true,
        textStyle = CellTextStyle,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
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
    val isLocked = LocalIsScreenLocked.current
    val fm = LocalFocusManager.current
    Box(modifier) {
        OutlinedTextField(
            value = state.value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, maxLines = 1) },
            singleLine = true,
            textStyle = CellTextStyle,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
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
                .clickable {
                    if (!isLocked) {
                        fm.clearFocus()
                        onClick()
                    }
                }
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
        shape = RoundedCornerShape(20.dp),
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
    val isLocked = LocalIsScreenLocked.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isLocked) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "capsule_press_scale"
    )
    val isGlassDark = MaterialTheme.colorScheme.background == GlassPitchBlack

    Surface(
        shape = RoundedCornerShape(50),
        color = if (isAuto) {
            if (isGlassDark) GlassHudCyan.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer
        } else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isAuto) {
                if (isGlassDark) GlassHudCyan.copy(alpha = 0.45f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            } else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLocked,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                hourText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                ),
                color = if (isAuto) {
                    if (isGlassDark) GlassHudCyan else MaterialTheme.colorScheme.onPrimaryContainer
                } else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isAuto) {
                    if (isGlassDark) GlassHudCyan else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                } else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ) {
                Text(
                    if (isAuto) "AUTO" else "MANUAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isAuto) {
                        if (isGlassDark) Color.Black else MaterialTheme.colorScheme.primary
                    } else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
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
        shape = RoundedCornerShape(20.dp),
        title = { Text("Observation time & date (UTC)") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Historical Date Selector Card with Inset Grouped style
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(Modifier.padding(12.dp)) {
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

@Composable
private fun ValidationLimitsDialog(
    limits: ValidationLimits,
    onSave: (ValidationLimits) -> Unit,
    onDismiss: () -> Unit
) {
    val enabledState = remember { mutableStateOf(limits.enabled) }
    val dbMinState = remember { mutableStateOf(limits.dbMin.toString()) }
    val dbMaxState = remember { mutableStateOf(limits.dbMax.toString()) }
    val wbMinState = remember { mutableStateOf(limits.wbMin.toString()) }
    val wbMaxState = remember { mutableStateOf(limits.wbMax.toString()) }
    val slpMinState = remember { mutableStateOf(limits.slpMin.toString()) }
    val slpMaxState = remember { mutableStateOf(limits.slpMax.toString()) }
    val rainMaxState = remember { mutableStateOf(limits.rainMax.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Validation limits") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Enable Limit Warnings",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = enabledState.value,
                        onCheckedChange = { enabledState.value = it }
                    )
                }

                Text(
                    "Subtle red borders will outline fields when values exceed these limits or violate meteorological sanity checks (e.g. Wet Bulb > Dry Bulb, Max < Dry Bulb, Max ≤ Min).",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (enabledState.value) {
                    Divider()
                    Text("Dry Bulb Range (°C)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dbMinState.value,
                            onValueChange = { dbMinState.value = it },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = dbMaxState.value,
                            onValueChange = { dbMaxState.value = it },
                            label = { Text("Max") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Text("Wet Bulb Range (°C)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = wbMinState.value,
                            onValueChange = { wbMinState.value = it },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = wbMaxState.value,
                            onValueChange = { wbMaxState.value = it },
                            label = { Text("Max") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Text("Surface Pressure Range (hPa)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = slpMinState.value,
                            onValueChange = { slpMinState.value = it },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = slpMaxState.value,
                            onValueChange = { slpMaxState.value = it },
                            label = { Text("Max") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    OutlinedTextField(
                        value = rainMaxState.value,
                        onValueChange = { rainMaxState.value = it },
                        label = { Text("Max Rain (mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    TextButton(
                        onClick = {
                            val def = ValidationLimits.DEFAULT
                            enabledState.value = def.enabled
                            dbMinState.value = def.dbMin.toString()
                            dbMaxState.value = def.dbMax.toString()
                            wbMinState.value = def.wbMin.toString()
                            wbMaxState.value = def.wbMax.toString()
                            slpMinState.value = def.slpMin.toString()
                            slpMaxState.value = def.slpMax.toString()
                            rainMaxState.value = def.rainMax.toString()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reset to Defaults", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val def = ValidationLimits.DEFAULT
                    val newLimits = ValidationLimits(
                        enabled = enabledState.value,
                        dbMin = dbMinState.value.toDoubleOrNull() ?: def.dbMin,
                        dbMax = dbMaxState.value.toDoubleOrNull() ?: def.dbMax,
                        wbMin = wbMinState.value.toDoubleOrNull() ?: def.wbMin,
                        wbMax = wbMaxState.value.toDoubleOrNull() ?: def.wbMax,
                        slpMin = slpMinState.value.toDoubleOrNull() ?: def.slpMin,
                        slpMax = slpMaxState.value.toDoubleOrNull() ?: def.slpMax,
                        rainMax = rainMaxState.value.toDoubleOrNull() ?: def.rainMax
                    )
                    onSave(newLimits)
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SideNotepadPanel(
    vm: ObservationViewModel,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Semi-transparent scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onClose() }
        )

        // Slide-out Drawer Panel on Right
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.92f)
                .align(Alignment.CenterEnd),
            color = if (vm.isDarkTheme.value) Color(0xFF0A0F17) else MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, GlassHudCyan.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            IconEditNote,
                            contentDescription = null,
                            tint = GlassHudCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "MINI NOTEPAD",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                "${vm.selectedHour} UTC • ${vm.selectedDate}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = GlassHudCyan
                            )
                        }
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close notepad")
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // ---------------- SECTION A: DYNAMIC CUSTOM INPUT BOXES ----------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "CUSTOM INPUT BOXES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        modifier = Modifier.clickable { vm.showAddCustomFieldDialog.value = true },
                        color = GlassHudCyan.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, GlassHudCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                tint = GlassHudCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Add Box",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassHudCyan
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Station template info pill
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = GlassHudCyan.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, GlassHudCyan.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Station template definitions persist across all synoptic hours. Values are saved per observation.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                if (vm.customFieldTemplates.value.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "No custom input boxes added yet. Tap '+ Add Box' above to create custom parameters like Soil Temp, Anemometer Run, or Observer ID.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    vm.customFieldTemplates.value.forEach { template ->
                        CustomBoxItem(
                            template = template,
                            value = vm.customFieldValues[template.id] ?: "",
                            onValueChange = { newVal ->
                                val filtered = when (template.type) {
                                    CustomFieldType.DECIMAL -> if (newVal.isEmpty() || Regex("^-?\\d*(\\.\\d*)?$").matches(newVal)) newVal else vm.customFieldValues[template.id] ?: ""
                                    CustomFieldType.INTEGER -> newVal.filter { it.isDigit() || it == '-' }
                                    CustomFieldType.TEXT -> newVal
                                }
                                vm.customFieldValues[template.id] = filtered
                            },
                            onDelete = { vm.removeCustomFieldTemplate(template.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // ---------------- SECTION B: STATION REMARKS & PHENOMENA ----------------
                Text(
                    "STATION REMARKS & PHENOMENA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                // Quick Meteorological tags
                val quickTags = listOf(
                    "⚡ Lightning",
                    "🌧️ Rain Started",
                    "💨 Squall / Gusts",
                    "🌫️ Fog Bank",
                    "☀️ Solar Halo",
                    "⚙️ Barometer Check"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickTags.take(3).forEach { tag ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vm.appendRemark(tag) },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                tag,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickTags.drop(3).forEach { tag ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vm.appendRemark(tag) },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                tag,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        modifier = Modifier.clickable { vm.appendUtcStamp() },
                        color = GlassHudCyan.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, GlassHudCyan.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                tint = GlassHudCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "+ UTC Stamp",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassHudCyan
                            )
                        }
                    }

                    if (vm.remarks.value.isNotBlank()) {
                        Text(
                            "Clear text",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error),
                            modifier = Modifier.clickable { vm.remarks.value = "" }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = vm.remarks.value,
                    onValueChange = { vm.remarks.value = it },
                    minLines = 4,
                    maxLines = 8,
                    placeholder = {
                        Text(
                            "Record local station remarks, squalls, optical phenomena, or observer notes here...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassHudCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    "Auto-saves to ${vm.selectedHour} UTC observation sheet",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun CustomBoxItem(
    template: CustomFieldTemplate,
    value: String,
    onValueChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, GlassHudCyan.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    template.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GlassHudCyan
                )
                Spacer(Modifier.width(6.dp))

                // Type badge
                val (badgeBg, badgeText) = when (template.type) {
                    CustomFieldType.DECIMAL -> GlassHudCyan.copy(alpha = 0.15f) to GlassHudCyan
                    CustomFieldType.INTEGER -> Color(0xFF00E676).copy(alpha = 0.15f) to Color(0xFF00E676)
                    CustomFieldType.TEXT -> Color(0xFFFFAB00).copy(alpha = 0.15f) to Color(0xFFFFAB00)
                }
                Surface(
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeText.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        template.type.name,
                        style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold),
                        color = badgeText,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                if (template.unit.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            template.unit,
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Remove field",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = template.type != CustomFieldType.TEXT,
                placeholder = {
                    Text(
                        if (template.unit.isNotBlank()) "Enter ${template.label} (${template.unit})" else "Enter ${template.label}...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (template.type) {
                        CustomFieldType.DECIMAL -> KeyboardType.Decimal
                        CustomFieldType.INTEGER -> KeyboardType.Number
                        CustomFieldType.TEXT -> KeyboardType.Text
                    }
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassHudCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AddCustomFieldDialog(
    onAdd: (label: String, type: CustomFieldType, unit: String) -> Unit,
    onDismiss: () -> Unit
) {
    val labelState = remember { mutableStateOf("") }
    val typeState = remember { mutableStateOf(CustomFieldType.DECIMAL) }
    val unitState = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = GlassHudCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Add Custom Input Box", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Field Name
                Text(
                    "FIELD LABEL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = labelState.value,
                    onValueChange = { labelState.value = it },
                    placeholder = { Text("e.g. Soil Temp 5cm, Run of Wind") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Input Type selector
                Text(
                    "INPUT TYPE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        CustomFieldType.DECIMAL to "Decimal (12.3)",
                        CustomFieldType.INTEGER to "Integer (123)",
                        CustomFieldType.TEXT to "Text (ABC)"
                    ).forEach { (t, display) ->
                        val isSel = typeState.value == t
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { typeState.value = t },
                            color = if (isSel) GlassHudCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            border = BorderStroke(
                                1.dp,
                                if (isSel) GlassHudCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    t.name,
                                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = if (isSel) GlassHudCyan else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    display.substringAfter("(").substringBefore(")"),
                                    style = TextStyle(fontSize = 9.sp),
                                    color = if (isSel) GlassHudCyan.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Unit (optional)
                Text(
                    "UNIT (OPTIONAL)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = unitState.value,
                    onValueChange = { unitState.value = it },
                    placeholder = { Text("e.g. °C, km, mm, hPa, %") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick unit suggestions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("°C", "km", "mm", "hPa", "kt", "%").forEach { u ->
                        Surface(
                            modifier = Modifier.clickable { unitState.value = u },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                u,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (labelState.value.isNotBlank()) {
                        onAdd(labelState.value, typeState.value, unitState.value)
                    }
                },
                enabled = labelState.value.isNotBlank()
            ) {
                Text("Add Box", fontWeight = FontWeight.Bold, color = GlassHudCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ---------------------------------------------------------------------------
// UI-only constants (data constants + pure helpers live in ObservationViewModel.kt)
// ---------------------------------------------------------------------------