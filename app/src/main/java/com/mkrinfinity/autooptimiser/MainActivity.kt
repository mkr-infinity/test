@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.mkrinfinity.autooptimiser

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mkrinfinity.autooptimiser.accessibility.AutoOptimiserAccessibilityService
import com.mkrinfinity.autooptimiser.accessibility.StopItem
import com.mkrinfinity.autooptimiser.accessibility.StopOutcome
import com.mkrinfinity.autooptimiser.accessibility.StopProgress
import com.mkrinfinity.autooptimiser.accessibility.StopSession
import com.mkrinfinity.autooptimiser.accessibility.StopStep
import com.mkrinfinity.autooptimiser.data.AppFilter
import com.mkrinfinity.autooptimiser.data.AppRecord
import com.mkrinfinity.autooptimiser.data.AppRepository
import com.mkrinfinity.autooptimiser.data.AppSort
import com.mkrinfinity.autooptimiser.data.AppSelectionLogic
import com.mkrinfinity.autooptimiser.data.DeviceRepository
import com.mkrinfinity.autooptimiser.data.DeviceStatus
import com.mkrinfinity.autooptimiser.data.LastOptimisation
import com.mkrinfinity.autooptimiser.data.PreferencesRepository
import com.mkrinfinity.autooptimiser.data.StorageCategory
import com.mkrinfinity.autooptimiser.data.StorageEntry
import com.mkrinfinity.autooptimiser.data.StorageReport
import com.mkrinfinity.autooptimiser.data.StorageRepository
import com.mkrinfinity.autooptimiser.data.ThemeMode
import com.mkrinfinity.autooptimiser.model.ProtectedApp
import com.mkrinfinity.autooptimiser.model.ProtectionReason
import com.mkrinfinity.autooptimiser.ui.theme.AutoOptimiserTheme
import com.mkrinfinity.autooptimiser.ui.theme.CopperColor
import com.mkrinfinity.autooptimiser.ui.theme.InfoColor
import com.mkrinfinity.autooptimiser.ui.theme.SuccessColor
import com.mkrinfinity.autooptimiser.ui.theme.WarningColor
import com.mkrinfinity.autooptimiser.work.AutoAnalysisWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val viewModel: AutoOptimiserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by viewModel.theme.collectAsStateWithLifecycle()
            AutoOptimiserTheme(darkTheme = when (theme) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }) {
                val onboardingComplete by viewModel.onboardingComplete.collectAsStateWithLifecycle()
                if (onboardingComplete) AutoOptimiserShell(viewModel) else OnboardingFlow(viewModel)
            }
        }
    }
}

data class UiMessage(val text: String, val isError: Boolean = false)

data class AnalysisState(
    val running: Boolean = false,
    val stage: String? = null,
    val complete: Boolean = false,
    val candidates: Int? = null,
    val storageBytes: Long? = null,
    val batteryNote: String? = null,
    val automaticOptimisation: Boolean? = null,
    val confirmationRequired: Boolean? = null,
    val error: String? = null
)

data class AppUiState(
    val apps: List<AppRecord> = emptyList(),
    val loading: Boolean = false,
    val query: String = "",
    val filter: AppFilter = AppFilter.ALL,
    val sort: AppSort = AppSort.NAME,
    val selected: Set<String> = emptySet(),
    val detailPackage: String? = null
)

class AutoOptimiserViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefs = PreferencesRepository(context)
    private val appRepository = AppRepository(application)
    private val deviceRepository = DeviceRepository(application)
    private val storageRepository = StorageRepository(context)
    private val mutableApps = MutableStateFlow(AppUiState())
    private val mutableDevice = MutableStateFlow<DeviceStatus?>(null)
    private val mutableReport = MutableStateFlow<StorageReport?>(null)
    private val mutableStorageLoading = MutableStateFlow(false)
    private val mutableAnalysis = MutableStateFlow(AnalysisState())
    private var analysisJob: Job? = null
    private var storageScanJob: Job? = null
    private val mutableMessage = MutableStateFlow<UiMessage?>(null)
    private val mutableOnboarding = MutableStateFlow(prefs.onboardingComplete)
    private val mutableProgress = MutableStateFlow(StopProgress())

    val apps: StateFlow<AppUiState> = mutableApps.asStateFlow()
    val device: StateFlow<DeviceStatus?> = mutableDevice.asStateFlow()
    val storageReport: StateFlow<StorageReport?> = mutableReport.asStateFlow()
    val storageLoading: StateFlow<Boolean> = mutableStorageLoading.asStateFlow()
    val analysis: StateFlow<AnalysisState> = mutableAnalysis.asStateFlow()
    val message: StateFlow<UiMessage?> = mutableMessage.asStateFlow()
    val onboardingComplete: StateFlow<Boolean> = mutableOnboarding.asStateFlow()
    val progress: StateFlow<StopProgress> = mutableProgress.asStateFlow()
    val theme: StateFlow<ThemeMode> = prefs.theme
    val lastOptimisation get() = prefs.getLastOptimisation()
    val selectedTreeUri get() = prefs.selectedTreeUri
    val largeThresholdBytes get() = prefs.largeThresholdBytes
    val automaticOptimisation get() = prefs.automaticOptimisation
    val automaticFrequency get() = prefs.automaticFrequency
    val confirmationRequired get() = prefs.confirmationRequired

    init {
        refreshApps()
        refreshDevice()
        viewModelScope.launch {
            StopSession.progress.collectLatest { value ->
                mutableProgress.value = value
                if (value.total > 0 && (value.step == StopStep.COMPLETED || value.step == StopStep.CANCELLED)) {
                    val successful = value.results.count { it.outcome == StopOutcome.SUCCESS }
                    val skipped = value.results.count { it.outcome == StopOutcome.SKIPPED }
                    val failed = value.results.count { it.outcome == StopOutcome.FAILED }
                    if (value.total > 0) prefs.setLastOptimisation(LastOptimisation(System.currentTimeMillis(), value.total, successful, skipped, failed))
                    if (value.step == StopStep.COMPLETED) refreshApps()
                }
            }
        }
    }

    fun completeOnboarding() { prefs.setOnboardingComplete(true); mutableOnboarding.value = true }
    fun setTheme(value: ThemeMode) { prefs.setTheme(value) }
    fun refreshDevice() { viewModelScope.launch { mutableDevice.value = deviceRepository.read() } }
    fun clearMessage() { mutableMessage.value = null }
    private fun message(text: String, error: Boolean = false) { mutableMessage.value = UiMessage(text, error) }

    fun refreshApps() {
        mutableApps.value = mutableApps.value.copy(loading = true)
        viewModelScope.launch {
            val protected = prefs.protectedPackages.associateWith { ProtectedApp(it, 0L, ProtectionReason.USER_SELECTED) }
            val fresh = runCatching { appRepository.refresh(protected) }.getOrElse {
                message("Android did not allow reading the installed-app list.", true)
                emptyList()
            }
            mutableApps.value = mutableApps.value.copy(apps = fresh, loading = false)
        }
    }

    fun setQuery(value: String) { mutableApps.value = mutableApps.value.copy(query = value) }
    fun setFilter(value: AppFilter) { mutableApps.value = mutableApps.value.copy(filter = value) }
    fun setSort(value: AppSort) { mutableApps.value = mutableApps.value.copy(sort = value) }
    fun toggleSelection(packageName: String) {
        val record = mutableApps.value.apps.firstOrNull { it.packageName == packageName } ?: return
        if (record.isProtected) return
        val next = mutableApps.value.selected.toMutableSet().apply { if (!add(packageName)) remove(packageName) }
        mutableApps.value = mutableApps.value.copy(selected = next)
    }
    fun selectAllEligible() {
        val eligible = AppSelectionLogic.filter(mutableApps.value.apps, "", AppFilter.ALL, mutableApps.value.sort)
            .filterNot { it.isProtected }.map { it.packageName }.toSet()
        mutableApps.value = mutableApps.value.copy(selected = eligible)
    }
    fun clearSelection() { mutableApps.value = mutableApps.value.copy(selected = emptySet()) }
    fun protect(packageName: String, value: Boolean) {
        prefs.toggleProtected(packageName, value)
        refreshApps()
    }
    fun openApp(packageName: String) {
        context.packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); context.startActivity(it)
        } ?: message("Android does not expose a launch action for this app.", true)
    }
    fun appInfo(packageName: String) {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    fun uninstall(packageName: String) {
        context.startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    fun filteredApps(): List<AppRecord> {
        val state = mutableApps.value
        return AppSelectionLogic.filter(state.apps, state.query, state.filter, state.sort)
    }

    fun analyse() {
        if (mutableAnalysis.value.running) return
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            try {
                mutableAnalysis.value = AnalysisState(running = true, stage = "Checking applications")
                val appCount = withContext(Dispatchers.Default) { mutableApps.value.apps.count { it.isRunning && !it.isProtected && !it.inventory.isSystemApp } }
                mutableAnalysis.value = AnalysisState(running = true, stage = "Checking storage", candidates = appCount)
                val report = selectedTreeUri?.let {
                    try { storageRepository.scanTree(it, largeThresholdBytes) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { null }
                }
                mutableAnalysis.value = AnalysisState(running = true, stage = "Checking battery", candidates = appCount, storageBytes = report?.entries?.filter { it.sizeBytes >= largeThresholdBytes }?.sumOf { it.sizeBytes })
                val status = deviceRepository.read()
                mutableAnalysis.value = AnalysisState(running = true, stage = "Checking optimisation settings", candidates = appCount, storageBytes = report?.entries?.filter { it.sizeBytes >= largeThresholdBytes }?.sumOf { it.sizeBytes })
                // Reading the persisted settings is intentionally the final, cheap stage.
                mutableAnalysis.value = AnalysisState(
                    running = false, complete = true, candidates = appCount,
                    storageBytes = report?.entries?.filter { it.sizeBytes >= largeThresholdBytes }?.sumOf { it.sizeBytes },
                    batteryNote = status.batteryPercent?.let { "$it% reported by Android" } ?: "Battery level is not exposed on this device",
                    automaticOptimisation = prefs.automaticOptimisation,
                    confirmationRequired = prefs.confirmationRequired
                )
                if (report != null) mutableReport.value = report
            } finally {
                if (mutableAnalysis.value.running) mutableAnalysis.value = mutableAnalysis.value.copy(running = false, stage = null)
            }
        }
    }

    fun cancelAnalysis() {
        analysisJob?.cancel()
        analysisJob = null
        if (mutableAnalysis.value.running) mutableAnalysis.value = mutableAnalysis.value.copy(running = false, stage = null)
    }
    fun scanStorage() {
        val uri = selectedTreeUri ?: run { message("Choose a folder first. Auto Optimiser only scans folders you explicitly grant."); return }
        storageScanJob?.cancel()
        storageScanJob = viewModelScope.launch {
            mutableStorageLoading.value = true
            try {
                mutableReport.value = null
                mutableReport.value = try {
                    storageRepository.scanTree(uri, largeThresholdBytes)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    message("The selected folder could not be scanned.", true)
                    null
                }
            } finally {
                mutableStorageLoading.value = false
            }
        }
    }
    fun cancelStorageScan() {
        storageScanJob?.cancel()
        storageScanJob = null
        mutableStorageLoading.value = false
    }
    fun saveTreeUri(uri: Uri) {
        try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (_: SecurityException) { }
        prefs.setTreeUri(uri); scanStorage()
    }
    fun deleteStorage(entries: List<StorageEntry>) {
        viewModelScope.launch {
            val result = storageRepository.delete(entries)
            val failures = result.count { !it.deleted }
            message(if (failures == 0) "Deleted ${result.size} selected item${if (result.size == 1) "" else "s"}." else "$failures item${if (failures == 1) " was" else "s were"} not permitted by Android.", failures > 0)
            scanStorage()
        }
    }

    fun startOptimisation() {
        val selected = mutableApps.value.apps.filter { mutableApps.value.selected.contains(it.packageName) && !it.isProtected }
        if (selected.isEmpty()) { message("Select at least one eligible app first."); return }
        startStopQueue(selected)
    }
    fun stopSingle(packageName: String) {
        val record = mutableApps.value.apps.firstOrNull { it.packageName == packageName }
        if (record == null || record.isProtected) { message("This app is protected and cannot be stopped by Auto Optimiser.", true); return }
        startStopQueue(listOf(record))
    }
    private fun startStopQueue(records: List<AppRecord>) {
        if (!isAccessibilityEnabled(context)) { message("Accessibility access is required for the supported multi-app stop workflow.", true); openAccessibilitySettings(); return }
        if (!StopSession.start(records.map { StopItem(it.packageName, it.label) })) {
            message("The automation service is not ready. Enable Auto Optimiser in Accessibility settings, then try again.", true)
        }
    }
    fun cancelOptimisation() { StopSession.cancel() }
    fun dismissOptimisationResult() { StopSession.set(StopProgress()) }
    fun openAccessibilitySettings() { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

    fun setAutomatic(value: Boolean) {
        prefs.setAutomatic(value)
        if (value) scheduleWorker() else WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
    fun setFrequency(value: String) { prefs.setAutomaticFrequency(value); if (automaticOptimisation) scheduleWorker() }
    fun setConfirmation(value: Boolean) { prefs.setConfirmationRequired(value) }
    fun setLargeThreshold(value: Long) { prefs.setLargeThresholdBytes(value) }
    private fun scheduleWorker() {
        val days = if (prefs.automaticFrequency == "daily") 1L else 7L
        val request = PeriodicWorkRequestBuilder<AutoAnalysisWorker>(days, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    companion object {
        const val WORK_NAME = "auto_optimiser_light_analysis"
        fun isAccessibilityEnabled(context: Context): Boolean {
            val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
            val full = "${context.packageName}/${AutoOptimiserAccessibilityService::class.java.name}"
            val short = "${context.packageName}/.accessibility.${AutoOptimiserAccessibilityService::class.java.simpleName}"
            return enabled.split(':').any { it.equals(full, true) || it.equals(short, true) }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun AutoOptimiserShell(vm: AutoOptimiserViewModel) {
    val navController = rememberNavController()
    val message by vm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it.text); vm.clearMessage() } }
    val navItems = listOf(
        NavItem("home", "Home", Icons.Outlined.Tune),
        NavItem("apps", "Apps", Icons.Outlined.Apps),
        NavItem("storage", "Storage", Icons.Outlined.Storage),
        NavItem("battery", "Battery", Icons.Outlined.BatteryStd),
        NavItem("settings", "Settings", Icons.Outlined.Settings)
    )
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "home"
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding(), containerColor = MaterialTheme.colorScheme.surface) {
                navItems.forEach { item ->
                    NavigationBarItem(selected = route == item.route, onClick = { navController.navigate(item.route) { launchSingleTop = true; popUpTo("home") { saveState = true } } }, icon = { Icon(item.icon, item.label) }, label = { Text(item.label) })
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen(vm, navController) }
            composable("apps") { AppsScreen(vm, navController) }
            composable("storage") { StorageScreen(vm) }
            composable("battery") { BatteryScreen(vm) }
            composable("settings") { SettingsScreen(vm, navController) }
            composable("about") { AboutScreen(navController) }
            composable("app/{packageName}") { back -> back.arguments?.getString("packageName")?.let { AppDetailScreen(vm, it, navController) } }
        }
    }
    OptimisationOverlay(vm)
    OptimisationResultDialog(vm)
}

@Composable
private fun BrandHeader(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(com.mkrinfinity.autooptimiser.R.drawable.ic_brand), null, Modifier.size(32.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("AUTO OPTIMISER", style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Understand your device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HomeScreen(vm: AutoOptimiserViewModel, nav: NavHostController) {
    DisposableEffect(Unit) { onDispose { vm.cancelAnalysis() } }
    val status by vm.device.collectAsStateWithLifecycle()
    val analysis by vm.analysis.collectAsStateWithLifecycle()
    val last = vm.lastOptimisation
    val progress by vm.progress.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { BrandHeader(Modifier.fillMaxWidth()) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("A calmer way to optimise", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text("See what Android exposes, understand the trade-offs, then choose what to do.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { DeviceStatusPanel(status, vm::refreshDevice) }
        item {
            Button(onClick = vm::analyse, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.Search, null); Spacer(Modifier.width(9.dp)); Text(if (analysis.running) analysis.stage ?: "Analysing" else "Analyse device")
            }
        }
        if (analysis.running) item { AnalysisProgress(analysis) }
        if (analysis.complete) item { AnalysisResult(analysis) }
        item { SectionLabel("Quick actions") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction("Optimise", Icons.Outlined.Tune, Modifier.weight(1f)) { nav.navigate("apps") }
                QuickAction("Apps", Icons.Outlined.Apps, Modifier.weight(1f)) { nav.navigate("apps") }
                QuickAction("Deep clean", Icons.Outlined.CleaningServices, Modifier.weight(1f)) { nav.navigate("storage") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction("Storage", Icons.Outlined.Storage, Modifier.weight(1f)) { nav.navigate("storage") }
                QuickAction("Battery", Icons.Outlined.BatteryChargingFull, Modifier.weight(1f)) { nav.navigate("battery") }
                Spacer(Modifier.weight(1f))
            }
        }
        item { SectionLabel("Last optimisation") }
        item { if (last == null) EmptyLastOptimisation() else LastOptimisationCard(last) }
        if (progress.step != StopStep.IDLE && progress.step != StopStep.COMPLETED && progress.step != StopStep.CANCELLED) item { ActiveProgressCard(progress) }
    }
}

@Composable
private fun DeviceStatusPanel(status: DeviceStatus?, refresh: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Device snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = refresh) { Icon(Icons.Outlined.Refresh, "Refresh") }
            }
            if (status == null) LinearProgressIndicator(Modifier.fillMaxWidth()) else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusMetric("Storage", "${status.storageUsedPercent}% used", Icons.Outlined.Storage, Modifier.weight(1f))
                    StatusMetric("Memory", "${status.memoryUsedPercent}% used", Icons.Outlined.Memory, Modifier.weight(1f))
                    StatusMetric("Battery", status.batteryPercent?.let { "$it%" } ?: "Not exposed", Icons.Outlined.BatteryStd, Modifier.weight(1f))
                }
                Text("Measured just now · free memory is not a performance score.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusMetric(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Text(label, style = MaterialTheme.typography.labelMedium); Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick, modifier.height(76.dp), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(8.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) { Icon(icon, null); Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
}

@Composable
private fun SectionLabel(text: String) { Text(text.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.1.sp, fontWeight = FontWeight.Bold) }

@Composable
private fun EmptyLastOptimisation() { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .35f)), shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Info, null, tint = InfoColor); Spacer(Modifier.width(12.dp)); Text("No optimisation has been run yet. Results will appear here after a real operation.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun LastOptimisationCard(last: LastOptimisation) { Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(last.atMillis)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { ResultNumber("Successful", last.successful, SuccessColor); ResultNumber("Skipped", last.skipped, WarningColor); ResultNumber("Failed", last.failed, MaterialTheme.colorScheme.error) } } } }
@Composable private fun ResultNumber(label: String, number: Int, color: Color) { Column { Text(number.toString(), style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.SemiBold); Text(label, style = MaterialTheme.typography.labelSmall) } }

@Composable
private fun AnalysisProgress(analysis: AnalysisState) { Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(analysis.stage ?: "Checking", fontWeight = FontWeight.SemiBold); LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Analysis is on demand and stops when complete.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable
private fun AnalysisResult(analysis: AnalysisState) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Analysis complete", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text("${analysis.candidates ?: 0} running user apps are eligible to review."); Text(analysis.storageBytes?.let { "${formatBytes(it)} above your large-file threshold." } ?: "Storage scan was not available; choose a folder in Deep clean."); Text(analysis.batteryNote ?: "Battery data was not exposed by Android.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Settings · automatic ${if (analysis.automaticOptimisation == true) "on" else "off"} · confirmation ${if (analysis.confirmationRequired == true) "on" else "off"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun AppsScreen(vm: AutoOptimiserViewModel, nav: NavHostController) {
    val state by vm.apps.collectAsStateWithLifecycle()
    val apps = vm.filteredApps()
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("Applications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold); Text("${state.selected.size} selected · ${state.apps.size} discovered", color = MaterialTheme.colorScheme.onSurfaceVariant) }; IconButton(vm::refreshApps) { Icon(Icons.Outlined.Refresh, "Refresh applications") } } }
        item { SearchField(state.query, vm::setQuery) }
        item { FilterRow(state.filter, vm::setFilter) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Sort", style = MaterialTheme.typography.labelLarge); Box { TextButton(onClick = { sortMenu = true }) { Text(state.sort.name.lowercase().replaceFirstChar { it.uppercase() }); Icon(Icons.Outlined.ArrowDropDown, null) }; DropdownMenu(sortMenu, { sortMenu = false }) { AppSort.values().forEach { DropdownMenuItem(text = { Text(it.name.lowercase().replaceFirstChar { c -> c.uppercase() }) }, onClick = { vm.setSort(it); sortMenu = false }) } } }; TextButton(onClick = vm::selectAllEligible) { Text("Select all eligible") }; TextButton(onClick = vm::clearSelection) { Text("Clear") } } }
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        items(apps, key = { it.packageName }) { record -> AppRow(record, state.selected.contains(record.packageName), { vm.toggleSelection(record.packageName) }, { nav.navigate("app/${Uri.encode(record.packageName)}") }) }
        item { Spacer(Modifier.height(82.dp)) }
    }
    if (state.selected.isNotEmpty()) {
        Surface(Modifier.fillMaxWidth().padding(12.dp), tonalElevation = 4.dp, shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("${state.selected.size} eligible selected", fontWeight = FontWeight.SemiBold); Button(onClick = { if (vm.confirmationRequired) showConfirm = true else vm.startOptimisation() }) { Icon(Icons.Outlined.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Optimise") } } }
    }
    if (showConfirm) AlertDialog(onDismissRequest = { showConfirm = false }, icon = { Icon(Icons.Outlined.Security, null) }, title = { Text("Optimise ${state.selected.size} applications?") }, text = { Text("Auto Optimiser will open Android App Info and use Accessibility to perform the supported stop workflow. Protected apps will not be touched.") }, confirmButton = { Button(onClick = { showConfirm = false; vm.startOptimisation() }) { Text("Start optimisation") } }, dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } })
}

@Composable private fun SearchField(value: String, onChange: (String) -> Unit) { androidx.compose.material3.OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), placeholder = { Text("Search name or package") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true, shape = RoundedCornerShape(14.dp)) }
@Composable private fun FilterRow(selected: AppFilter, onSelect: (AppFilter) -> Unit) { Row(Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { AppFilter.values().forEach { FilterChip(selected == it, { onSelect(it) }, label = { Text(it.name.lowercase().replaceFirstChar { c -> c.uppercase() }) }) } } }

@Composable
private fun AppRow(record: AppRecord, selected: Boolean, onToggle: () -> Unit, onOpen: () -> Unit) {
    Card(onClick = onOpen, shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .22f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(record.icon, record.label)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(record.label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold); Text(record.packageName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { if (record.inventory.isSystemApp) LabelChip("System") else LabelChip("User"); if (record.isRunning) LabelChip("Running"); if (record.isProtected) LabelChip("Protected") } }
            Checkbox(selected, { onToggle() }, enabled = !record.isProtected)
        }
    }
}

@Composable private fun LabelChip(text: String) { AssistChip(onClick = {}, enabled = false, label = { Text(text, style = MaterialTheme.typography.labelSmall) }) }
@Composable private fun AppIcon(drawable: Drawable, label: String, size: androidx.compose.ui.unit.Dp = 48.dp) { val bitmap = remember(drawable) { drawable.toBitmap(size.value.toInt().coerceAtLeast(1)) }; Image(bitmap.asImageBitmap(), contentDescription = "$label icon", modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit) }
private fun Drawable.toBitmap(size: Int): Bitmap { val bitmap = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888); val canvas = Canvas(bitmap); setBounds(0, 0, canvas.width, canvas.height); draw(canvas); return bitmap }

@Composable
private fun AppDetailScreen(vm: AutoOptimiserViewModel, packageName: String, nav: NavHostController) {
    val record = vm.apps.collectAsStateWithLifecycle().value.apps.firstOrNull { it.packageName == packageName }
    var stopConfirm by rememberSaveable { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Text("App details") }, navigationIcon = { IconButton({ nav.popBackStack() }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) }) { padding ->
        if (record == null) Column(Modifier.padding(padding).padding(20.dp)) { Text("This app is no longer available.") } else LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Row(verticalAlignment = Alignment.CenterVertically) { AppIcon(record.icon, record.label, 72.dp); Spacer(Modifier.width(16.dp)); Column { Text(record.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold); Text(record.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            item { DetailFacts(record) }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button({ vm.openApp(packageName) }, Modifier.weight(1f), enabled = record.inventory.isLaunchable) { Text("Open") }; OutlinedButton({ vm.appInfo(packageName) }, Modifier.weight(1f)) { Text("App Info") } } }
            item { Button({ stopConfirm = true }, Modifier.fillMaxWidth(), enabled = !record.isProtected) { Icon(Icons.Outlined.Tune, null); Spacer(Modifier.width(6.dp)); Text("Stop app") } }
            item { OutlinedButton({ vm.uninstall(packageName) }, Modifier.fillMaxWidth(), enabled = packageName != "com.mkrinfinity.autooptimiser") { Icon(Icons.Outlined.DeleteOutline, null); Spacer(Modifier.width(6.dp)); Text("Uninstall through Android") } }
            item { val userCanToggle = record.protectionReason == null || record.protectionReason == ProtectionReason.USER_SELECTED; Button({ vm.protect(packageName, !record.isProtected) }, Modifier.fillMaxWidth(), enabled = userCanToggle, colors = ButtonDefaults.buttonColors(containerColor = if (record.isProtected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)) { Icon(Icons.Outlined.Shield, null); Spacer(Modifier.width(6.dp)); Text(if (!userCanToggle) "Protected by Android" else if (record.isProtected) "Remove protection" else "Protect this app") } }
            item { Text("Stopping an app is only available through Android's App Info workflow. Cache and private data remain untouched.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
    if (stopConfirm && record != null) AlertDialog(onDismissRequest = { stopConfirm = false }, icon = { Icon(Icons.Outlined.Tune, null) }, title = { Text("Stop ${record.label}?") }, text = { Text("Android will open App Info and ask for the supported Force stop action. Auto Optimiser will not clear data or cache.") }, confirmButton = { Button({ stopConfirm = false; vm.stopSingle(packageName) }) { Text("Start") } }, dismissButton = { TextButton({ stopConfirm = false }) { Text("Cancel") } })
}

@Composable private fun DetailFacts(record: AppRecord) { Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { FactRow("Version", record.inventory.versionName ?: "Not exposed"); FactRow("Version code", record.inventory.versionCode.toString()); FactRow("APK size", record.inventory.sizeBytes?.let(::formatBytes) ?: "Not exposed"); FactRow("State", if (record.isRunning) "Running" else "Not observed running"); FactRow("Protection", record.protectionReason?.name?.lowercase()?.replace('_', ' ') ?: "Not protected") } } }
@Composable private fun FactRow(label: String, value: String) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.End) } }

@Composable
private fun StorageScreen(vm: AutoOptimiserViewModel) {
    DisposableEffect(Unit) { onDispose { vm.cancelStorageScan() } }
    val report by vm.storageReport.collectAsStateWithLifecycle()
    val loading by vm.storageLoading.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf(setOf<String>()) }
    var category by rememberSaveable { mutableStateOf<StorageCategory?>(null) }
    var deleteConfirm by remember { mutableStateOf(false) }
    val treeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> uri?.let(vm::saveTreeUri) }
    val entries = report?.entries.orEmpty().filter { category == null || it.category == category }
    Scaffold(topBar = { TopAppBar(title = { Text("Deep clean") }, actions = { IconButton(vm::scanStorage) { Icon(Icons.Outlined.Refresh, "Rescan") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Only folders you grant are scanned. Nothing is deleted without your confirmation.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { OutlinedButton({ treeLauncher.launch(vm.selectedTreeUri) }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.FolderOpen, null); Spacer(Modifier.width(8.dp)); Text(if (vm.selectedTreeUri == null) "Choose a folder to scan" else "Change scanned folder") } }
            if (loading) item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Scanning the selected folder…", fontWeight = FontWeight.SemiBold); LinearProgressIndicator(Modifier.fillMaxWidth()) } }
            report?.let { r ->
                item { StorageSummary(r) }
                item { CategoryRow(r, category) { category = it } }
                item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${entries.size} files", fontWeight = FontWeight.SemiBold); TextButton(onClick = { selected = entries.map { it.id }.toSet() }) { Text("Select visible") } } }
                items(entries, key = { it.id }) { entry -> StorageEntryRow(entry, selected.contains(entry.id)) { selected = selected.toMutableSet().apply { if (!add(entry.id)) remove(entry.id) } } }
                if (selected.isNotEmpty()) item { Button({ deleteConfirm = true }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.DeleteOutline, null); Spacer(Modifier.width(8.dp)); Text("Delete ${selected.size} selected") } }
            } ?: if (!loading) { item { EmptyStorage() } } else Unit
        }
    }
    if (deleteConfirm) AlertDialog(onDismissRequest = { deleteConfirm = false }, icon = { Icon(Icons.Outlined.DeleteOutline, null) }, title = { Text("Delete selected files?") }, text = { Text("Android will delete these real files from the folder you granted. This cannot be undone.") }, confirmButton = { Button({ deleteConfirm = false; vm.deleteStorage(entries.filter { selected.contains(it.id) }); selected = emptySet() }) { Text("Delete") } }, dismissButton = { TextButton({ deleteConfirm = false }) { Text("Cancel") } })
}

@Composable private fun EmptyStorage() { Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Outlined.FolderOpen, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary); Text("No folder scanned", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text("Choose Downloads or another folder. Auto Optimiser cannot access other apps' private data.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun StorageSummary(report: StorageReport) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(report.rootName, fontWeight = FontWeight.SemiBold); Text("${report.entries.size} files · ${formatBytes(report.totalBytes)} indexed"); if (report.skippedCount > 0) Text("${report.skippedCount} folders could not be read by Android.", color = WarningColor, style = MaterialTheme.typography.bodySmall) } } }
@Composable private fun CategoryRow(report: StorageReport, current: StorageCategory?, onPick: (StorageCategory?) -> Unit) { androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { item { FilterChip(current == null, { onPick(null) }, label = { Text("All") }) }; StorageCategory.values().forEach { c -> if (report.entries.any { it.category == c }) item { FilterChip(current == c, { onPick(c) }, label = { Text(c.label) }) } } } }
@Composable private fun StorageEntryRow(entry: StorageEntry, selected: Boolean, onToggle: () -> Unit) { Card(shape = RoundedCornerShape(14.dp)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(selected, { onToggle() }); Column(Modifier.weight(1f)) { Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium); Text(entry.category.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); Text(formatBytes(entry.sizeBytes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun BatteryScreen(vm: AutoOptimiserViewModel) {
    val status by vm.device.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Battery", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold) }
        item { BatteryHero(status) }
        item { if (status != null) BatteryFacts(status!!) else LinearProgressIndicator(Modifier.fillMaxWidth()) }
        item { Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("What Android exposes", fontWeight = FontWeight.SemiBold); Text("Auto Optimiser reports the battery data Android makes available. It cannot increase battery capacity or promise a battery boost.", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Per-app battery usage is a system surface and is not exposed to ordinary apps on every Android release.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
        item { OutlinedButton(vm::refreshDevice, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Refresh snapshot") } }
    }
}
@Composable private fun BatteryHero(status: DeviceStatus?) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (status?.isCharging == true) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd, null, Modifier.size(54.dp), tint = CopperColor); Spacer(Modifier.width(16.dp)); Column { Text(status?.batteryPercent?.let { "$it%" } ?: "Unavailable", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold); Text(if (status?.isCharging == true) "Charging" else "Not charging", color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
@Composable private fun BatteryFacts(status: DeviceStatus) { Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { FactRow("Temperature", status.batteryTemperatureCelsius?.let { "%.1f °C".format(it) } ?: "Not exposed"); FactRow("Health", status.batteryHealth ?: "Not exposed"); FactRow("Battery optimisation", if (status.batteryOptimisationIgnored) "Not restricted" else "System-managed") } } }

@Composable
private fun SettingsScreen(vm: AutoOptimiserViewModel, nav: NavHostController) {
    val theme by vm.theme.collectAsStateWithLifecycle()
    var themeMenu by remember { mutableStateOf(false) }
    var frequencyMenu by remember { mutableStateOf(false) }
    var thresholdMenu by remember { mutableStateOf(false) }
    var threshold by remember { mutableStateOf(vm.largeThresholdBytes) }
    var automatic by remember { mutableStateOf(vm.automaticOptimisation) }
    var confirmation by remember { mutableStateOf(vm.confirmationRequired) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold) }
        item { SettingsSection("Appearance", Icons.Outlined.Palette) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Theme"); Box { TextButton({ themeMenu = true }) { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }); Icon(Icons.Outlined.ArrowDropDown, null) }; DropdownMenu(themeMenu, { themeMenu = false }) { ThemeMode.values().forEach { mode -> DropdownMenuItem(text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { vm.setTheme(mode); themeMenu = false }) } } } } } }
        item { SettingsSection("Optimisation", Icons.Outlined.Tune) { ToggleRow("Automatic optimisation", "Runs a lightweight check with WorkManager; it does not stop apps in the background.", automatic) { automatic = it; vm.setAutomatic(it) }; Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Schedule"); Text("${vm.automaticFrequency.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Box { IconButton({ frequencyMenu = true }) { Icon(Icons.Outlined.MoreVert, "Choose schedule") }; DropdownMenu(frequencyMenu, { frequencyMenu = false }) { listOf("daily", "weekly").forEach { DropdownMenuItem(text = { Text(it.replaceFirstChar { c -> c.uppercase() }) }, onClick = { vm.setFrequency(it); frequencyMenu = false }) } } } } ; ToggleRow("Require confirmation", "Review the queue before opening Android App Info.", confirmation) { confirmation = it; vm.setConfirmation(it) }; TextButton({ nav.navigate("apps") }) { Icon(Icons.Outlined.Shield, null); Spacer(Modifier.width(6.dp)); Text("Manage protected apps") } } }
        item { SettingsSection("Cleaning", Icons.Outlined.CleaningServices) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Large-file threshold"); Text("Files at or above this size are marked Large files.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Box { TextButton({ thresholdMenu = true }) { Text(formatBytes(threshold)); Icon(Icons.Outlined.ArrowDropDown, null) }; DropdownMenu(thresholdMenu, { thresholdMenu = false }) { listOf(100L * 1024 * 1024, 500L * 1024 * 1024, 1024L * 1024 * 1024).forEach { size -> DropdownMenuItem(text = { Text(formatBytes(size)) }, onClick = { threshold = size; vm.setLargeThreshold(size); thresholdMenu = false }) } } } } } }
        item { SettingsSection("Accessibility", Icons.Outlined.Accessibility) { val enabled = AutoOptimiserViewModel.isAccessibilityEnabled(LocalContext.current); Text(if (enabled) "Enabled · used only during a queue you start" else "Disabled · required for multi-app stop automation", color = if (enabled) SuccessColor else WarningColor); OutlinedButton(vm::openAccessibilitySettings, Modifier.fillMaxWidth()) { Text("Open Accessibility settings") } } }
        item { SettingsSection("Privacy", Icons.Outlined.Security) { Text("Device data is processed locally. Auto Optimiser does not collect accounts, contacts, location, messages, or private app data.", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Folder scans use Android's Storage Access Framework and only include folders you choose.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        item { SettingsSection("About", Icons.Outlined.Info) { Text("Auto Optimiser", fontWeight = FontWeight.SemiBold); Text("A local, explainable device utility.", color = MaterialTheme.colorScheme.onSurfaceVariant); TextButton({ nav.navigate("about") }) { Text("About and support") } } }
    }
}
@Composable private fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) { Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }; Divider(); content() } } }
@Composable private fun ToggleRow(title: String, detail: String, checked: Boolean, onChange: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked, onChange) } }

@Composable
private fun AboutScreen(nav: NavHostController) {
    val context = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TopAppBar(title = { Text("About") }, navigationIcon = { IconButton({ nav.popBackStack() }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent), scrollBehavior = null) }
        item { DeveloperBlock() }
        item { Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text("Build information", fontWeight = FontWeight.SemiBold); FactRow("Application", "Auto Optimiser"); FactRow("Version", BuildConfig.VERSION_NAME); FactRow("Version code", BuildConfig.VERSION_CODE.toString()); FactRow("Build type", BuildConfig.BUILD_TYPE) } } }
        item { LinkButton("GitHub", "github.com/mkr-infinity", "https://github.com/mkr-infinity", context); LinkButton("Instagram", "instagram.com/mkr_infinity", "https://www.instagram.com/mkr_infinity", context); LinkButton("Telegram", "t.me/mkr_infinity", "https://t.me/mkr_infinity", context); LinkButton("Website", "mkr-infinity.github.io", "https://mkr-infinity.github.io", context) }
        item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Support the developer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text("Auto Optimiser is built independently. If you find it useful, you can support its continued development.", color = MaterialTheme.colorScheme.onSurfaceVariant); Button({ openUrl(context, "https://buymeacoffee.com/mkr_infinity") }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Language, null); Spacer(Modifier.width(8.dp)); Text("Buy Me a Coffee") } } } }
    }
}

@Composable
private fun DeveloperBlock() { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { GithubAvatar(); Spacer(Modifier.width(14.dp)); Column { Text("Mohammad Kaif Raja", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text("Independent developer", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Auto Optimiser", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) } } }

@Composable
private fun GithubAvatar() {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(Unit) { bitmap = withContext(Dispatchers.IO) { runCatching { (URL("https://github.com/mkr-infinity.png").openConnection() as HttpURLConnection).apply { connectTimeout = 2500; readTimeout = 2500 }.inputStream.use(BitmapFactory::decodeStream) }.getOrNull() } }
    if (bitmap != null) Image(bitmap!!.asImageBitmap(), "Mohammad Kaif Raja GitHub avatar", Modifier.size(70.dp).clip(CircleShape), contentScale = ContentScale.Crop)
    else Surface(Modifier.size(70.dp), CircleShape, color = MaterialTheme.colorScheme.primary) { Box(contentAlignment = Alignment.Center) { Text("MK", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) } }
}
@Composable private fun LinkButton(label: String, visible: String, url: String, context: Context) { OutlinedButton({ openUrl(context, url) }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.OpenInNew, null); Spacer(Modifier.width(8.dp)); Text("$label · $visible") } }
private fun openUrl(context: Context, url: String) { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }

@Composable
private fun OnboardingFlow(vm: AutoOptimiserViewModel) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableStateOf(0) }
    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> uri?.let(vm::saveTreeUri) }
    val accessibilityEnabled = AutoOptimiserViewModel.isAccessibilityEnabled(context)
    val pages = listOf("Welcome", "Accessibility", "Storage & files", "Notifications", "Automation", "Ready")
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 24.dp, vertical = 26.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Image(painterResource(R.drawable.ic_brand), null, Modifier.size(42.dp)); Text("${page + 1} / ${pages.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(Modifier.height(34.dp))
        LinearProgressIndicator({ (page + 1f) / pages.size }, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))
        when (page) {
            0 -> OnboardingWelcome()
            1 -> PermissionPage(Icons.Outlined.Accessibility, "Accessibility, only when you ask", "Auto Optimiser uses Accessibility access to automate repetitive navigation through Android App Info screens when you choose to optimise multiple applications.", "It does not continuously monitor your personal content and stays idle until a queue is started.", if (accessibilityEnabled) "Enabled" else "Open Accessibility settings", { if (!accessibilityEnabled) vm.openAccessibilitySettings() else page++ }, accessibilityEnabled)
            2 -> PermissionPage(Icons.Outlined.FolderOpen, "Files you choose", "Choose a folder such as Downloads when you want to inspect large files, duplicates, media, APKs, and archives.", "Auto Optimiser cannot read other apps' private data and will not scan folders you did not grant.", if (vm.selectedTreeUri == null) "Choose a folder" else "Folder selected", { if (vm.selectedTreeUri == null) folderLauncher.launch(null) else page++ }, vm.selectedTreeUri != null)
            3 -> PermissionPage(Icons.Outlined.Description, "Notifications are optional", "The core app does not require notification access. Android may show its own confirmation UI for system actions.", "Auto Optimiser does not read your notifications or messages.", "Continue without notifications", { page++ }, true)
            4 -> PermissionPage(Icons.Outlined.Tune, "Automation stays lightweight", "Optional automatic optimisation schedules a quick device check with WorkManager. It does not continuously poll or silently stop apps.", "Deep scans and app stopping always require an action you start in the app.", "Continue", { page++ }, true)
            5 -> ReadyPage(vm, accessibilityEnabled, { page = 1 }, { page = 2 }, { vm.completeOnboarding() })
        }
        Spacer(Modifier.weight(1f))
        if (page > 0 && page < 5) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { TextButton({ page-- }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null); Spacer(Modifier.width(5.dp)); Text("Back") }; TextButton({ page++ }) { Text("Skip"); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null) } }
    }
}

@Composable private fun OnboardingWelcome() { Column(verticalArrangement = Arrangement.spacedBy(18.dp)) { Text("Make the next action obvious.", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold); Text("Auto Optimiser is a local utility for understanding app, storage, memory, and battery information Android makes available.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Icon(Icons.Outlined.Security, null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary); Text("Analyse → explain → choose → verify", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text("No promises that Android cannot keep. No silent deletion. No cloud account.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
@Composable private fun PermissionPage(icon: ImageVector, title: String, why: String, notDo: String, button: String, onClick: () -> Unit, enabled: Boolean) { Column(verticalArrangement = Arrangement.spacedBy(18.dp)) { Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold); Text(why, style = MaterialTheme.typography.bodyLarge); Card(shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("What it does", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary); Text(why); Text("What it does not do", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary); Text(notDo, color = MaterialTheme.colorScheme.onSurfaceVariant) } }; Button(onClick, Modifier.fillMaxWidth()) { Text(button) } } }
@Composable private fun ReadyPage(vm: AutoOptimiserViewModel, accessibility: Boolean, fixAccessibility: () -> Unit, fixStorage: () -> Unit, finish: () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(18.dp)) { Text("Ready when you are.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold); Text("Optional access is never a requirement for browsing the app. Enable only what you plan to use.", color = MaterialTheme.colorScheme.onSurfaceVariant); ChecklistRow("Accessibility", if (accessibility) "Enabled" else "Disabled", accessibility, fixAccessibility); ChecklistRow("Storage access", if (vm.selectedTreeUri != null) "Folder selected" else "Not selected", vm.selectedTreeUri != null, fixStorage); ChecklistRow("Notifications", "Optional", true, {}); ChecklistRow("Automation", "Disabled", true, {}); Button(finish, Modifier.fillMaxWidth().height(52.dp)) { Text("Start using Auto Optimiser") } } }
@Composable private fun ChecklistRow(label: String, state: String, okay: Boolean, onFix: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(onClick = onFix).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (okay) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber, null, tint = if (okay) SuccessColor else WarningColor); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(label, fontWeight = FontWeight.Medium); Text(state, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; if (!okay) TextButton(onFix) { Text("Fix") } } }

@Composable
private fun ActiveProgressCard(progress: StopProgress) { Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text("Optimising selected apps", fontWeight = FontWeight.SemiBold); Text(progress.item?.label ?: "Preparing", style = MaterialTheme.typography.titleMedium); Text("${progress.current.coerceAtMost(progress.total)} of ${progress.total}"); LinearProgressIndicator({ if (progress.total == 0) 0f else progress.current.toFloat() / progress.total }, Modifier.fillMaxWidth()); Text(progress.step.humanLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); TextButton({ StopSession.cancel() }) { Icon(Icons.Outlined.Close, null); Spacer(Modifier.width(5.dp)); Text("Cancel") } } } }
@Composable private fun OptimisationOverlay(vm: AutoOptimiserViewModel) { val progress by vm.progress.collectAsStateWithLifecycle(); if (progress.step != StopStep.IDLE && progress.step != StopStep.COMPLETED && progress.step != StopStep.CANCELLED) AlertDialog(onDismissRequest = {}, icon = { Icon(Icons.Outlined.Tune, null) }, title = { Text("Auto Optimiser") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(progress.item?.label ?: "Preparing"); Text("${progress.current.coerceAtMost(progress.total)} of ${progress.total}"); LinearProgressIndicator({ if (progress.total == 0) 0f else progress.current.toFloat() / progress.total }, Modifier.fillMaxWidth()); Text(progress.step.humanLabel(), style = MaterialTheme.typography.bodySmall) } }, confirmButton = { TextButton(vm::cancelOptimisation) { Text("Cancel") } }) }
@Composable private fun OptimisationResultDialog(vm: AutoOptimiserViewModel) { val result by vm.progress.collectAsStateWithLifecycle(); if (result.total > 0 && result.step in setOf(StopStep.COMPLETED, StopStep.CANCELLED)) AlertDialog(onDismissRequest = vm::dismissOptimisationResult, icon = { Icon(Icons.Outlined.CheckCircle, null) }, title = { Text(if (result.cancelled) "Optimisation cancelled" else "Optimisation complete") }, text = { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Text("Successful: ${result.results.count { it.outcome == StopOutcome.SUCCESS }}"); Text("Skipped: ${result.results.count { it.outcome == StopOutcome.SKIPPED }}"); Text("Failed: ${result.results.count { it.outcome == StopOutcome.FAILED }}"); result.results.filter { it.outcome != StopOutcome.SUCCESS }.take(5).forEach { Text("${it.item.label}: ${it.detail}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }, confirmButton = { TextButton(vm::dismissOptimisationResult) { Text("Done") } }) }
private fun StopStep.humanLabel(): String = when (this) { StopStep.OPENING_APP_INFO -> "Opening App Info…"; StopStep.WAITING_FOR_APP_INFO -> "Waiting for Android…"; StopStep.FINDING_STOP_BUTTON -> "Finding Stop action…"; StopStep.WAITING_FOR_CONFIRMATION -> "Waiting for confirmation…"; StopStep.VERIFYING -> "Verifying the stop…"; StopStep.RETURNING -> "Returning to Auto Optimiser…"; StopStep.CANCELLED -> "Cancelled"; StopStep.COMPLETED -> "Complete"; StopStep.FAILED, StopStep.TIMED_OUT -> "Unable to complete"; StopStep.IDLE -> "Ready" }

private fun formatBytes(bytes: Long): String { if (bytes < 1024) return "$bytes B"; val units = arrayOf("KB", "MB", "GB", "TB"); var value = bytes.toDouble(); var i = -1; while (value >= 1024 && i < units.lastIndex) { value /= 1024; i++ }; return String.format(Locale.getDefault(), "%.1f %s", value, units[i]) }
