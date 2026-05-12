package com.sololedger

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololedger.data.LedgerEntry
import com.sololedger.data.PrefStore
import com.sololedger.data.Repo
import com.sololedger.ui.theme.AESTHETICS
import java.text.SimpleDateFormat
import java.util.*

private val CURRENCIES = listOf(
    "₹" to "Indian Rupee", "$" to "US Dollar", "€" to "Euro", "£" to "British Pound",
    "¥" to "Japanese Yen", "₿" to "Bitcoin", "kr" to "Swedish Krona", "₩" to "Korean Won",
    "₽" to "Russian Ruble", "R$" to "Brazilian Real", "A$" to "Australian Dollar",
    "C$" to "Canadian Dollar", "CHF" to "Swiss Franc"
)

private val CATEGORIES = listOf(
    "Food", "Transport", "Shopping", "Entertainment", "Bills", "Health",
    "Travel", "Education", "Salary", "Freelance", "Investment", "Gift", "Other"
)

private val SUPPORT_LINKS = listOf(
    "Buy Me a Coffee" to "https://buymeacoffee.com/mkr-infinity",
    "Patreon" to "https://patreon.com/mkr_infinity"
)

private val SUPPORT_QUOTES = listOf(
    "Your support keeps this app free forever.",
    "Made with care. Every contribution matters.",
    "Thank you for believing in this project.",
    "Fuel the mission — one coffee at a time."
)

private const val STAGE_HOME = 0
private const val STAGE_SEARCH = 1
private const val STAGE_EDIT = 2
private const val STAGE_SETTINGS = 3
private const val STAGE_PROFILE = 4
private const val STAGE_BIN = 5

@Composable
fun SoloApp() {
    val ctx = LocalContext.current
    val prefStore = remember { PrefStore(ctx) }
    LaunchedEffect(Unit) { Repo.init(ctx) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var stage by remember { mutableIntStateOf(STAGE_HOME) }
    var showEntrySheet by remember { mutableStateOf(false) }
    var editingEntryId by remember { mutableLongStateOf(-1L) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableIntStateOf(-1) }
    var entriesSnapshot by remember { mutableStateOf(emptyList<LedgerEntry>()) }
    var deletedSnapshot by remember { mutableStateOf(emptyList<LedgerEntry>()) }
    var aesthetic by remember {
        mutableStateOf(AESTHETICS.getOrElse(prefStore.aestheticIdx.coerceIn(0, AESTHETICS.lastIndex)) { AESTHETICS[0] })
    }
    var currencySymbol by remember { mutableStateOf(prefStore.currencySymbol.takeIf { it.isNotBlank() } ?: "₹") }

    LaunchedEffect(stage) {
        when (stage) {
            STAGE_HOME, STAGE_SEARCH -> {
                entriesSnapshot = Repo.entries()
                deletedSnapshot = Repo.deletedEntries()
            }
            STAGE_BIN -> { deletedSnapshot = Repo.deletedEntries() }
            else -> {}
        }
    }

    val accent = aesthetic.accent
    val bg = aesthetic.surface
    val card = aesthetic.card
    val text = aesthetic.foreground
    val muted = aesthetic.muted
    val surfaceAlt = aesthetic.surfaceAlt
    val isDark = aesthetic.isDark

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isDark) Color(0xFF0D0D1A) else Color(0xFFF5F5F5),
                            bg
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            when (stage) {
                STAGE_HOME -> HomeScreen(
                    entries = entriesSnapshot,
                    searchQuery = searchQuery,
                    filterType = filterType,
                    sym = currencySymbol,
                    aesthetic = aesthetic,
                    onAdd = { showEntrySheet = true },
                    onSearch = { stage = STAGE_SEARCH },
                    onSettings = { stage = STAGE_SETTINGS },
                    onEditEntry = { id -> editingEntryId = id; stage = STAGE_EDIT },
                    onDeleteEntry = { id ->
                        Repo.deleteEntry(id)
                        entriesSnapshot = entriesSnapshot.filter { it.id != id }
                    },
                    onBin = { stage = STAGE_BIN },
                    onProfile = { stage = STAGE_PROFILE },
                    onFilterChange = { filterType = it }
                )
                STAGE_SEARCH -> SearchScreen(
                    entries = entriesSnapshot,
                    searchQuery = searchQuery,
                    filterType = filterType,
                    sym = currencySymbol,
                    aesthetic = aesthetic,
                    onQueryChange = { searchQuery = it },
                    onEditEntry = { id -> editingEntryId = id; stage = STAGE_EDIT },
                    onDeleteEntry = { id ->
                        Repo.deleteEntry(id)
                        entriesSnapshot = entriesSnapshot.filter { it.id != id }
                    },
                    onFilterChange = { filterType = it },
                    onBack = { stage = STAGE_HOME }
                )
                STAGE_EDIT -> EntryEditorScreen(
                    entryId = editingEntryId,
                    sym = currencySymbol,
                    aesthetic = aesthetic,
                    onDismiss = { editingEntryId = -1L; stage = STAGE_HOME },
                    onSaved = { editingEntryId = -1L; stage = STAGE_HOME },
                    onSymChange = { v -> currencySymbol = v; prefStore.currencySymbol = v },
                    prefStore = prefStore
                )
                STAGE_SETTINGS -> SettingsScreenContent(
                    aesthetic = aesthetic,
                    onAestheticChange = { a -> aesthetic = a; prefStore.aestheticIdx = AESTHETICS.indexOf(a) },
                    onCurrencyChange = { v -> currencySymbol = v; prefStore.currencySymbol = v },
                    onBack = { stage = STAGE_HOME },
                    onProfile = { stage = STAGE_PROFILE },
                    prefStore = prefStore,
                    isDark = isDark,
                    text = text,
                    accent = accent,
                    muted = muted,
                    card = card,
                    surfaceAlt = surfaceAlt,
                    bg = bg
                )
                STAGE_PROFILE -> ProfileScreenContent(
                    aesthetic = aesthetic,
                    onAestheticChange = { aesthetic = it },
                    onBack = { stage = STAGE_HOME },
                    onSettings = { stage = STAGE_SETTINGS },
                    isDark = isDark,
                    text = text,
                    accent = accent,
                    muted = muted,
                    card = card,
                    surfaceAlt = surfaceAlt,
                    bg = bg
                )
                STAGE_BIN -> BinScreenContent(
                    entries = deletedSnapshot,
                    sym = currencySymbol,
                    aesthetic = aesthetic,
                    onRestore = { id ->
                        Repo.restoreEntry(id)
                        deletedSnapshot = deletedSnapshot.filter { it.id != id }
                        entriesSnapshot = entriesSnapshot.map { if (it.id == id) it.copy(deleted = false) else it }
                    },
                    onDeleteForever = { id ->
                        Repo.deleteForever(id)
                        deletedSnapshot = deletedSnapshot.filter { it.id != id }
                    },
                    onBack = { stage = STAGE_HOME }
                )
            }
        }

        if (stage == STAGE_HOME || stage == STAGE_SEARCH) {
            AnimatedVisibility(
                visible = showEntrySheet,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    EntryEditorScreen(
                        entryId = -1L,
                        sym = currencySymbol,
                        aesthetic = aesthetic,
                        onDismiss = { showEntrySheet = false },
                        onSaved = {
                            showEntrySheet = false
                            entriesSnapshot = Repo.entries()
                        },
                        onSymChange = { v -> currencySymbol = v; prefStore.currencySymbol = v },
                        prefStore = prefStore
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = stage == STAGE_HOME || stage == STAGE_SEARCH,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            BottomNavBarContent(
                selected = selectedTab,
                accent = accent,
                text = text,
                muted = muted,
                bg = bg,
                onHome = { selectedTab = 0; stage = STAGE_HOME },
                onSearch = { selectedTab = 1; stage = STAGE_SEARCH },
                onSettings = { selectedTab = 2; stage = STAGE_SETTINGS },
                onProfile = { selectedTab = 3; stage = STAGE_PROFILE }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    entries: List<LedgerEntry>,
    searchQuery: String,
    filterType: Int,
    sym: String,
    aesthetic: com.sololedger.ui.theme.Aesthetic,
    onAdd: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onEditEntry: (Long) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onBin: () -> Unit,
    onProfile: () -> Unit,
    onFilterChange: (Int) -> Unit
) {
    val accent = aesthetic.accent
    val bg = aesthetic.surface
    val text = aesthetic.foreground
    val muted = aesthetic.muted
    val card = aesthetic.card

    val totalIncome = entries.filter { it.kind == 0 }.sumOf { it.amountMinor }
    val totalExpense = entries.filter { it.kind == 1 }.sumOf { it.amountMinor }
    val balance = totalIncome - totalExpense

    Column(modifier = Modifier.fillMaxSize().background(bg).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Solo Ledger", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = text)
                Text(formatDate(System.currentTimeMillis()), fontSize = 12.sp, color = muted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBtn("search", text, card, onSearch)
                IconBtn("bin", text, card, onBin)
                IconBtn("settings", text, card, onSettings)
                IconBtn("profile", text, card, onProfile)
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            GlassCard(bg, if (aesthetic.isDark) 0.5f else 0.08f, 16) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Total Balance", fontSize = 13.sp, color = muted)
                    Text("$sym ${formatAmount(balance)}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = text)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatPill("Income", "$sym ${formatAmount(totalIncome)}", Color(0xFF4CAF50), bg)
                        StatPill("Expense", "$sym ${formatAmount(totalExpense)}", Color(0xFFE91E63), bg)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip("All", if (filterType == -1) accent else muted, card, text) { onFilterChange(-1) }
            FilterChip("Income", if (filterType == 0) accent else muted, card, text) { onFilterChange(0) }
            FilterChip("Expense", if (filterType == 1) accent else muted, card, text) { onFilterChange(1) }
        }

        val filtered = entries.filter { e ->
            (filterType == -1 || e.kind == filterType) &&
            (searchQuery.isEmpty() || e.title.contains(searchQuery, ignoreCase = true) || e.category.contains(searchQuery, ignoreCase = true))
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No entries yet.\nTap + to add your first!", color = muted, textAlign = TextAlign.Center, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filtered, key = { it.id }) { entry ->
                    val isIncome = entry.kind == 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16))
                            .background(card)
                            .shadow(2.dp, RoundedCornerShape(16))
                            .clickable { onEditEntry(entry.id) }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isIncome) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFE91E63).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (isIncome) "^" else "v",
                                        fontSize = 18.sp,
                                        color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(entry.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(entry.category, fontSize = 12.sp, color = muted)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${if (isIncome) "+" else "-"} $sym ${formatAmount(entry.amountMinor)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63)
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.End).padding(16.dp).padding(bottom = 72.dp),
            containerColor = accent,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchScreen(
    entries: List<LedgerEntry>,
    searchQuery: String,
    filterType: Int,
    sym: String,
    aesthetic: com.sololedger.ui.theme.Aesthetic,
    onQueryChange: (String) -> Unit,
    onEditEntry: (Long) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onFilterChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    val accent = aesthetic.accent
    val bg = aesthetic.surface
    val text = aesthetic.foreground
    val muted = aesthetic.muted
    val card = aesthetic.card

    Column(modifier = Modifier.fillMaxSize().background(bg).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBtn("back", text, card, onBack)
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search entries...", color = muted) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = muted)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip("All", if (filterType == -1) accent else muted, card, text) { onFilterChange(-1) }
            FilterChip("Income", if (filterType == 0) accent else muted, card, text) { onFilterChange(0) }
            FilterChip("Expense", if (filterType == 1) accent else muted, card, text) { onFilterChange(1) }
        }
        val filtered = entries.filter { e ->
            (filterType == -1 || e.kind == filterType) &&
            (searchQuery.isEmpty() || e.title.contains(searchQuery, ignoreCase = true) || e.category.contains(searchQuery, ignoreCase = true))
        }
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results found.", color = muted, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filtered, key = { it.id }) { entry ->
                    val isIncome = entry.kind == 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16))
                            .background(card)
                            .shadow(2.dp, RoundedCornerShape(16))
                            .clickable { onEditEntry(entry.id) }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isIncome) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFE91E63).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (isIncome) "^" else "v", fontSize = 18.sp, color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(entry.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(entry.category, fontSize = 12.sp, color = muted)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${if (isIncome) "+" else "-"} $sym ${formatAmount(entry.amountMinor)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryEditorScreen(
    entryId: Long,
    sym: String,
    aesthetic: com.sololedger.ui.theme.Aesthetic,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onSymChange: (String) -> Unit,
    prefStore: PrefStore
) {
    val accent = aesthetic.accent
    val bg = aesthetic.surface
    val text = aesthetic.foreground
    val muted = aesthetic.muted
    val surfaceAlt = aesthetic.surfaceAlt
    val ctx = LocalContext.current

    var kind by remember { mutableIntStateOf(1) }
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CATEGORIES[0]) }
    var note by remember { mutableStateOf("") }
    var dateMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showCatPicker by remember { mutableStateOf(false) }
    var showSymPicker by remember { mutableStateOf(false) }

    val isEdit = entryId > 0

    LaunchedEffect(entryId) {
        if (isEdit) {
            Repo.entries().find { it.id == entryId }?.let { e ->
                kind = e.kind
                title = e.title
                amountStr = if (e.amountMinor != 0) (e.amountMinor / 100.0).toString() else ""
                category = e.category
                note = e.note
                dateMs = e.dateMs
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable(onClick = onDismiss)) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24, topEnd = 24))
                .background(bg)
                .clickable(enabled = true, onClick = {})
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Text(if (isEdit) "Edit Entry" else "Add Entry", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = text)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12))
                        .background(if (kind == 0) accent else surfaceAlt)
                        .clickable { kind = 0 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Income", fontSize = 14.sp, color = if (kind == 0) Color.White else muted, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12))
                        .background(if (kind == 1) accent else surfaceAlt)
                        .clickable { kind = 1 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Expense", fontSize = 14.sp, color = if (kind == 1) Color.White else muted, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = muted, focusedLabelColor = accent),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = muted, focusedLabelColor = accent),
                    singleLine = true
                )
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .clip(RoundedCornerShape(4))
                        .background(surfaceAlt)
                        .clickable { showSymPicker = true }
                        .padding(16.dp)
                ) {
                    Text(sym, fontSize = 16.sp, color = text)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4))
                    .background(surfaceAlt)
                    .clickable { showCatPicker = true }
                    .padding(16.dp)
            ) {
                Text(category, fontSize = 16.sp, color = text)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = muted, focusedLabelColor = accent),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4))
                    .background(surfaceAlt)
                    .clickable {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            ctx,
                            { _, y, m, d -> cal.set(y, m, d); dateMs = cal.timeInMillis },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .padding(16.dp)
            ) {
                Text(formatDate(dateMs), fontSize = 16.sp, color = text)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val amountMinor = (amountStr.toDoubleOrNull() ?: 0.0).times(100).toInt()
                    if (title.isNotBlank() && amountMinor > 0) {
                        if (isEdit) Repo.updateEntry(entryId, title, amountMinor, kind, category, note, dateMs)
                        else Repo.addEntry(title, amountMinor, kind, category, note, dateMs)
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(12)
            ) {
                Text(if (isEdit) "Update Entry" else "Save Entry", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }

    if (showCatPicker) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable { showCatPicker = false }) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24, topEnd = 24))
                    .background(bg)
                    .clickable(enabled = true, onClick = {})
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text("Select Category", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = text)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(CATEGORIES.size) { idx ->
                        val cat = CATEGORIES[idx]
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { category = cat; showCatPicker = false }.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat, fontSize = 16.sp, color = text)
                            if (cat == category) Text("Selected", fontSize = 14.sp, color = accent)
                        }
                        if (idx < CATEGORIES.lastIndex) HorizontalDivider(color = muted.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }

    if (showSymPicker) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable { showSymPicker = false }) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24, topEnd = 24))
                    .background(bg)
                    .clickable(enabled = true, onClick = {})
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text("Select Currency", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = text)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(CURRENCIES.size) { idx ->
                        val (s, name) = CURRENCIES[idx]
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onSymChange(s); showSymPicker = false }.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(s, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = text)
                                Text(name, fontSize = 12.sp, color = muted)
                            }
                            if (s == sym) Text("Selected", fontSize = 14.sp, color = accent)
                        }
                        if (idx < CURRENCIES.lastIndex) HorizontalDivider(color = muted.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreenContent(
    aesthetic: com.sololedger.ui.theme.Aesthetic,
    onAestheticChange: (com.sololedger.ui.theme.Aesthetic) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    prefStore: PrefStore,
    isDark: Boolean,
    text: Color,
    accent: Color,
    muted: Color,
    card: Color,
    surfaceAlt: Color,
    bg: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBtn("back", text, card, onBack)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = text)
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            GlassCard(bg, if (isDark) 0.3f else 0.05f, 16) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Appearance", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = text)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(AESTHETICS.size) { idx ->
                            val a = AESTHETICS[idx]
                            val isSelected = idx == AESTHETICS.indexOf(aesthetic)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10))
                                    .then(if (isSelected) Modifier.background(a.accent.copy(alpha = 0.15f), RoundedCornerShape(10)).padding(8.dp) else Modifier)
                                    .clickable { onAestheticChange(a) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(a.accent))
                                    Column {
                                        Text("Theme ${idx + 1}", fontSize = 14.sp, color = text)
                                        Text(if (a.isDark) "Dark" else "Light", fontSize = 12.sp, color = muted)
                                    }
                                }
                                if (isSelected) Text("Active", fontSize = 13.sp, color = a.accent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard(bg, if (isDark) 0.3f else 0.05f, 16) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Display", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = text)
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleRow("Show Title", prefStore.showTitle, accent, text, muted, card) { prefStore.showTitle = it }
                    ToggleRow("Show Category", prefStore.showCategory, accent, text, muted, card) { prefStore.showCategory = it }
                    ToggleRow("Show Notes", prefStore.showNotes, accent, text, muted, card) { prefStore.showNotes = it }
                    ToggleRow("Show Date", prefStore.showDate, accent, text, muted, card) { prefStore.showDate = it }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard(bg, if (isDark) 0.3f else 0.05f, 16) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onProfile() }.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Profile", fontSize = 16.sp, color = text)
                        Text("->", color = muted, fontSize = 18.sp)
                    }
                    HorizontalDivider(color = muted.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onCurrencyChange(prefStore.currencySymbol) }.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Currency", fontSize = 16.sp, color = text)
                        Text("->", color = muted, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, accent: Color, text: Color, muted: Color, card: Color, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, color = text)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accent,
                checkedTrackColor = accent.copy(alpha = 0.4f),
                uncheckedThumbColor = muted,
                uncheckedTrackColor = muted.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun ProfileScreenContent(
    aesthetic: com.sololedger.ui.theme.Aesthetic,
    onAestheticChange: (com.sololedger.ui.theme.Aesthetic) -> Unit,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    isDark: Boolean,
    text: Color,
    accent: Color,
    muted: Color,
    card: Color,
    surfaceAlt: Color,
    bg: Color
) {
    val ctx = LocalContext.current
    val quote = SUPPORT_QUOTES.random()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBtn("back", text, card, onBack)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = text)
        }
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            GlassCard(bg, if (isDark) 0.5f else 0.08f, 16) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(accent)
                            .clickable { onSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Solo Ledger", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = text)
                    Text("Personal Finance Tracker", fontSize = 14.sp, color = muted)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            GlassCard(bg, if (isDark) 0.3f else 0.05f, 16) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Choose Theme", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = text)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(AESTHETICS.size) { idx ->
                            val a = AESTHETICS[idx]
                            val isSelected = idx == AESTHETICS.indexOf(aesthetic)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10))
                                    .then(if (isSelected) Modifier.background(a.accent.copy(alpha = 0.15f), RoundedCornerShape(10)).padding(8.dp) else Modifier)
                                    .clickable { onAestheticChange(a) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(a.accent))
                                    Column {
                                        Text("Theme ${idx + 1}", fontSize = 14.sp, color = text)
                                        Text(if (a.isDark) "Dark" else "Light", fontSize = 12.sp, color = muted)
                                    }
                                }
                                if (isSelected) Text("Active", fontSize = 13.sp, color = a.accent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard(bg, if (isDark) 0.3f else 0.05f, 16) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Support", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = text)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(quote, fontSize = 12.sp, color = muted, fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.height(12.dp))
                    SUPPORT_LINKS.forEach { (label, url) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 15.sp, color = text)
                            Text("->", color = muted, fontSize = 18.sp)
                        }
                        HorizontalDivider(color = muted.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BinScreenContent(
    entries: List<LedgerEntry>,
    sym: String,
    aesthetic: com.sololedger.ui.theme.Aesthetic,
    onRestore: (Long) -> Unit,
    onDeleteForever: (Long) -> Unit,
    onBack: () -> Unit
) {
    val text = aesthetic.foreground
    val muted = aesthetic.muted
    val card = aesthetic.card
    val surfaceAlt = aesthetic.surfaceAlt
    val accent = aesthetic.accent

    Column(modifier = Modifier.fillMaxSize().background(aesthetic.surface).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBtn("back", text, card, onBack)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Bin", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = text)
        }
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bin is empty", color = muted, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14))
                            .background(card)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatDate(entry.dateMs), fontSize = 12.sp, color = muted)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconBtn("restore", accent, surfaceAlt) { onRestore(entry.id) }
                            IconBtn("delete", Color(0xFFE91E63), surfaceAlt) { onDeleteForever(entry.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavBarContent(
    selected: Int,
    accent: Color,
    text: Color,
    muted: Color,
    bg: Color,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onHome).padding(8.dp)) {
                Text("Home", fontSize = 12.sp, color = if (selected == 0) accent else muted, fontWeight = if (selected == 0) FontWeight.Bold else FontWeight.Normal)
                Box(modifier = Modifier.height(3.dp).width(if (selected == 0) 20.dp else 0.dp).background(if (selected == 0) accent else Color.Transparent, RoundedCornerShape(2)))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onSearch).padding(8.dp)) {
                Text("Search", fontSize = 12.sp, color = if (selected == 1) accent else muted, fontWeight = if (selected == 1) FontWeight.Bold else FontWeight.Normal)
                Box(modifier = Modifier.height(3.dp).width(if (selected == 1) 20.dp else 0.dp).background(if (selected == 1) accent else Color.Transparent, RoundedCornerShape(2)))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onSettings).padding(8.dp)) {
                Text("Settings", fontSize = 12.sp, color = if (selected == 2) accent else muted, fontWeight = if (selected == 2) FontWeight.Bold else FontWeight.Normal)
                Box(modifier = Modifier.height(3.dp).width(if (selected == 2) 20.dp else 0.dp).background(if (selected == 2) accent else Color.Transparent, RoundedCornerShape(2)))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onProfile).padding(8.dp)) {
                Text("Profile", fontSize = 12.sp, color = if (selected == 3) accent else muted, fontWeight = if (selected == 3) FontWeight.Bold else FontWeight.Normal)
                Box(modifier = Modifier.height(3.dp).width(if (selected == 3) 20.dp else 0.dp).background(if (selected == 3) accent else Color.Transparent, RoundedCornerShape(2)))
            }
        }
    }
}

@Composable
private fun GlassCard(bg: Color, alpha: Float, rad: Int, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(rad))
            .background(bg.copy(alpha = alpha))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun IconBtn(icon: String, color: Color, bg: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            when (icon) {
                "back" -> "<"
                "search" -> "?"
                "bin" -> "#"
                "settings" -> "*"
                "profile" -> "@"
                "restore" -> "R"
                "delete" -> "X"
                else -> icon
            },
            fontSize = 16.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FilterChip(label: String, color: Color, bg: Color, text: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color, bg: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 12.sp, color = color)
        Text(value, fontSize = 15.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

private fun formatAmount(minor: Int): String {
    val d = minor / 100.0
    return if (d == d.toLong().toDouble()) d.toLong().toString() else String.format("%.2f", d)
}

private fun formatDate(ms: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(ms))
