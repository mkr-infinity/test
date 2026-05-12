package com.sololedger

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.text.toInputStream
import androidx.room.Room
import com.sololedger.data.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private val dFmt = DateTimeFormatter.ofPattern("dd MMM yyyy")

enum class EntryKind { INCOME, EXPENSE }

enum class NavTab { Home, History, Reports, Settings, Profile }

private data class NavState(
    val tab: NavTab = NavTab.Home,
    val entryForEdit: LedgerEntry? = null,
    val showEntrySheet: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteTarget: LedgerEntry? = null
)

@Composable fun SoloApp() {
    val ctx = LocalContext.current
    val prefs = remember { PrefStore(ctx) }
    val aestheticIdx by prefs.aestheticIdx.collectAsState(initial = 0)
    val darkTheme = prefs.darkTheme.collectAsState(initial = false).value

    val aes = when {
        darkTheme -> DarkPalette
        else -> AESTHETICS.getOrNull(aestheticIdx) ?: AESTHETICS[0]
    }

    LaunchedEffect(Unit) { prefs.seedDefaultCategories() }
    val repo = remember { SoloRepository(Room.databaseBuilder(ctx, AppDatabase::class.java, "solo_ledger").build()) }

    val allEntries by repo.getAll().collectAsState(initial = emptyList())
    val categories by repo.getAllCategories().collectAsState(initial = emptyList())

    var nav by remember { mutableStateOf(NavState()) }
    val entrySheetDraft = remember { mutableStateOf(EntryDraft()) }
    var filterType by remember { mutableStateOf<EntryKind?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableIntStateOf(0) }

    Surface(modifier = Modifier.fillMaxSize(), color = aes.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (nav.tab) {
                    NavTab.Home -> HomeScreen(
                        entries = allEntries, categories = categories, filterType = filterType,
                        searchQuery = searchQuery, selectedPeriod = selectedPeriod,
                        aes = aes, prefs = prefs, repo = repo,
                        onFilter = { filterType = it },
                        onSearch = { searchQuery = it },
                        onPeriod = { selectedPeriod = it },
                        onEditEntry = { e ->
                            entrySheetDraft.value = EntryDraft(
                                title = e.title, notes = e.notes, amountStr = (e.amountMinor / 100.0).toString(),
                                kind = e.kind, category = e.category, color = e.color, icon = e.icon, dateMs = e.dateMs, editingId = e.id
                            )
                            nav = nav.copy(showEntrySheet = true, entryForEdit = e)
                        },
                        onDeleteEntry = { nav = nav.copy(showDeleteDialog = true, deleteTarget = it) }
                    )
                    NavTab.History -> HistoryScreen(
                        entries = allEntries, categories = categories, aes = aes, prefs = prefs,
                        onEdit = { e ->
                            entrySheetDraft.value = EntryDraft(
                                title = e.title, notes = e.notes, amountStr = (e.amountMinor / 100.0).toString(),
                                kind = e.kind, category = e.category, color = e.color, icon = e.icon, dateMs = e.dateMs, editingId = e.id
                            )
                            nav = nav.copy(showEntrySheet = true, entryForEdit = e)
                        },
                        onDelete = { nav = nav.copy(showDeleteDialog = true, deleteTarget = it) }
                    )
                    NavTab.Reports -> ReportsScreen(
                        entries = allEntries, categories = categories, aes = aes, prefs = prefs,
                        selectedPeriod = selectedPeriod, onPeriodChange = { selectedPeriod = it }
                    )
                    NavTab.Settings -> SettingsScreen(
                        aes = aes, prefs = prefs, repo = repo, categories = categories,
                        onOpenProfile = { nav = nav.copy(tab = NavTab.Profile) }
                    )
                    NavTab.Profile -> ProfileEditScreen(
                        aes = aes, prefs = prefs,
                        onBack = { nav = nav.copy(tab = NavTab.Settings) }
                    )
                }
            }
            AnimatedBottomNav(
                selected = nav.tab, accent = aes.accent, surface = aes.surface,
                text = aes.text, muted = aes.text.copy(alpha = 0.5f),
                onSelect = { nav = nav.copy(tab = it, showEntrySheet = false) }
            )
        }

        if (nav.showEntrySheet) {
            EntrySheet(
                draft = entrySheetDraft.value,
                categories = categories,
                aes = aes,
                onChange = { entrySheetDraft.value = it },
                onSave = { draft ->
                    val minor = (draft.amountStr.toDoubleOrNull() ?: 0.0).times(100).toInt()
                    val e = LedgerEntry(
                        id = draft.editingId ?: UUID.randomUUID().toString(),
                        kind = draft.kind, title = draft.title.ifBlank { "Entry" },
                        notes = draft.notes, amountMinor = minor,
                        category = draft.category.ifBlank { "General" },
                        color = draft.color, icon = draft.icon.ifBlank { "payments" },
                        dateMs = draft.dateMs, createdAt = System.currentTimeMillis()
                    )
                    kotlinx.coroutines.run { if (draft.editingId != null) repo.update(e) else repo.insert(e) }
                    entrySheetDraft.value = EntryDraft()
                    nav = nav.copy(showEntrySheet = false, entryForEdit = null)
                },
                onDismiss = {
                    entrySheetDraft.value = EntryDraft()
                    nav = nav.copy(showEntrySheet = false, entryForEdit = null)
                }
            )
        }

        if (nav.showDeleteDialog && nav.deleteTarget != null) {
            DeleteDialog(
                entry = nav.deleteTarget!!, aes = aes,
                onConfirm = {
                    kotlinx.coroutines.run { repo.delete(nav.deleteTarget!!.id) }
                    nav = nav.copy(showDeleteDialog = false, deleteTarget = null)
                },
                onDismiss = { nav = nav.copy(showDeleteDialog = false, deleteTarget = null) }
            )
        }
    }
}

private data class EntryDraft(
    val title: String = "", val notes: String = "", val amountStr: String = "",
    val kind: EntryKind = EntryKind.EXPENSE, val category: String = "General",
    val color: Long = 0xFFE8537AL, val icon: String = "payments",
    val dateMs: Long = System.currentTimeMillis(), val editingId: String? = null
)

@Composable private fun HomeScreen(
    entries: List<LedgerEntry>, categories: List<Category>, filterType: EntryKind?, searchQuery: String,
    selectedPeriod: Int, aes: Aesthetic, prefs: PrefStore, repo: SoloRepository,
    onFilter: (EntryKind?) -> Unit, onSearch: (String) -> Unit, onPeriod: (Int) -> Unit,
    onEditEntry: (LedgerEntry) -> Unit, onDeleteEntry: (LedgerEntry) -> Unit
) {
    val showTitle by prefs.showTitle.collectAsState(initial = true)
    val showCategory by prefs.showCategory.collectAsState(initial = true)
    val showNotes by prefs.showNotes.collectAsState(initial = true)
    val showDate by prefs.showDate.collectAsState(initial = true)
    val sym by prefs.currencySymbol.collectAsState(initial = "INR")

    val filtered = remember(entries, filterType, searchQuery, selectedPeriod) {
        entries.filter { e ->
            val okType = filterType == null || e.kind == filterType
            val okSearch = searchQuery.isBlank() || e.title.contains(searchQuery, true) || e.notes.contains(searchQuery, true)
            val okPeriod = when (selectedPeriod) {
                0 -> true
                1 -> e.dateMs > System.currentTimeMillis() - 7 * 86400000L
                2 -> e.dateMs > System.currentTimeMillis() - 90 * 86400000L
                else -> true
            }
            okType && okSearch && okPeriod
        }
    }

    val income = entries.filter { it.kind == EntryKind.INCOME }.sumOf { it.amountMinor }
    val expense = entries.filter { it.kind == EntryKind.EXPENSE }.sumOf { it.amountMinor }
    val net = income - expense

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopHeader(sym = sym, income = income, expense = expense, net = net, aes = aes, prefs = prefs)
        FilterBar(selected = filterType, accent = aes.accent, onSelect = onFilter)
        PeriodSelector(selected = selectedPeriod, accent = aes.accent, muted = aes.text.copy(alpha = 0.4f), onPeriod = onPeriod)
        SearchBar(query = searchQuery, accent = aes.accent, placeholder = aes.text.copy(alpha = 0.4f), onQuery = onSearch)

        val grouped = filtered.groupBy { LocalDate.ofEpochDay(it.dateMs / 86400000L) }
        grouped.entries.sortedByDescending { it.key }.forEach { (day, items) ->
            DateSection(
                date = day, entries = items, categories = categories, aes = aes,
                showTitle = showTitle, showCategory = showCategory, showNotes = showNotes, showDate = showDate,
                onEdit = onEditEntry, onDelete = onDeleteEntry, sym = sym
            )
        }
        if (filtered.isEmpty()) EmptyState(text = aes.text.copy(alpha = 0.4f), label = "No entries yet")
        BottomPadding()
    }
}

@Composable private fun TopHeader(sym: String, income: Int, expense: Int, net: Int, aes: Aesthetic, prefs: PrefStore) {
    val p by prefs.name.collectAsState(initial = "")
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Column {
            Text("Solo Ledger", style = TextStyle(color = aes.text, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            Text(p.ifBlank { "Welcome back" }, style = TextStyle(color = aes.text.copy(alpha = 0.5f), fontSize = 13.sp))
        }
        val now = LocalDate.now()
        Text(now.format(dFmt), style = TextStyle(color = aes.text.copy(alpha = 0.4f), fontSize = 12.sp), modifier = Modifier.align(Alignment.TopEnd))
    }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(label = "Income", amount = income, sym = sym, accent = aes.accent, bg = aes.surfaceVariant, fg = aes.text, isPositive = true)
        StatCard(label = "Expense", amount = expense, sym = sym, accent = Color(0xFFE8537A), bg = aes.surfaceVariant, fg = aes.text, isPositive = false)
        StatCard(label = "Balance", amount = net, sym = sym, accent = aes.accent, bg = aes.accent.copy(alpha = 0.12f), fg = if (net >= 0) aes.accent else Color(0xFFE8537A), isPositive = net >= 0)
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable private fun StatCard(label: String, amount: Int, sym: String, accent: Color, bg: Color, fg: Color, isPositive: Boolean) {
    Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(bg).padding(14.dp)) {
        Text(label, style = TextStyle(color = fg.copy(alpha = 0.6f), fontSize = 11.sp))
        Text("$sym ${String.format("%.2f", amount / 100.0)}", style = TextStyle(color = if (label == "Expense") Color(0xFFE8537A) else fg, fontSize = 15.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable private fun FilterBar(selected: EntryKind?, accent: Color, onSelect: (EntryKind?) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(label = "All", selected = selected == null, accent = accent, onClick = { onSelect(null) })
        FilterChip(label = "Income", selected = selected == EntryKind.INCOME, accent = accent, onClick = { onSelect(EntryKind.INCOME) })
        FilterChip(label = "Expense", selected = selected == EntryKind.EXPENSE, accent = accent, onClick = { onSelect(EntryKind.EXPENSE) })
    }
}

@Composable private fun FilterChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val bg = if (selected) accent else Color.Unspecified
    val fg = if (selected) Color.White else Color.Gray
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), color = bg, modifier = Modifier.height(34.dp)) {
        Text(label, style = TextStyle(color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium), modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
    }
}

@Composable private fun PeriodSelector(selected: Int, accent: Color, muted: Color, onPeriod: (Int) -> Unit) {
    val periods = listOf("All", "7D", "3M", "1Y")
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        periods.forEachIndexed { i, label ->
            val sel = selected == i
            Surface(onClick = { onPeriod(i) }, shape = RoundedCornerShape(16.dp), color = if (sel) accent.copy(alpha = 0.15f) else Color.Transparent, modifier = Modifier.height(30.dp).weight(1f)) {
                Text(label, style = TextStyle(color = if (sel) accent else muted, fontSize = 11.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 5.dp))
            }
        }
    }
}

@Composable private fun SearchBar(query: String, accent: Color, placeholder: Color, onQuery: (String) -> Unit) {
    OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), placeholder = { Text("Search entries...", color = placeholder) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = placeholder) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = accent.copy(alpha = 0.06f), unfocusedContainerColor = accent.copy(alpha = 0.04f)), shape = RoundedCornerShape(16.dp))
}

@Composable private fun DateSection(date: LocalDate, entries: List<LedgerEntry>, categories: List<Category>, aes: Aesthetic, showTitle: Boolean, showCategory: Boolean, showNotes: Boolean, showDate: Boolean, onEdit: (LedgerEntry) -> Unit, onDelete: (LedgerEntry) -> Unit, sym: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(date.format(dFmt), style = TextStyle(color = aes.text.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(vertical = 8.dp))
        entries.forEach { e ->
            val cat = categories.find { it.name == e.category }
            EntryRowCard(entry = e, category = cat, aes = aes, showTitle = showTitle, showCategory = showCategory, showNotes = showNotes, showDate = showDate, onEdit = onEdit, onDelete = onDelete, sym = sym)
        }
    }
}

@Composable private fun EntryRowCard(entry: LedgerEntry, category: Category?, aes: Aesthetic, showTitle: Boolean, showCategory: Boolean, showNotes: Boolean, showDate: Boolean, onEdit: (LedgerEntry) -> Unit, onDelete: (LedgerEntry) -> Unit, sym: String) {
    val isExpense = entry.kind == EntryKind.EXPENSE
    val amtColor = if (isExpense) Color(0xFFE8537A) else aes.accent

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onEdit(entry) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = aes.surfaceVariant.copy(alpha = 0.5f)) ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassCircle(icon = entry.icon, color = Color(entry.color), size = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (showTitle && entry.title.isNotBlank()) Text(entry.title, style = TextStyle(color = aes.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                if (showCategory && entry.category.isNotBlank()) Text(entry.category, style = TextStyle(color = aes.text.copy(alpha = 0.5f), fontSize = 11.sp))
                if (showNotes && entry.notes.isNotBlank()) Text(entry.notes, style = TextStyle(color = aes.text.copy(alpha = 0.35f), fontSize = 10.sp), maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${if (isExpense) "-" else "+"}$sym ${String.format("%.2f", entry.amountMinor / 100.0)}", style = TextStyle(color = amtColor, fontSize = 15.sp, fontWeight = FontWeight.Bold))
                if (showDate) Text(LocalDate.ofEpochDay(entry.dateMs / 86400000L).format(DateTimeFormatter.ofPattern("dd MMM")), style = TextStyle(color = aes.text.copy(alpha = 0.35f), fontSize = 10.sp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = aes.text.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable private fun EmptyState(text: Color, label: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = text, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, style = TextStyle(color = text, fontSize = 15.sp))
        }
    }
}

@Composable private fun HistoryScreen(entries: List<LedgerEntry>, categories: List<Category>, aes: Aesthetic, prefs: PrefStore, onEdit: (LedgerEntry) -> Unit, onDelete: (LedgerEntry) -> Unit) {
    val showTitle by prefs.showTitle.collectAsState(initial = true)
    val showCategory by prefs.showCategory.collectAsState(initial = true)
    val sym by prefs.currencySymbol.collectAsState(initial = "INR")

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionHeader("History", aes.text)
        if (entries.isEmpty()) EmptyState(text = aes.text.copy(alpha = 0.4f), label = "No entries yet")
        val grouped = entries.groupBy { LocalDate.ofEpochDay(it.dateMs / 86400000L) }
        grouped.entries.sortedByDescending { it.key }.forEach { (day, items) ->
            Text(day.format(dFmt), style = TextStyle(color = aes.text.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            items.forEach { e ->
                val cat = categories.find { it.name == e.category }
                val isExpense = e.kind == EntryKind.EXPENSE
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onEdit(e) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = aes.surfaceVariant.copy(alpha = 0.5f))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        GlassCircle(icon = e.icon, color = Color(e.color), size = 40.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (showTitle && e.title.isNotBlank()) Text(e.title, style = TextStyle(color = aes.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                            if (showCategory && e.category.isNotBlank()) Text(e.category, style = TextStyle(color = aes.text.copy(alpha = 0.5f), fontSize = 11.sp))
                        }
                        Text("${if (isExpense) "-" else "+"}$sym ${String.format("%.2f", e.amountMinor / 100.0)}", style = TextStyle(color = if (isExpense) Color(0xFFE8537A) else aes.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold))
                        IconButton(onClick = { onDelete(e) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Delete", tint = aes.text.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
        BottomPadding()
    }
}

@Composable private fun ReportsScreen(entries: List<LedgerEntry>, categories: List<Category>, aes: Aesthetic, prefs: PrefStore, selectedPeriod: Int, onPeriodChange: (Int) -> Unit) {
    val sym by prefs.currencySymbol.collectAsState(initial = "INR")

    val filtered = remember(entries, selectedPeriod) {
        when (selectedPeriod) {
            0 -> entries
            1 -> entries.filter { it.dateMs > System.currentTimeMillis() - 7 * 86400000L }
            2 -> entries.filter { it.dateMs > System.currentTimeMillis() - 90 * 86400000L }
            3 -> entries.filter { it.dateMs > System.currentTimeMillis() - 365 * 86400000L }
            else -> entries
        }
    }

    val income = filtered.filter { it.kind == EntryKind.INCOME }.sumOf { it.amountMinor }
    val expense = filtered.filter { it.kind == EntryKind.EXPENSE }.sumOf { it.amountMinor }
    val net = income - expense
    val catMap = filtered.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amountMinor } }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionHeader("Reports", aes.text)
        PeriodSelector(selected = selectedPeriod, accent = aes.accent, muted = aes.text.copy(alpha = 0.4f), onPeriod = onPeriodChange)

        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = aes.accent.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Net Balance", style = TextStyle(color = aes.text.copy(alpha = 0.6f), fontSize = 12.sp))
                Text("$sym ${String.format("%.2f", net / 100.0)}", style = TextStyle(color = if (net >= 0) aes.accent else Color(0xFFE8537A), fontSize = 32.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Income", style = TextStyle(color = aes.text.copy(alpha = 0.5f), fontSize = 11.sp))
                        Text("$sym ${String.format("%.2f", income / 100.0)}", style = TextStyle(color = aes.accent, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Expense", style = TextStyle(color = aes.text.copy(alpha = 0.5f), fontSize = 11.sp))
                        Text("$sym ${String.format("%.2f", expense / 100.0)}", style = TextStyle(color = Color(0xFFE8537A), fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = aes.surfaceVariant.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("By Category", style = TextStyle(color = aes.text, fontSize = 15.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                catMap.entries.sortedByDescending { it.value }.forEach { (cat, total) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(cat, style = TextStyle(color = aes.text, fontSize = 13.sp), modifier = Modifier.weight(1f))
                        Text("$sym ${String.format("%.2f", total / 100.0)}", style = TextStyle(color = aes.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                    }
                }
                if (catMap.isEmpty()) Text("No data for this period", style = TextStyle(color = aes.text.copy(alpha = 0.4f), fontSize = 13.sp))
            }
        }
        BottomPadding()
    }
}

@Composable private fun SettingsScreen(aes: Aesthetic, prefs: PrefStore, repo: SoloRepository, categories: List<Category>, onOpenProfile: () -> Unit) {
    val darkTheme by prefs.darkTheme.collectAsState(initial = false)
    val showTitle by prefs.showTitle.collectAsState(initial = true)
    val showCategory by prefs.showCategory.collectAsState(initial = true)
    val showNotes by prefs.showNotes.collectAsState(initial = true)
    val showDate by prefs.showDate.collectAsState(initial = true)
    val name by prefs.name.collectAsState(initial = "")

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionHeader("Settings", aes.text)
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = aes.surfaceVariant.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Display", style = TextStyle(color = aes.text, fontSize = 15.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                ToggleRow(label = "Show Title", checked = showTitle, accent = aes.accent, muted = aes.text.copy(alpha = 0.5f)) { prefs.setShowTitle(it) }
                ToggleRow(label = "Show Category", checked = showCategory, accent = aes.accent, muted = aes.text.copy(alpha = 0.5f)) { prefs.setShowCategory(it) }
                ToggleRow(label = "Show Notes", checked = showNotes, accent = aes.accent, muted = aes.text.copy(alpha = 0.5f)) { prefs.setShowNotes(it) }
                ToggleRow(label = "Show Date", checked = showDate, accent = aes.accent, muted = aes.text.copy(alpha = 0.5f)) { prefs.setShowDate(it) }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = aes.text.copy(alpha = 0.08f))
                ToggleRow(label = "Dark Theme", checked = darkTheme, accent = aes.accent, muted = aes.text.copy(alpha = 0.5f)) { prefs.setDarkTheme(it) }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = aes.surfaceVariant.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Profile", style = TextStyle(color = aes.text, fontSize = 15.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                OptionRow(icon = Icons.Default.Person, label = "Edit Profile", accent = aes.accent, text = aes.text) { onOpenProfile() }
            }
        }
        BottomPadding()
    }
}

@Composable private fun ProfileEditScreen(aes: Aesthetic, prefs: PrefStore, onBack: () -> Unit) {
    var name by remember { mutableStateOf(prefs.name.collectAsState(initial = "").value) }
    var sym by remember { mutableStateOf(prefs.currencySymbol.collectAsState(initial = "INR").value) }
    val showPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = aes.text) }
            Text("Edit Profile", style = TextStyle(color = aes.text, fontSize = 18.sp, fontWeight = FontWeight.Bold))
        }

        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = aes.surfaceVariant.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Your Name", style = TextStyle(color = aes.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                OutlinedTextField(value = name, onValueChange = {
                    name = it; kotlinx.coroutines.run { prefs.setName(it) }
                }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = aes.accent, unfocusedBorderColor = Color.Transparent), shape = RoundedCornerShape(14.dp))

                Spacer(modifier = Modifier.height(20.dp))
                Text("Currency Symbol", style = TextStyle(color = aes.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(8.dp))
                CurrencyPicker(selected = sym, accent = aes.accent, surface = aes.surfaceVariant, text = aes.text, muted = aes.text.copy(alpha = 0.5f)) { newSym ->
                    sym = newSym; kotlinx.coroutines.run { prefs.setCurrencySymbol(newSym) }
                }
            }
        }

        SupportSection(aes = aes)
        BottomPadding()
    }
}

@Composable private fun SupportSection(aes: Aesthetic) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = aes.accent.copy(alpha = 0.08f))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(Icons.Default.Favorite, null, tint = aes.accent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Support the Developer", style = TextStyle(color = aes.text, fontSize = 16.sp, fontWeight = FontWeight.Bold))
            }
            Text("If Solo Ledger adds value to your life, consider a small support. Every contribution fuels more features and improvements.", style = TextStyle(color = aes.text.copy(alpha = 0.6f), fontSize = 12.sp), modifier = Modifier.padding(bottom = 16.dp))
            SUPPORT_LINKS.forEach { (label, url, icon) ->
                SupportLinkRow(label = label, url = url, icon = icon, accent = aes.accent, text = aes.text)
            }
        }
    }
}

private val SUPPORT_LINKS = listOf(
    Triple("Buy Me a Coffee", "https://www.buymeacoffee.com/mkr_in", Icons.Default.LocalCafe),
    Triple("Patreon", "https://www.patreon.com/mkrinfinity", Icons.Default.CardMembership),
    Triple("Ko-fi", "https://ko-fi.com/mkrinfinity", Icons.Default.Coffee)
)

@Composable private fun SupportLinkRow(label: String, url: String, icon: ImageVector, accent: Color, text: Color) {
    val ctx = LocalContext.current
    Surface(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.1f)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = TextStyle(color = text, fontSize = 14.sp, fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
            Icon(Icons.Default.OpenInNew, null, tint = text.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable private fun CurrencyPicker(selected: String, accent: Color, surface: Color, text: Color, muted: Color, onSelect: (String) -> Unit) {
    val currencies = listOf("INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "NZD")
    Column {
        currencies.chunked(5).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { cur ->
                    val sel = cur == selected
                    Surface(onClick = { onSelect(cur) }, shape = RoundedCornerShape(12.dp), color = if (sel) accent else surface, modifier = Modifier.weight(1f)) {
                        Text(cur, style = TextStyle(color = if (sel) Color.White else text, fontSize = 12.sp, fontWeight = FontWeight.Medium), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 10.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = if (!currencies.contains(selected)) selected else "", onValueChange = onSelect, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Custom currency symbol", color = muted) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = Color.Transparent), shape = RoundedCornerShape(14.dp))
    }
}

@Composable private fun ToggleRow(label: String, checked: Boolean, accent: Color, muted: Color, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = TextStyle(color = muted, fontSize = 14.sp), modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = accent, checkedTrackColor = accent.copy(alpha = 0.4f)))
    }
}

@Composable private fun OptionRow(icon: ImageVector, label: String, accent: Color, text: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = TextStyle(color = text, fontSize = 14.sp), modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = text.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable private fun SectionHeader(title: String, text: Color) {
    Text(title, style = TextStyle(color = text, fontSize = 22.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))
}

@Composable private fun BottomPadding() { Spacer(modifier = Modifier.height(80.dp)) }

@Composable private fun DeleteDialog(entry: LedgerEntry, aes: Aesthetic, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { Text("Delete", color = Color(0xFFE8537A), onClick = onConfirm) }, dismissButton = { Text("Cancel", color = aes.text.copy(alpha = 0.5f), onClick = onDismiss) }, title = { Text("Delete entry?", color = aes.text) }, text = { Text("\"${entry.title}\" will be permanently deleted.", color = aes.text.copy(alpha = 0.6f)) }, containerColor = aes.surfaceVariant)
}

@Composable private fun EntrySheet(draft: EntryDraft, categories: List<Category>, aes: Aesthetic, onChange: (EntryDraft) -> Unit, onSave: (EntryDraft) -> Unit, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.BottomCenter) {
        Card(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), colors = CardDefaults.cardColors(containerColor = aes.background)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (draft.editingId != null) "Edit Entry" else "New Entry", style = TextStyle(color = aes.text, fontSize = 18.sp, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close", tint = aes.text) }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(EntryKind.EXPENSE to "Expense", EntryKind.INCOME to "Income").forEach { (kind, label) ->
                        val sel = draft.kind == kind
                        val c = if (sel) (if (kind == EntryKind.EXPENSE) Color(0xFFE8537A) else aes.accent) else aes.surfaceVariant
                        Surface(onClick = { onChange(draft.copy(kind = kind)) }, shape = RoundedCornerShape(16.dp), color = c, modifier = Modifier.weight(1f)) {
                            Text(label, style = TextStyle(color = if (sel) Color.White else aes.text, fontSize = 13.sp, fontWeight = FontWeight.Medium), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(value = draft.amountStr, onValueChange = { onChange(draft.copy(amountStr = it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Amount") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = aes.accent, unfocusedBorderColor = Color.Transparent), shape = RoundedCornerShape(14.dp))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(value = draft.title, onValueChange = { onChange(draft.copy(title = it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = aes.accent, unfocusedBorderColor = Color.Transparent), shape = RoundedCornerShape(14.dp))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(value = draft.notes, onValueChange = { onChange(draft.copy(notes = it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Notes (optional)") }, maxLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = aes.accent, unfocusedBorderColor = Color.Transparent), shape = RoundedCornerShape(14.dp))
                Spacer(modifier = Modifier.height(10.dp))

                Text("Category", style = TextStyle(color = aes.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories.filter { it.kind == draft.kind || draft.kind == EntryKind.EXPENSE }) {
                        val sel = draft.category == it.name
                        Surface(onClick = { onChange(draft.copy(category = it.name, color = it.color, icon = it.icon)) }, shape = RoundedCornerShape(14.dp), color = if (sel) Color(it.color) else aes.surfaceVariant) {
                            Text(it.name, style = TextStyle(color = if (sel) Color.White else Color(it.color), fontSize = 11.sp), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                Surface(onClick = {
                    val calendar = java.util.Calendar.getInstance()
                    DatePickerDialog(ctx, { _, y, m, d ->
                        calendar.set(y, m, d)
                        onChange(draft.copy(dateMs = calendar.timeInMillis))
                    }, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)).show()
                }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), color = aes.surfaceVariant) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = aes.accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(java.time.Instant.ofEpochMilli(draft.dateMs).atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(dFmt), style = TextStyle(color = aes.text, fontSize = 14.sp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = { onSave(draft) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = aes.accent)) {
                    Text(if (draft.editingId != null) "Update Entry" else "Save Entry", style = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable private fun AnimatedBottomNav(selected: NavTab, accent: Color, surface: Color, text: Color, muted: Color, onSelect: (NavTab) -> Unit) {
    val items = listOf(NavTab.Home to Icons.Default.Home, NavTab.History to Icons.Default.History, NavTab.Reports to Icons.Default.BarChart, NavTab.Settings to Icons.Default.Settings, NavTab.Profile to Icons.Default.Person)
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = surface), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            items.forEach { (tab, icon) ->
                val sel = selected == tab
                val scale by animateFloatAsState(targetValue = if (sel) 1.15f else 1f, label = "")
                Column(onClick = { onSelect(tab) }, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)) {
                    Icon(icon, null, tint = if (sel) accent else muted, modifier = Modifier.scale(scale).size(if (sel) 26.dp else 22.dp))
                    if (sel) Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }, style = TextStyle(color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable private fun GlassCircle(icon: String, color: Color, size: Dp) {
    val icons = mapOf("payments" to Icons.Default.Payments, "restaurant" to Icons.Default.Restaurant, "directions_car" to Icons.Default.DirectionsCar, "local_gas_station" to Icons.Default.LocalGasStation, "shopping_bag" to Icons.Default.ShoppingBag, "home" to Icons.Default.Home, "movie" to Icons.Default.Movie, "school" to Icons.Default.School, "flight" to Icons.Default.Flight, "fitness_center" to Icons.Default.FitnessCenter, "medical_services" to Icons.Default.MedicalServices, "work" to Icons.Default.Work, "celebration" to Icons.Default.Celebration, "pets" to Icons.Default.Pets, "checkroom" to Icons.Default.Checkroom, "coffee" to Icons.Default.Coffee)
    val mapped = icons[icon] ?: Icons.Default.Payments
    Surface(modifier = Modifier.size(size), shape = CircleShape, color = color.copy(alpha = 0.2f)) {
        Box(contentAlignment = Alignment.Center) { Icon(mapped, null, tint = color, modifier = Modifier.size(size * 0.5f)) }
    }
}