package com.sololedger

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.CoreAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.GlassBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.alpha as composeAlpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.sololedger.data.EntryDraft
import com.sololedger.data.EntryKind
import com.sololedger.data.EntryRow
import com.sololedger.data.PreferencesState
import com.sololedger.data.Repo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

private enum class Stage {
    Splash, Onboarding, Home, History, Reports, Bin, Settings, ProfileEdit
}

import androidx.activity.compose.BackHandler
private enum class NavTab { Home, History, Reports, Bin, Settings }

// ─────────────────────────────────────────────────────────────
//  CURRENCIES (no emojis)
// ─────────────────────────────────────────────────────────────
private data class CurrencyOpt(val sym: String, val code: String, val name: String)
private val CURRENCIES = listOf(
    CurrencyOpt("INR", "INR", "Indian Rupee"),
    CurrencyOpt("$", "USD", "US Dollar"),
    CurrencyOpt("EUR", "EUR", "Euro"),
    CurrencyOpt("GBP", "GBP", "British Pound"),
    CurrencyOpt("JPY", "JPY", "Japanese Yen"),
    CurrencyOpt("KRW", "KRW", "Korean Won"),
    CurrencyOpt("AUD", "AUD", "Australian Dollar"),
    CurrencyOpt("CAD", "CAD", "Canadian Dollar"),
    CurrencyOpt("CHF", "CHF", "Swiss Franc"),
    CurrencyOpt("SGD", "SGD", "Singapore Dollar"),
)

// ─────────────────────────────────────────────────────────────
//  AESTHETICS — 5 unique themes (no emojis)
// ─────────────────────────────────────────────────────────────
private data class Aesthetic(
    val id: Int,
    val name: String,
    val accent: Color,
    val accentMuted: Color,
    val bg: Color,
    val surface: Color,
    val surfaceGlass: Color,
    val surfaceAlt: Color,
    val line: Color,
    val text: Color,
    val muted: Color,
    val success: Color,
    val successMuted: Color,
    val danger: Color,
    val dangerMuted: Color,
    val chart: Color,
    val navPill: Color,
    val gradient: List<Color>,
    val isDark: Boolean,
    val navStyle: Int, // 0=pill slide, 1=scale, 2=underline, 3=fade+scale, 4=gradient fill
)

private val AESTHETICS = listOf(
    // 0 — Aurora (violet glass, pill nav)
    Aesthetic(
        id = 0, name = "Aurora",
        accent = Color(0xFF9D7BEA), accentMuted = Color(0x309D7BEA),
        bg = Color(0xFFF2F0FF), surface = Color(0xFFFFFFFF),
        surfaceGlass = Color(0x08FFFFFF), surfaceAlt = Color(0xFFF0EDFF),
        line = Color(0xFFE8E0FF), text = Color(0xFF1A1630),
        muted = Color(0xFF8B85A8), success = Color(0xFF22C49A),
        successMuted = Color(0x1822C49A), danger = Color(0xFFFF4D6A),
        dangerMuted = Color(0x18FF4D6A), chart = Color(0xFF7B5FE8),
        navPill = Color(0xFF9D7BEA),
        gradient = listOf(Color(0xFF9D7BEA), Color(0xFF6B5FE8), Color(0xFF4D8BEA)),
        isDark = false, navStyle = 0,
    ),
    // 1 — Ocean (teal glass, underline nav)
    Aesthetic(
        id = 1, name = "Ocean",
        accent = Color(0xFF22D3EE), accentMuted = Color(0x1822D3EE),
        bg = Color(0xFFF0F9FF), surface = Color(0xFFFFFFFF),
        surfaceGlass = Color(0x08FFFFFF), surfaceAlt = Color(0xFFE8F6FF),
        line = Color(0xFFD4EDFF), text = Color(0xFF0C2535),
        muted = Color(0xFF5B8DA8), success = Color(0xFF10B981),
        successMuted = Color(0x1810B981), danger = Color(0xFFEF4444),
        dangerMuted = Color(0x18EF4444), chart = Color(0xFF06B6D4),
        navPill = Color(0xFF22D3EE),
        gradient = listOf(Color(0xFF06B6D4), Color(0xFF22D3EE), Color(0xFF38BDF8)),
        isDark = false, navStyle = 2,
    ),
    // 2 — Dusk (amber/warm, scale nav)
    Aesthetic(
        id = 2, name = "Dusk",
        accent = Color(0xFFFF9A3C), accentMuted = Color(0x18FF9A3C),
        bg = Color(0xFFFFF8F0), surface = Color(0xFFFFFFFF),
        surfaceGlass = Color(0x08FFFFFF), surfaceAlt = Color(0xFFFFF0E4),
        line = Color(0xFFFFE4C8), text = Color(0xFF2D1800),
        muted = Color(0xFFA07840), success = Color(0xFF22C49A),
        successMuted = Color(0x1822C49A), danger = Color(0xFFFF4D6A),
        dangerMuted = Color(0x18FF4D6A), chart = Color(0xFFFF8C3C),
        navPill = Color(0xFFFF9A3C),
        gradient = listOf(Color(0xFFFF8C3C), Color(0xFFFF9A3C), Color(0xFFFFB03C)),
        isDark = false, navStyle = 1,
    ),
    // 3 — Neon Night (hot pink, gradient fill nav)
    Aesthetic(
        id = 3, name = "Neon Night",
        accent = Color(0xFFFF2D7B), accentMuted = Color(0x30FF2D7B),
        bg = Color(0xFF0A0A14), surface = Color(0xFF141428),
        surfaceGlass = Color(0x10141428), surfaceAlt = Color(0xFF1C1C38),
        line = Color(0xFF2C2C55), text = Color(0xFFF0F0FF),
        muted = Color(0xFF7070A0), success = Color(0xFF00E5A0),
        successMuted = Color(0x2000E5A0), danger = Color(0xFFFF4D7B),
        dangerMuted = Color(0x20FF4D7B), chart = Color(0xFFFF2D7B),
        navPill = Color(0xFFFF2D7B),
        gradient = listOf(Color(0xFFFF2D7B), Color(0xFFFF6B9D), Color(0xFFFF2D7B)),
        isDark = true, navStyle = 4,
    ),
    // 4 — Midnight (cobalt/pitch, fade+scale nav)
    Aesthetic(
        id = 4, name = "Midnight",
        accent = Color(0xFF4F8FFF), accentMuted = Color(0x204F8FFF),
        bg = Color(0xFF080D1A), surface = Color(0xFF0F1729),
        surfaceGlass = Color(0x100F1729), surfaceAlt = Color(0xFF162240),
        line = Color(0xFF1E3058), text = Color(0xFFF0F4FF),
        muted = Color(0xFF6070A0), success = Color(0xFF10B981),
        successMuted = Color(0x2010B981), danger = Color(0xFFFF4D6A),
        dangerMuted = Color(0x20FF4D6A), chart = Color(0xFF4F8FFF),
        navPill = Color(0xFF4F8FFF),
        gradient = listOf(Color(0xFF1E40FF), Color(0xFF4F8FFF), Color(0xFF1E88FF)),
        isDark = true, navStyle = 3,
    ),
)

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
private val groupDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.getDefault())

// ─────────────────────────────────────────────────────────────
//  SOLO APP — Root composable
// ─────────────────────────────────────────────────────────────
@Composable
fun SoloApp(repo: Repo) {
    val prefs by repo.prefs.collectAsState(initial = PreferencesState())
    val entries by repo.entries.collectAsState(initial = emptyList())
    val binEntries by repo.binEntries.collectAsState(initial = emptyList())

    val aesthetic = AESTHETICS.getOrElse(prefs.aestheticIdx) { AESTHETICS[0] }
    val isDark = prefs.darkMode xor aesthetic.isDark

    val bg = if (isDark) aesthetic.bg else aesthetic.bg
    val surface = if (isDark) aesthetic.surface else aesthetic.surface
    val surfaceAlt = if (isDark) aesthetic.surfaceAlt else aesthetic.surfaceAlt
    val line = if (isDark) aesthetic.line else aesthetic.line
    val text = if (isDark) aesthetic.text else aesthetic.text
    val muted = if (isDark) aesthetic.muted else aesthetic.muted
    val accent = aesthetic.accent
    val success = aesthetic.success
    val danger = aesthetic.danger
    val chart = aesthetic.chart
    val surfaceGlass = if (isDark) aesthetic.surfaceGlass else Color(0x08FFFFFF)
    val accentM = aesthetic.accentMuted
    val successM = aesthetic.successMuted
    val dangerM = aesthetic.dangerMuted
    val navPill = aesthetic.navPill
    val grad = aesthetic.gradient

    val ctx = LocalContext.current
    var stage by rememberSaveable { mutableStateOf(Stage.Splash) }
    var navTab by rememberSaveable { mutableStateOf(NavTab.Home) }
    var onboardPage by rememberSaveable { mutableStateOf(0) }
    var showEntrySheet by rememberSaveable { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<EntryRow?>(null) }
    var fabAnimKey by rememberSaveable { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        delay(1100)
        stage = if (prefs.onboarded) Stage.Home else Stage.Onboarding
    }

    LaunchedEffect(navTab) { fabAnimKey++ }

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        when (stage) {
            Stage.Splash -> SplashScreen(accent, grad)
            Stage.Onboarding -> OnboardingScreen(
                page = onboardPage,
                aesthetic = aesthetic,
                isDark = isDark,
                surface = surface, surfaceAlt = surfaceAlt,
                line = line, text = text, muted = muted,
                accent = accent, accentM = accentM,
                grad = grad,
                onNext = {
                    if (onboardPage == 0) onboardPage++ else {
                        scope.launch { repo.store.setOnboard(true) }
                        stage = Stage.Home
                    }
                },
                onSkip = {
                    scope.launch { repo.store.setOnboard(true) }
                    stage = Stage.Home
                },
            )
            else -> {
                LedgerScaffold(
                    stage = stage,
                    navTab = navTab,
                    entries = entries,
                    binEntries = binEntries,
                    prefs = prefs,
                    aesthetic = aesthetic,
                    isDark = isDark,
                    fabAnimKey = fabAnimKey,
                    bg = bg, surface = surface, surfaceGlass = surfaceGlass,
                    surfaceAlt = surfaceAlt, line = line, text = text, muted = muted,
                    accent = accent, accentM = accentM, success = success,
                    successM = successM, danger = danger, dangerM = dangerM,
                    chart = chart, navPill = navPill, grad = grad,
                    onNavChange = { navTab = it },
                    onOpenProfile = { stage = Stage.ProfileEdit },
                    onAdd = { editingEntry = null; showEntrySheet = true },
                    onEdit = { editingEntry = it; showEntrySheet = true },
                    onDelete = { scope.launch { repo.delete(it.id) } },
                    onRestore = { scope.launch { repo.restore(it.id) } },
                    onDeleteForever = { scope.launch { repo.deleteForever(it.id) } },
                    onClearBin = { scope.launch { repo.clearBin() } },
                    onSettingsToggle = { key, value -> scope.launch {
                        when (key) {
                            "title" -> repo.store.setShowTitle(value)
                            "cat" -> repo.store.setShowCategory(value)
                            "notes" -> repo.store.setShowNotes(value)
                            "date" -> repo.store.setShowDate(value)
                        }
                    } },
                    onDarkToggle = { scope.launch { repo.store.setDark(!prefs.darkMode) } },
                    onAestheticChange = { scope.launch { repo.store.setAesthetic(it) } },
                    onNameSave = { scope.launch { repo.store.setName(it) } },
                    onSymSave = { scope.launch { repo.store.setSym(it) } },
                    onBackToHome = { stage = when (navTab) {
                        NavTab.Home -> Stage.Home; NavTab.History -> Stage.History
                        NavTab.Reports -> Stage.Reports; NavTab.Bin -> Stage.Bin
                        NavTab.Settings -> Stage.Settings
                    } },
                )

                if (showEntrySheet) {
                    EntrySheet(
                        prefs = prefs, aesthetic = aesthetic, isDark = isDark,
                        entry = editingEntry,
                        surface = surface, surfaceGlass = surfaceGlass,
                        surfaceAlt = surfaceAlt, line = line, text = text, muted = muted,
                        accent = accent, accentM = accentM, success = success,
                        danger = danger,
                        onDismiss = { showEntrySheet = false; editingEntry = null },
                        onSave = { draft ->
                            scope.launch { repo.save(draft) }
                            showEntrySheet = false; editingEntry = null
                        },
                        onDeleteEntry = editingEntry?.id?.let { id ->
                            { scope.launch { repo.delete(id); showEntrySheet = false; editingEntry = null } }
                        },
                    )
                }
            }
        }
    }
}

private val scope get() = rememberCoroutineScope()

// ─────────────────────────────────────────────────────────────
//  LEDGER SCAFFOLD — Nav + FAB + stage routing
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerScaffold(
    stage: Stage, navTab: NavTab,
    entries: List<EntryRow>, binEntries: List<EntryRow>,
    prefs: PreferencesState, aesthetic: Aesthetic, isDark: Boolean,
    fabAnimKey: Long,
    bg: Color, surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, success: Color, successM: Color,
    danger: Color, dangerM: Color, chart: Color, navPill: Color, grad: List<Color>,
    onNavChange: (NavTab) -> Unit,
    onOpenProfile: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (EntryRow) -> Unit,
    onDelete: (EntryRow) -> Unit,
    onRestore: (EntryRow) -> Unit,
    onDeleteForever: (EntryRow) -> Unit,
    onClearBin: () -> Unit,
    onSettingsToggle: (String, Boolean) -> Unit,
    onDarkToggle: (suspend () -> Unit) -> Unit,
    onAestheticChange: (Int) -> Unit,
    onNameSave: (String) -> Unit,
    onSymSave: (String) -> Unit,
    onBackToHome: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ctx = LocalContext.current

    AnimatedContent(
        targetState = stage,
        transitionSpec = {
            (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
        },
        label = "stageTransition",
    ) { targetStage ->
        Scaffold(
            containerColor = bg,
            contentColor = text,
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                if (targetStage in listOf(Stage.Home, Stage.History, Stage.Reports)) {
                    AnimatedFAB(
                        key = fabAnimKey,
                        accent = accent,
                        onClick = onAdd,
                    )
                }
            },
            bottomBar = {
                AnimatedBottomNav(
                    current = navTab,
                    aesthetic = aesthetic,
                    isDark = isDark,
                    accent = accent, navPill = navPill, grad = grad,
                    text = text, muted = muted, surface = surface,
                    onNavigate = onNavChange,
                )
            },
        ) { innerPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                when (targetStage) {
                    Stage.Home -> HomeScreen(
                        entries = entries,
                        prefs = prefs, aesthetic = aesthetic, isDark = isDark,
                        bg = bg, surface = surface, surfaceGlass = surfaceGlass,
                        surfaceAlt = surfaceAlt, line = line, text = text, muted = muted,
                        accent = accent, accentM = accentM, success = success,
                        successM = successM, danger = danger, dangerM = dangerM,
                        chart = chart, grad = grad,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onOpenProfile = onOpenProfile,
                    )
                    Stage.History -> HistoryScreen(
                        entries = entries,
                        prefs = prefs, aesthetic = aesthetic, isDark = isDark,
                        bg = bg, surface = surface, surfaceGlass = surfaceGlass,
                        surfaceAlt = surfaceAlt, line = line, text = text, muted = muted,
                        accent = accent, accentM = accentM, success = success,
                        danger = danger,
                        onEdit = onEdit,
                        onDelete = onDelete,
                    )
                    Stage.Reports -> ReportsScreen(
                        entries = entries,
                        prefs = prefs, aesthetic = aesthetic, isDark = isDark,
                        bg = bg, surface = surface, surfaceGlass = surfaceGlass,
                        surfaceAlt = surfaceAlt, line = line, text = text, muted = muted,
                        accent = accent, accentM = accentM, success = success,
                        successM = successM, danger = danger, dangerM = dangerM,
                        chart = chart,
                    )
                    Stage.Bin -> BinScreen(
                        entries = binEntries,
                        prefs = prefs, aesthetic = aesthetic, isDark = isDark,
                        bg = bg, surface = surface, surfaceGlass = surfaceGlass,
                        surfaceAlt = surfaceAlt, line = line, text = text, muted = muted,
                        accent = accent, danger = danger,
                        onRestore = onRestore,
                        onDeleteForever = onDeleteForever,
                        onClearBin = onClearBin,
                    )
                    Stage.Settings -> SettingsScreen(
                        prefs = prefs, aesthetic = aesthetic, isDark = isDark,
                        bg = bg, surface = surface, surfaceGlass = surfaceGlass,
                        surfaceAlt = surfaceAlt, line = line, text = text, muted = muted,
                        accent = accent, accentM = accentM, success = success,
                        danger = danger,
                        onToggle = { v -> onSettingsToggle("title", v) },
                        onDarkToggle = onDarkToggle,
                        onAestheticChange = onAestheticChange,
                        onOpenProfile = onOpenProfile,
                    )
                    Stage.ProfileEdit -> ProfileEditScreen(
                        prefs = prefs, aesthetic = aesthetic, isDark = isDark,
                        bg = bg, surface = surface, surfaceGlass = surfaceGlass,
                        accent = accent, accentM = accentM, success = success,
                        danger = danger,
                        onBack = onBackToHome,
                        onNameSave = onNameSave,
                        onSymSave = onSymSave,
                    }
                    else -> {}
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SPLASH SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
private fun SplashScreen(accent: Color, grad: List<Color>) {
    val animScale by animateFloatAsState(1f, label = "")
    val animAlpha by animateFloatAsState(1f, label = "")

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.18f), Color.White)
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(animScale).alpha(animAlpha),
        ) {
            Box(
                Modifier.size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(grad)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Wallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp),
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                "Solo Ledger",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = accent,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Offline personal finance",
                color = Color(0xFF8B85A8),
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(32.dp))
            LoadingDots(accent = accent)
        }
    }
}

@Composable private fun LoadingDots(accent: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { i ->
            val alpha by animateFloatAsState(
                targetValue = if (true) 1f else 0.3f,
                animationSpec = tween(600),
                label = "",
            )
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = alpha)),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ONBOARDING SCREEN — beautiful, modern
// ─────────────────────────────────────────────────────────────
private val onboardSlides = listOf(
    OnboardSlide(
        icon = Icons.Rounded.Wallet,
        title = "Your money,\nfully private",
        body = "Every entry is stored only on your device. No accounts, no servers, no sync. Just your data.",
        hint = "100% offline",
    ),
    OnboardSlide(
        icon = Icons.Rounded.BarChart,
        title = "Track everything.\nSee it clearly.",
        body = "Add income and expenses in seconds. Get instant insights and beautiful reports.",
        hint = "Instant reports",
    ),
    OnboardSlide(
        icon = Icons.Rounded.Star,
        title = "Make it\nuniquely yours",
        body = "Choose from 5 aesthetics. Switch themes. Set your own currency. This app is yours.",
        hint = "5 themes",
    ),
)
private data class OnboardSlide(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val hint: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingScreen(
    page: Int,
    aesthetic: Aesthetic,
    isDark: Boolean,
    surface: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, grad: List<Color>,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val slide = onboardSlides.getOrElse(page) { onboardSlides[0] }
    val pages = onboardSlides.size

    val titleAlpha by animateFloatAsState(if (true) 1f else 0f, label = "")
    val bodyAlpha by animateFloatAsState(if (true) 1f else 0f, label = "")

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.08f),
                        accent.copy(alpha = 0.04f),
                        surface,
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar with skip
            Box(Modifier.fillMaxWidth()) {
                TextButton(onClick = onSkip) {
                    Text("Skip", color = muted, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Icon card
            Box(
                Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(accent, accent.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    slide.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(46.dp),
                )
            }

            Spacer(Modifier.height(40.dp))

            // Title
            Text(
                slide.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = text,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                modifier = Modifier.alpha(titleAlpha),
            )

            Spacer(Modifier.height(16.dp))

            // Body
            Text(
                slide.body,
                fontSize = 16.sp,
                color = muted,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.alpha(bodyAlpha).padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(12.dp))

            // Hint chip
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentM)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(slide.hint, color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.weight(1f))

            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages) { i ->
                    val dotScale by animateFloatAsState(if (i == page) 1.3f else 1f, label = "")
                    val dotAlpha by animateFloatAsState(if (i == page) 1f else 0.3f, label = "")
                    Box(
                        Modifier
                            .size(if (i == page) 10.dp else 8.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = dotAlpha)),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Next / Get Started button
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
            ) {
                Text(
                    if (page == pages - 1) "Get Started" else "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ANIMATED BOTTOM NAV — 5 styles, one per aesthetic
// ─────────────────────────────────────────────────────────────
@Composable
private fun AnimatedBottomNav(
    current: NavTab,
    aesthetic: Aesthetic,
    isDark: Boolean,
    accent: Color, navPill: Color, grad: List<Color>,
    text: Color, muted: Color, surface: Color,
    onNavigate: (NavTab) -> Unit,
) {
    val style = aesthetic.navStyle
    val bgColor = if (isDark) aesthetic.surface.copy(alpha = 0.92f) else surface

    Box(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(bgColor)
            .border(
                width = if (isDark) 0.dp else 0.5.dp,
                color = aesthetic.line.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            )
    ) {
        when (style) {
            0 -> PillNav(current, accent, navPill, text, muted, onNavigate)
            1 -> ScaleNav(current, accent, navPill, text, muted, onNavigate)
            2 -> UnderlineNav(current, accent, text, muted, onNavigate)
            3 -> FadeScaleNav(current, accent, navPill, text, muted, onNavigate)
            else -> GradientFillNav(current, accent, grad, text, muted, onNavigate)
        }
    }
}

@Composable
private fun PillNav(
    current: NavTab, accent: Color, navPill: Color,
    text: Color, muted: Color, onNavigate: (NavTab) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavTab.entries.forEach { tab ->
            val selected = tab == current
            val pillAnim by animateFloatAsState(if (selected) 1f else 0f, label = "")
            Box(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = pillAnim * 0.18f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigate(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        navIcon(tab),
                        contentDescription = null,
                        tint = if (selected) accent else muted,
                        modifier = Modifier.size(22.dp),
                    )
                    AnimatedVisibility(visible = selected) {
                        Text(
                            navLabel(tab),
                            color = accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScaleNav(
    current: NavTab, accent: Color, navPill: Color,
    text: Color, muted: Color, onNavigate: (NavTab) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavTab.entries.forEach { tab ->
            val selected = tab == current
            val scale by animateFloatAsState(if (selected) 1.15f else 1f, label = "")
            val alpha by animateFloatAsState(if (selected) 1f else 0.5f, label = "")
            Column(
                Modifier
                    .scale(scale)
                    .alpha(alpha)
                    .clickable { onNavigate(tab) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(if (selected) 48.dp else 44.dp)
                        .clip(CircleShape)
                        .background(if (selected) accent else Color.Transparent)
                        .padding(if (selected) 0.dp else 0.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        navIcon(tab),
                        contentDescription = null,
                        tint = if (selected) Color.White else muted,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    navLabel(tab),
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) accent else muted,
                )
            }
        }
    }
}

@Composable
private fun UnderlineNav(
    current: NavTab, accent: Color,
    text: Color, muted: Color, onNavigate: (NavTab) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavTab.entries.forEach { tab ->
            val selected = tab == current
            val underlineScale by animateFloatAsState(if (selected) 1f else 0f, label = "")
            Column(
                Modifier
                    .clickable { onNavigate(tab) }
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    navIcon(tab),
                    contentDescription = null,
                    tint = if (selected) accent else muted,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    navLabel(tab),
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) accent else muted,
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier.fillMaxWidth(if (selected) 1f else 0f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(accent),
                )
            }
        }
    }
}

@Composable
private fun FadeScaleNav(
    current: NavTab, accent: Color, navPill: Color,
    text: Color, muted: Color, onNavigate: (NavTab) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavTab.entries.forEach { tab ->
            val selected = tab == current
            val scale by animateFloatAsState(if (selected) 1.1f else 0.9f, label = "")
            val alpha by animateFloatAsState(if (selected) 1f else 0.45f, label = "")
            Column(
                Modifier
                    .scale(scale)
                    .alpha(alpha)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) accent.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable { onNavigate(tab) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    navIcon(tab),
                    contentDescription = null,
                    tint = if (selected) accent else muted,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    navLabel(tab),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) accent else muted,
                )
            }
        }
    }
}

@Composable
private fun GradientFillNav(
    current: NavTab, accent: Color, grad: List<Color>,
    text: Color, muted: Color, onNavigate: (NavTab) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavTab.entries.forEach { tab ->
            val selected = tab == current
            val alpha by animateFloatAsState(if (selected) 1f else 0.4f, label = "")
            Box(
                Modifier
                    .alpha(alpha)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (selected) Modifier.background(
                            Brush.linearGradient(grad),
                        ) else Modifier
                    )
                    .clickable { onNavigate(tab) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    navIcon(tab),
                    contentDescription = null,
                    tint = if (selected) Color.White else muted,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

private fun navIcon(tab: NavTab) = when (tab) {
    NavTab.Home -> Icons.Rounded.Home
    NavTab.History -> Icons.Rounded.History
    NavTab.Reports -> Icons.Rounded.Insights
    NavTab.Bin -> Icons.Rounded.Delete
    NavTab.Settings -> Icons.Rounded.Settings
}

private fun navLabel(tab: NavTab) = when (tab) {
    NavTab.Home -> "Home"
    NavTab.History -> "History"
    NavTab.Reports -> "Reports"
    NavTab.Bin -> "Bin"
    NavTab.Settings -> "Settings"
}

// ─────────────────────────────────────────────────────────────
//  ANIMATED FAB
// ─────────────────────────────────────────────────────────────
@Composable
private fun AnimatedFAB(
    key: Any,
    accent: Color,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "",
    )
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = accent,
        contentColor = Color.White,
        shape = CircleShape,
    ) {
        Icon(Icons.Rounded.Add, contentDescription = "Add entry")
    }
}

// ─────────────────────────────────────────────────────────────
//  HOME SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
private fun HomeScreen(
    entries: List<EntryRow>,
    prefs: PreferencesState,
    aesthetic: Aesthetic,
    isDark: Boolean,
    bg: Color, surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, success: Color, successM: Color,
    danger: Color, dangerM: Color, chart: Color, grad: List<Color>,
    onEdit: (EntryRow) -> Unit,
    onDelete: (EntryRow) -> Unit,
    onOpenProfile: () -> Unit,
) {
    val balance = entries.filter { it.kind != "income" || true }
        .sumOf { if (it.kind == "income") it.amountMinor else -it.amountMinor }
    val income = entries.filter { it.kind == "income" }.sumOf { it.amountMinor }
    val expense = entries.filter { it.kind == "expense" }.sumOf { it.amountMinor }
    val recent = entries.take(8)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Balance Hero Card
        item {
            HeroCard(
                prefs = prefs, aesthetic = aesthetic, isDark = isDark,
                balance = balance, income = income, expense = expense,
                bg = bg, surface = surface, surfaceGlass = surfaceGlass,
                surfaceAlt = surfaceAlt, line = line, text = text, muted = muted,
                accent = accent, accentM = accentM, success = success,
                successM = successM, danger = danger, dangerM = dangerM,
                grad = grad,
                onOpenProfile = onOpenProfile,
            )
        }

        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MiniStatCard(
                    label = "Income",
                    value = moneyFmt(income, prefs.symbol),
                    color = success,
                    bgColor = successM,
                    surface = surface, text = text, muted = muted,
                    modifier = Modifier.weight(1f),
                )
                MiniStatCard(
                    label = "Expense",
                    value = moneyFmt(expense, prefs.symbol),
                    color = danger,
                    bgColor = dangerM,
                    surface = surface, text = text, muted = muted,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Recent entries
        if (recent.isNotEmpty()) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Recent",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = text,
                    )
                    Text(
                        "${entries.size} total",
                        color = muted,
                        fontSize = 12.sp,
                    )
                }
            }
            items(recent, key = { it.id }) { entry ->
                EntryRowCard(
                    entry = entry,
                    prefs = prefs,
                    aesthetic = aesthetic,
                    isDark = isDark,
                    surface = surface, line = line, text = text, muted = muted,
                    accent = accent, success = success, danger = danger,
                    onClick = { onEdit(entry) },
                    onDelete = { onDelete(entry) },
                )
            }
        } else {
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.Wallet,
                    title = "No entries yet",
                    body = "Tap the + button to add your first entry",
                    accent = accent,
                    surface = surface,
                    text = text,
                    muted = muted,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  HISTORY SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
private fun HistoryScreen(
    entries: List<EntryRow>,
    prefs: PreferencesState,
    aesthetic: Aesthetic,
    isDark: Boolean,
    bg: Color, surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, success: Color, danger: Color,
    onEdit: (EntryRow) -> Unit,
    onDelete: (EntryRow) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filterKind by rememberSaveable { mutableStateOf("all") }

    val filtered = entries.filter { e ->
        val matchesQuery = query.isBlank() ||
                e.title.contains(query, ignoreCase = true) ||
                e.category.contains(query, ignoreCase = true) ||
                e.notes.contains(query, ignoreCase = true)
        val matchesKind = when (filterKind) {
            "income" -> e.kind == "income"
            "expense" -> e.kind == "expense"
            "transfer" -> e.kind == "transfer"
            else -> true
        }
        matchesQuery && matchesKind
    }

    val groups = filtered.groupBy { e ->
        Instant.ofEpochMilli(e.occurredAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(groupDateFormatter)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search + Filter header
        Column(
            Modifier.fillMaxWidth()
                .background(bg)
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.statusBars),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search entries...", color = muted) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = muted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = line,
                    focusedContainerColor = surface,
                    unfocusedContainerColor = surface,
                    focusedTextColor = text,
                    unfocusedTextColor = text,
                ),
                shape = RoundedCornerShape(14.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "All", "income" to "Income", "expense" to "Expense", "transfer" to "Transfer").forEach { (k, v) ->
                    FilterChip(
                        selected = filterKind == k,
                        onClick = { filterKind = k },
                        label = { Text(v, fontSize = 12.sp) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accent,
                            selectedLabelColor = Color.White,
                            containerColor = surface,
                            labelColor = muted,
                        ),
                    )
                }
            }
        }

        if (groups.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyStateCard(
                    icon = Icons.Rounded.History,
                    title = "No entries found",
                    body = if (query.isNotBlank()) "Try a different search" else "Add your first entry",
                    accent = accent,
                    surface = surface,
                    text = text,
                    muted = muted,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                groups.forEach { (groupLabel, groupEntries) ->
                    item {
                        Text(
                            groupLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = muted,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                    items(groupEntries, key = { it.id }) { entry ->
                        EntryRowCard(
                            entry = entry,
                            prefs = prefs,
                            aesthetic = aesthetic,
                            isDark = isDark,
                            surface = surface, line = line, text = text, muted = muted,
                            accent = accent, success = success, danger = danger,
                            onClick = { onEdit(entry) },
                            onDelete = { onDelete(entry) },
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  REPORTS SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
private fun ReportsScreen(
    entries: List<EntryRow>,
    prefs: PreferencesState,
    aesthetic: Aesthetic,
    isDark: Boolean,
    bg: Color, surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, success: Color, successM: Color,
    danger: Color, dangerM: Color, chart: Color,
) {
    var period by rememberSaveable { mutableStateOf("all") }
    var customStart by rememberSaveable { mutableStateOf<Long?>(null) }
    var customEnd by rememberSaveable { mutableStateOf<Long?>(null) }

    val filtered = entries.filter { e ->
        val date = Instant.ofEpochMilli(e.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate()
        when (period) {
            "7d" -> !date.isBefore(LocalDate.now().minusDays(7))
            "3m" -> !date.isBefore(LocalDate.now().minusMonths(3))
            "custom" -> {
                val start = customStart?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                    ?: LocalDate.MIN
                val end = customEnd?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                    ?: LocalDate.MAX
                !date.isBefore(start) && !date.isAfter(end)
            }
            else -> true
        }
    }

    val income = filtered.filter { it.kind == "income" }.sumOf { it.amountMinor }
    val expense = filtered.filter { it.kind == "expense" }.sumOf { it.amountMinor }
    val net = income - expense
    val categories = filtered
        .filter { it.kind == "expense" }
        .groupBy { it.category.ifBlank { "Uncategorised" } }
        .mapValues { it.value.sumOf { e -> e.amountMinor } }
        .toList()
        .sortedByDescending { it.second }
    val chartEntries = filtered.takeLast(14)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Reports", fontSize = 24.sp, fontWeight = FontWeight.Black, color = text)
        }
        // Period chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "All", "7d" to "7 Days", "3m" to "3 Mo", "custom" to "Custom").forEach { (k, v) ->
                    FilterChip(
                        selected = period == k,
                        onClick = { period = k },
                        label = { Text(v, fontSize = 12.sp) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accent,
                            selectedLabelColor = Color.White,
                            containerColor = surface,
                            labelColor = muted,
                        ),
                    )
                )
            }
        }
        if (period == "custom") {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { openDatePicker(LocalContext.current, customStart) { customStart = it } },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(customStart?.let { fmtDate(it) } ?: "Start date", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { openDatePicker(LocalContext.current, customEnd) { customEnd = it } },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(customEnd?.let { fmtDate(it) } ?: "End date", fontSize = 12.sp)
                    }
                }
            }
        }
        // Performance card
        item {
            GlassCard(surface = surface, line = line, isDark = isDark) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Performance", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = text)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MiniStatCard(
                            label = "Income", value = moneyFmt(income, prefs.symbol),
                            color = success, bgColor = successM,
                            surface = surface, text = text, muted = muted, modifier = Modifier.weight(1f),
                        )
                        MiniStatCard(
                            label = "Expense", value = moneyFmt(expense, prefs.symbol),
                            color = danger, bgColor = dangerM,
                            surface = surface, text = text, muted = muted, modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Net", color = muted, fontSize = 14.sp)
                        Text(
                            "${if (net >= 0) "+" else ""}${prefs.symbol}${moneyFmt(net, "")}",
                            color = accent, fontSize = 22.sp, fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
        // Chart
        item {
            GlassCard(surface = surface, line = line, isDark = isDark) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cash Flow", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = text)
                    if (chartEntries.isEmpty()) {
                        Text("No data for this period", color = muted, fontSize = 13.sp)
                    } else {
                        AnimatedBarChart(
                            entries = chartEntries,
                            accent = chart,
                            surfaceAlt = surfaceAlt,
                            line = line,
                        )
                    }
                }
            }
        }
        // Category breakdown
        if (categories.isNotEmpty()) {
            item {
                GlassCard(surface = surface, line = line, isDark = isDark) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Categories", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = text)
                        val maxVal = categories.firstOrNull()?.second ?: 1L
                        categories.take(6).forEach { (cat, amount) ->
                            ProgressRow(
                                label = cat,
                                value = amount,
                                max = maxVal,
                                accent = accent,
                                surfaceAlt = surfaceAlt,
                                text = text,
                                muted = muted,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  BIN SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
private fun BinScreen(
    entries: List<EntryRow>,
    prefs: PreferencesState,
    aesthetic: Aesthetic,
    isDark: Boolean,
    bg: Color, surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, danger: Color,
    onRestore: (EntryRow) -> Unit,
    onDeleteForever: (EntryRow) -> Unit,
    onClearBin: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Bin", fontSize = 24.sp, fontWeight = FontWeight.Black, color = text)
                    Text("${entries.size} deleted entries", color = muted, fontSize = 13.sp)
                }
                if (entries.isNotEmpty()) {
                    TextButton(onClick = onClearBin) {
                        Icon(Icons.Rounded.Delete, null, tint = danger, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Empty", color = danger, fontSize = 13.sp)
                    }
                }
            }
        }
        if (entries.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateCard(
                        icon = Icons.Rounded.Delete,
                        title = "Bin is empty",
                        body = "Deleted entries will appear here",
                        accent = accent,
                        surface = surface,
                        text = text,
                        muted = muted,
                    )
                }
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                EntryRowCard(
                    entry = entry,
                    prefs = prefs,
                    aesthetic = aesthetic,
                    isDark = isDark,
                    surface = surface, line = line, text = text, muted = muted,
                    accent = accent, success = success, danger = danger,
                    onClick = { onRestore(entry) },
                    onDelete = { onDeleteForever(entry) },
                    showRestore = true,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SETTINGS SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
private fun SettingsScreen(
    prefs: PreferencesState,
    aesthetic: Aesthetic,
    isDark: Boolean,
    bg: Color, surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, success: Color, danger: Color,
    onToggle: (Boolean) -> Unit,
    onDarkToggle: (suspend () -> Unit) -> Unit,
    onAestheticChange: (Int) -> Unit,
    onOpenProfile: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Black, color = text) }

        // Profile card
        item {
            GlassCard(surface = surface, line = line, isDark = isDark) {
                Row(
                    Modifier.fillMaxWidth().clickable { onOpenProfile() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassCircle(size = 48.dp, color = accent, isDark = isDark) {
                            Icon(Icons.Rounded.Person, null, tint = Color.White)
                        }
                        Column {
                            Text(if (prefs.name.isBlank()) "Your Profile" else prefs.name, fontWeight = FontWeight.Bold, color = text)
                            Text("Tap to edit name & currency", color = muted, fontSize = 12.sp)

        // Accent color section
        item {
            GlassCard(surface = surface, line = line, isDark = isDark) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Accent Color", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = text)
                    Text("Choose your app's highlight color", color = muted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AESTHETICS.forEach { a ->
                            Box(
                                Modifier.size(36.dp)
                                    .clip(CircleShape)
                                    .background(a.accent)
                                    .border(
                                        2.dp,
                                        if (a.id == prefs.aestheticIdx) text else Color.Transparent,
                                        CircleShape,
                                    )
                                    .clickable { onAestheticChange(a.id) }
                            )
                        }
                    }
                }
            }
        }
                        }
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = muted)
                }
            }
        }

        // Field visibility
        item {
            GlassCard(surface = surface, line = line, isDark = isDark) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Field Visibility", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = text)
                    ToggleRow("Title field", prefs.showTitle, { onSettingsToggle("title", it) }, accent, surfaceAlt, text, muted)
                    ToggleRow("Category field", prefs.showCategory, { onSettingsToggle("cat", it) }, accent, surfaceAlt, text, muted)
                    ToggleRow("Notes field", prefs.showNotes, { onSettingsToggle("notes", it) }, accent, surfaceAlt, text, muted)
                    ToggleRow("Date field", prefs.showDate, { onSettingsToggle("date", it) }, accent, surfaceAlt, text, muted)
                }
            }
        }

        // Theme / Dark mode
        item {
            GlassCard(surface = surface, line = line, isDark = isDark) {
                Row(
                    Modifier.fillMaxWidth().clickable { onDarkToggle {} }
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            if (prefs.darkMode xor aesthetic.isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            null, tint = accent,
                        )
                        Text("Dark Mode", fontWeight = FontWeight.SemiBold, color = text)
                    }
                    Switch(
                        checked = prefs.darkMode,
                        onCheckedChange = { onDarkToggle {} },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
                    )
                }
            }
        }

        // Aesthetic picker
        item {
            GlassCard(surface = surface, line = line, isDark = isDark) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Aesthetic", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = text)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AESTHETICS.forEach { aes ->
                            val sel = aes.id == prefs.aestheticIdx
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (sel) accentM else Color.Transparent)
                                    .border(
                                        width = if (sel) 2.dp else 1.dp,
                                        color = if (sel) accent else line,
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    .clickable { onAestheticChange(aes.id) }
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    Modifier.size(32.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(aes.gradient)),
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    aes.name,
                                    fontSize = 9.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sel) accent else muted,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Data
        item {
            GlassCard(surface = surface, line = line, isDark = isDark) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Data", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = text)
                    Text(
                        "All data stored locally on this device. No accounts required.",
                        color = muted, fontSize = 13.sp, lineHeight = 20.sp,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  PROFILE EDIT SCREEN
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditScreen(
    prefs: PreferencesState,
    aesthetic: Aesthetic,
    isDark: Boolean,
    surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, success: Color, danger: Color,
    onBack: () -> Unit,
    onNameSave: (String) -> Unit,
    onSymSave: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(prefs.name) }
    var sym by rememberSaveable { mutableStateOf(prefs.symbol) }
    var symMode by rememberSaveable { mutableStateOf(if (prefs.symbol == "custom") "custom" else "preset") }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    Box(
        Modifier.fillMaxSize()
            .background(aesthetic.bg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Back + Title
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = text)
                    }
                    Text("Profile", fontSize = 20.sp, fontWeight = FontWeight.Black, color = text)
                    Spacer(Modifier.width(48.dp))
                }
            }

            // Avatar + Name card
            item {
                GlassCard(surface = surface, line = line, isDark = isDark) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        GlassCircle(size = 90.dp, color = accent, isDark = isDark) {
                            Text(
                                name.take(1).uppercase().ifBlank { "S" },
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                            )
                        }
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accent,
                                unfocusedBorderColor = line,
                                focusedTextColor = text,
                                unfocusedTextColor = text,
                                focusedLabelColor = accent,
                            ),
                            shape = RoundedCornerShape(14.dp),
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    onNameSave(name)
                                    Toast.makeText(LocalContext.current, "Name saved", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                        ) {
                            Text("Save Name", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Currency card
            item {
                GlassCard(surface = surface, line = line, isDark = isDark) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Currency", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = text)

                        // Mode tabs
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = symMode == "preset",
                                onClick = { symMode = "preset" },
                                label = { Text("Preset", fontSize = 12.sp) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent,
                                    selectedLabelColor = Color.White,
                                    containerColor = surfaceAlt,
                                    labelColor = muted,
                                ),
                            )
                            FilterChip(
                                selected = symMode == "custom",
                                onClick = { symMode = "custom" },
                                label = { Text("Custom", fontSize = 12.sp) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent,
                                    selectedLabelColor = Color.White,
                                    containerColor = surfaceAlt,
                                    labelColor = muted,
                                ),
                            )
                        }

                        if (symMode == "custom") {
                            OutlinedTextField(
                                value = sym,
                                onValueChange = { sym = it.take(6) },
                                label = { Text("Symbol (e.g. Rs., L, Rs)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accent,
                                    unfocusedBorderColor = line,
                                    focusedTextColor = text,
                                    unfocusedTextColor = text,
                                    focusedLabelColor = accent,
                                ),
                                shape = RoundedCornerShape(14.dp),
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        onSymSave(sym.ifBlank { "INR" })
                                        Toast.makeText(LocalContext.current, "Currency saved", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = accent),
                            ) {
                                Text("Save Currency", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { showPicker = true },
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = accentM),
                            ) {
                                Icon(Icons.Rounded.Palette, null, tint = accent)
                                Spacer(Modifier.width(8.dp))
                                Text("Choose Currency", color = accent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Current display
            item {
                GlassCard(surface = surface, line = line, isDark = isDark) {
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Current", color = muted)
                        Text(sym.ifBlank { prefs.symbol }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = accent)
                    }
                }
            }
        }

        // Currency picker sheet
        if (showPicker) {
            ModalBottomSheet(
                onDismissRequest = { showPicker = false },
                containerColor = surface,
                modifier = Modifier.navigationBarsPadding().imePadding(),
            ) {
                Column(
                    Modifier.fillMaxWidth()
                        .padding(20.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Select Currency", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = text)
                    CURRENCIES.forEach { opt ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    sym = opt.sym
                                    scope.launch {
                                        onSymSave(opt.sym)
                                        showPicker = false
                                        Toast.makeText(LocalContext.current, "Currency set to ${opt.name}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .background(if (opt.sym == sym) accentM else Color.Transparent)
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(opt.sym, fontWeight = FontWeight.Bold, color = text)
                                Text(opt.name, color = muted, fontSize = 12.sp)
                            }
                            Text(opt.code, color = muted, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }

        // Support section
        item {
            SupportSection(
                aesthetic = aesthetic,
                isDark = isDark,
                bg = bg, surface = surface, surfaceGlass = surfaceGlass,
                surfaceAlt = surfaceAlt, line = line, text = text, muted = muted,
                accent = accent, accentM = accentM, success = success, danger = danger,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
//  CURRENCY PICKER (preset 10 + custom)
// ─────────────────────────────────────────────────────────────
@Composable
private fun CurrencyPicker(
    symbol: String,
    showPicker: Boolean,
    onShowPicker: (Boolean) -> Unit,
    onSelect: (String, String) -> Unit,
    surface: Color,
    surfaceAlt: Color,
    line: Color,
    text: Color,
    muted: Color,
    accent: Color,
) {
    Box {
        OutlinedTextField(
            value = if (symbol == "custom") "Custom" else CURRENCIES.find { it.sym == symbol }?.let { "${it.sym}  ${it.code}" } ?: symbol,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { onShowPicker(true) },
            label = { Text("Currency") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = line,
                focusedLabelColor = accent,
                unfocusedLabelColor = muted,
                unfocusedTextColor = text,
                disabledTextColor = text,
            ),
            trailingIcon = {
                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = muted)
            },
            enabled = false,
        )
        if (showPicker) {
            ModalBottomSheet(
                onDismissRequest = { onShowPicker(false) },
                containerColor = surface,
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Select Currency", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = text)
                    CURRENCIES.forEach { opt ->
                        val isSel = opt.sym == symbol
                        Row(
                            Modifier.fillMaxWidth().clickable { onSelect(opt.sym, opt.code); onShowPicker(false) }
                                .background(if (isSel) surfaceAlt else Color.Transparent)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(opt.sym, fontWeight = FontWeight.Bold, color = text)
                                Text(opt.name, color = muted, fontSize = 12.sp)
                            }
                            Text(opt.code, color = muted, fontSize = 12.sp)
                        }
                    }
                    // Custom option
                    val isCustom = symbol == "custom"
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect("custom", "Custom"); onShowPicker(false) }
                            .background(if (isCustom) surfaceAlt else Color.Transparent)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Custom", fontWeight = FontWeight.Bold, color = text)
                            Text("Type your own symbol", color = muted, fontSize = 12.sp)
                        }
                        Text("...", color = muted)
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

//  ENTRY SHEET (Add / Edit)
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntrySheet(
    prefs: PreferencesState,
    aesthetic: Aesthetic,
    isDark: Boolean,
    entry: EntryRow?,
    surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, success: Color, danger: Color,
    onDismiss: () -> Unit,
    onSave: (EntryDraft) -> Unit,
    onDeleteEntry: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var kind by rememberSaveable { mutableStateOf(EntryKind.valueOf(entry?.kind?.uppercase() ?: "EXPENSE")) }
    var amount by rememberSaveable { mutableStateOf(entry?.amountMinor?.let { moneyFmt(it, "") } ?: "") }
    var title by rememberSaveable { mutableStateOf(entry?.title ?: "") }
    var cat by rememberSaveable { mutableStateOf(entry?.category ?: "") }
    var notes by rememberSaveable { mutableStateOf(entry?.notes ?: "") }
    var dateMs by rememberSaveable { mutableStateOf(entry?.occurredAt ?: System.currentTimeMillis()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surface,
        modifier = Modifier.navigationBarsPadding().imePadding(),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        if (entry == null) "Add Entry" else "Edit Entry",
                        fontSize = 22.sp, fontWeight = FontWeight.Black, color = text,
                    )
                    Text("Stored locally on this device", color = muted, fontSize = 12.sp)
                }
                if (entry != null && onDeleteEntry != null) {
                    TextButton(onClick = onDeleteEntry) {
                        Icon(Icons.Rounded.Delete, null, tint = danger, modifier = Modifier.size(16.dp))
                        Text(" Delete", color = danger, fontSize = 13.sp)
                    }
                }
            }

            // Kind selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                EntryKind.entries.forEach { k ->
                    val sel = k == kind
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) when (k) {
                                EntryKind.INCOME -> success
                                EntryKind.TRANSFER -> accent
                                else -> danger
                            } else surfaceAlt)
                            .clickable { kind = k }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            k.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (sel) Color.White else muted,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount") },
                leadingIcon = { Text(prefs.symbol, fontWeight = FontWeight.Bold, color = accent) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = line,
                    focusedTextColor = text,
                    unfocusedTextColor = text,
                ),
                shape = RoundedCornerShape(14.dp),
            )

            if (prefs.showTitle) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = line,
                        focusedTextColor = text,
                        unfocusedTextColor = text,
                    ),
                    shape = RoundedCornerShape(14.dp),
                )
            }

            if (prefs.showCategory) {
                OutlinedTextField(
                    value = cat,
                    onValueChange = { cat = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = line,
                        focusedTextColor = text,
                        unfocusedTextColor = text,
                    ),
                    shape = RoundedCornerShape(14.dp),
                )
            }

            if (prefs.showNotes) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = line,
                        focusedTextColor = text,
                        unfocusedTextColor = text,
                    ),
                    shape = RoundedCornerShape(14.dp),
                )
            }

            // Date picker
            OutlinedButton(
                onClick = { openDatePicker(ctx, dateMs) { dateMs = it } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Rounded.CalendarToday, null, tint = muted)
                Spacer(Modifier.width(8.dp))
                Text(fmtDate(dateMs), color = text)
            }

            error?.let {
                Text(it, color = danger, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    val parsed = amount.toDoubleOrNull()
                    if (parsed == null || parsed <= 0) {
                        error = "Enter a valid amount greater than 0"
                        return@Button
                    }
                    onSave(
                        EntryDraft(
                            id = entry?.id,
                            kind = kind,
                            amountMinor = (parsed * 100).toLong(),
                            title = title,
                            category = cat,
                            notes = notes,
                            occurredAt = dateMs,
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
                    .clip(RoundedCornerShape(14.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
            ) {
                Text(if (entry == null) "Save Entry" else "Update Entry", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SHARED UI COMPONENTS
// ─────────────────────────────────────────────────────────────
@Composable
private fun HeroCard(
    prefs: PreferencesState,
    aesthetic: Aesthetic,
    isDark: Boolean,
    balance: Long, income: Long, expense: Long,
    bg: Color, surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, success: Color, successM: Color,
    danger: Color, dangerM: Color, grad: List<Color>,
    onOpenProfile: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.22f),
                        accent.copy(alpha = 0.08f),
                        surface,
                    )
                )
            )
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .clickable { onOpenProfile() }
            .padding(22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassCircle(size = 46.dp, color = accent, isDark = isDark) {
                        Text(
                            prefs.name.take(1).uppercase().ifBlank { "S" },
                            fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White,
                        )
                    }
                    Column {
                        Text(
                            prefs.name.ifBlank { "Solo Ledger" },
                            fontWeight = FontWeight.Bold, fontSize = 15.sp, color = text,
                        )
                        Text("Offline personal finance", color = muted, fontSize = 11.sp)
                    }
                }
                Icon(Icons.Rounded.Edit, null, tint = muted, modifier = Modifier.size(18.dp))
            }
            Text(
                "${prefs.symbol}${"%,.2f".format(kotlin.math.abs(balance) / 100.0)}",
                fontSize = 36.sp, fontWeight = FontWeight.Black, color = text,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCard(surface = surface, line = line, isDark = isDark) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Income", color = muted, fontSize = 11.sp)
                        Text(
                            "${prefs.symbol}${"%,.2f".format(income / 100.0)}",
                            color = success, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        )
                    }
                }
                GlassCard(surface = surface, line = line, isDark = isDark) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Expense", color = muted, fontSize = 11.sp)
                        Text(
                            "${prefs.symbol}${"%,.2f".format(expense / 100.0)}",
                            color = danger, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    color: Color,
    bgColor: Color,
    surface: Color,
    text: Color,
    muted: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = muted, fontSize = 12.sp)
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 15.sp)
        }
    }
}

@Composable
private fun EntryRowCard(
    entry: EntryRow,
    prefs: PreferencesState,
    aesthetic: Aesthetic,
    isDark: Boolean,
    surface: Color, line: Color, text: Color, muted: Color,
    accent: Color, success: Color, danger: Color,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    showRestore: Boolean = false,
) {
    val tint = when (entry.kind) {
        "income" -> success
        "transfer" -> accent
        else -> danger
    }
    val kindIcon = when (entry.kind) {
        "income" -> Icons.Rounded.ArrowUpward
        "transfer" -> Icons.Rounded.SwapVert
        else -> Icons.Rounded.ArrowDownward
    }

    GlassCard(surface = surface, line = line, isDark = isDark) {
        Row(
            Modifier.fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(42.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(kindIcon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                if (prefs.showTitle) {
                    Text(
                        entry.title.ifBlank { "(no title)" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = text,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (prefs.showCategory && entry.category.isNotBlank()) {
                        Text(entry.category, color = muted, fontSize = 11.sp)
                    }
                    if (prefs.showDate) {
                        Text(fmtDate(entry.occurredAt), color = muted, fontSize = 11.sp)
                    }
                }
                if (prefs.showNotes && entry.notes.isNotBlank()) {
                    Text(entry.notes, color = muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (entry.kind == "income") "+" else "-"}${prefs.symbol}${"%,.2f".format(entry.amountMinor / 100.0)}",
                    color = tint, fontWeight = FontWeight.Black, fontSize = 14.sp,
                )
                if (showRestore) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onClick, modifier = Modifier.height(24.dp)) {
                            Icon(Icons.Rounded.Restore, null, tint = success, modifier = Modifier.size(12.dp))
                            Text("Restore", color = success, fontSize = 10.sp)
                        }
                        onDelete?.let { del ->
                            TextButton(onClick = del, modifier = Modifier.height(24.dp)) {
                                Icon(Icons.Rounded.Delete, null, tint = danger, modifier = Modifier.size(12.dp))
                                Text("Delete", color = danger, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    body: String,
    accent: Color,
    surface: Color,
    text: Color,
    muted: Color,
) {
    GlassCard(surface = surface, line = Color.Transparent, isDark = false) {
        Column(
            Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(38.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = text)
            Text(body, color = muted, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun GlassCard(
    surface: Color,
    line: Color,
    isDark: Boolean,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(containerColor = surface.copy(alpha = if (isDark) 0.85f else 0.96f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, line.copy(alpha = if (isDark) 0.3f else 0.5f)),
        shape = RoundedCornerShape(20.dp),
    ) { content() }
}

@Composable
private fun GlassCircle(
    size: Dp,
    color: Color,
    isDark: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier.size(size)
            .clip(CircleShape)
            .background(
                if (isDark) Brush.linearGradient(listOf(color, color.copy(alpha = 0.6f)))
                else Brush.linearGradient(listOf(color, color.copy(alpha = 0.8f)))
            )
            .then(
                if (isDark) Modifier.border(0.dp, Color.Transparent, CircleShape)
                else Modifier.shadow(4.dp, CircleShape, color.copy(alpha = 0.3f), androidx.compose.ui.geometry.Offset.Zero)
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    accent: Color,
    surfaceAlt: Color,
    text: Color,
    muted: Color,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = text, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
        )
    }
}

@Composable
private fun AnimatedBarChart(
    entries: List<EntryRow>,
    accent: Color,
    surfaceAlt: Color,
    line: Color,
) {
    val grouped = entries.groupBy {
        Instant.ofEpochMilli(it.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSortedMap().values.takeLast(10)

    val maxVal = grouped.flatten().maxOfOrNull { it.amountMinor }?.coerceAtLeast(1L) ?: 1L

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            grouped.forEach { dayEntries ->
                val total = dayEntries.sumOf { it.amountMinor }
                val height = (total.toFloat() / maxVal * 100).coerceAtLeast(4f)
                Box(
                    Modifier.weight(1f)
                        .height(height.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(accent, accent.copy(alpha = 0.4f))
                            )
                        ),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            grouped.lastOrNull()?.firstOrNull()?.let {
                Text(
                    fmtDate(it.occurredAt),
                    color = muted, fontSize = 10.sp,
                )
            }
            grouped.firstOrNull()?.firstOrNull()?.let {
                Text(
                    fmtDate(it.occurredAt),
                    color = muted, fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun ProgressRow(
    label: String,
    value: Long,
    max: Long,
    accent: Color,
    surfaceAlt: Color,
    text: Color,
    muted: Color,
) {
    val ratio = (value.toFloat() / max.toFloat()).coerceAtMost(1f)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, color = text, fontSize = 13.sp)
            Text(
                "%,.2f".format(value / 100.0),
                color = accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            Modifier.fillMaxWidth().height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(surfaceAlt),
        ) {
            Box(
                Modifier.fillMaxWidth(ratio)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent),
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────
//  SUPPORT SECTION
// ─────────────────────────────────────────────────────────────
private val SUPPORT_QUOTES = listOf(
    "Every contribution fuels the next breakthrough.",
    "Great software deserves great supporters like you.",
    "Your generosity keeps this project alive.",
    "Together, we build tools that matter.",
    "Thank you for believing in open, offline-first software.",
)

private val SUPPORT_LINKS = mapOf(
    "Buy Me a Coffee" to "https://buymeacoffee.com/mkr_infinity",
    "GitHub Sponsors" to "https://github.com/sponsors/mkr_infinity",
    "Ko-fi" to "https://ko-fi.com/mkr_infinity",
    "Patreon" to "https://patreon.com/mkr_infinity",
)

@Composable
private fun SupportSection(
    aesthetic: Aesthetic,
    isDark: Boolean,
    bg: Color, surface: Color, surfaceGlass: Color, surfaceAlt: Color,
    line: Color, text: Color, muted: Color,
    accent: Color, accentM: Color, success: Color, danger: Color,
) {
    var selectedQuote by remember { mutableStateOf(SUPPORT_QUOTES.random()) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GlassCircle(size = 40.dp, color = accent, isDark = isDark) {
                Icon(Icons.Rounded.Favorite, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column {
                Text("Support the Project", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = text)
                Text("If Solo Ledger helps you, consider a small gesture of appreciation.", color = muted, fontSize = 11.sp)
            }
        }

        // Quote card with refresh
        GlassCard(surface = surface, line = line, isDark = isDark) {
            Box(
                Modifier.fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(accentM.copy(alpha = 0.12f), accent.copy(alpha = 0.06f)),
                        ),
                    )
                    .clickable { selectedQuote = SUPPORT_QUOTES.random() }
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            Icons.Rounded.FormatQuote,
                            null,
                            tint = accent.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp),
                        )
                        Icon(
                            Icons.Rounded.Refresh,
                            null,
                            tint = muted.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        ""$selectedQuote"",
                        color = text,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        style = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                }
            }
        }

        // Link buttons
        GlassCard(surface = surface, line = line, isDark = isDark) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Ways to support", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = muted)

                SUPPORT_LINKS.forEach { (label, url) ->
                    val isCoffee = label.contains("Coffee")
                    val bgColor = if (isCoffee) Color(0xFFFFD700) else if (isDark) Color.White else Color(0xFF24292E)
                    val txtColor = if (isCoffee) Color(0xFF1A1A1A) else if (isCoffee) Color.Black else Color.White

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    LocalContext.current.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(vertical = 13.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (isCoffee) {
                                // Coffee cup icon
                                Icon(
                                    Icons.Rounded.LocalCafe,
                                    null,
                                    tint = txtColor,
                                    modifier = Modifier.size(18.dp),
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.Code,
                                    null,
                                    tint = txtColor,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Text(
                                label,
                                color = txtColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  DATE PICKER HELPER
// ─────────────────────────────────────────────────────────────
private fun openDatePicker(ctx: Context, current: Long?, onPicked: (Long) -> Unit) {
    val base = current ?: System.currentTimeMillis()
    val loc = Instant.ofEpochMilli(base).atZone(ZoneId.systemDefault()).toLocalDate()
    DatePickerDialog(
        ctx,
        { _, y, m, d ->
            onPicked(LocalDate.of(y, m + 1, d).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        },
        loc.year, loc.monthValue - 1, loc.dayOfMonth,
    ).show()
}

private fun fmtDate(epoch: Long): String =
    Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)

// ─────────────────────────────────────────────────────────────
//  CURRENCY FORMATTER
// ─────────────────────────────────────────────────────────────
private fun moneyFmt(minor: Long, symbol: String): String =
    "%,.2f".format(kotlin.math.abs(minor) / 100.0)