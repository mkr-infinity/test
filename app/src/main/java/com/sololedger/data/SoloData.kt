package com.sololedger.data

import android.content.Context
import android.content.SharedPreferences

class PrefStore(ctx: Context) {
    private val sp: SharedPreferences = ctx.getSharedPreferences("solo_prefs", Context.MODE_PRIVATE)

    var currencySymbol: String
        get() = sp.getString("currency_symbol", "₹") ?: "₹"
        set(v) = sp.edit().putString("currency_symbol", v).apply()

    var aestheticIdx: Int
        get() = sp.getInt("aesthetic_idx", 0)
        set(v) = sp.edit().putInt("aesthetic_idx", v).apply()

    var darkTheme: Boolean
        get() = sp.getBoolean("dark_theme", false)
        set(v) = sp.edit().putBoolean("dark_theme", v).apply()

    var showTitle: Boolean
        get() = sp.getBoolean("show_title", true)
        set(v) = sp.edit().putBoolean("show_title", v).apply()

    var showCategory: Boolean
        get() = sp.getBoolean("show_category", true)
        set(v) = sp.edit().putBoolean("show_category", v).apply()

    var showNotes: Boolean
        get() = sp.getBoolean("show_notes", true)
        set(v) = sp.edit().putBoolean("show_notes", v).apply()

    var showDate: Boolean
        get() = sp.getBoolean("show_date", true)
        set(v) = sp.edit().putBoolean("show_date", v).apply()
}

data class LedgerEntry(
    val id: Long,
    val title: String,
    val amountMinor: Int,
    val kind: Int,
    val category: String,
    val note: String,
    val dateMs: Long,
    val createdAt: Long,
    val deleted: Boolean
) {
    companion object {
        fun make(title: String, amountMinor: Int, kind: Int, category: String, note: String, dateMs: Long): LedgerEntry {
            return LedgerEntry(
                id = System.currentTimeMillis(),
                title = title,
                amountMinor = amountMinor,
                kind = kind,
                category = category,
                note = note,
                dateMs = dateMs,
                createdAt = System.currentTimeMillis(),
                deleted = false
            )
        }
    }
}

object Repo {
    private lateinit var sp: SharedPreferences

    fun init(ctx: Context) {
        sp = ctx.getSharedPreferences("solo_data", Context.MODE_PRIVATE)
    }

    private fun saveAll(entries: List<LedgerEntry>) {
        val json = entries.joinToString("\n") { e ->
            listOf(e.id, e.title, e.amountMinor, e.kind, e.category, e.note, e.dateMs, e.createdAt, e.deleted).joinToString("|")
        }
        sp.edit().putString("entries", json).apply()
    }

    private fun parseEntry(line: String): LedgerEntry {
        val parts = line.split("|")
        return LedgerEntry(
            id = parts[0].toLong(),
            title = parts[1],
            amountMinor = parts[2].toInt(),
            kind = parts[3].toInt(),
            category = parts[4],
            note = parts.getOrElse(5) { "" },
            dateMs = parts.getOrElse(6) { System.currentTimeMillis().toString() }.toLong(),
            createdAt = parts.getOrElse(7) { "0" }.toLong(),
            deleted = parts.getOrElse(8) { "false" }.toBoolean()
        )
    }

    private fun getAllEntries(): List<LedgerEntry> =
        sp.getString("entries", "")?.split("\n")?.filter { it.isNotBlank() }?.map { parseEntry(it) } ?: emptyList()

    fun entries(): List<LedgerEntry> = getAllEntries().filter { !it.deleted }
    fun deletedEntries(): List<LedgerEntry> = getAllEntries().filter { it.deleted }

    fun addEntry(title: String, amountMinor: Int, kind: Int, category: String, note: String, dateMs: Long): Long {
        val newId = (getAllEntries().maxOfOrNull { it.id } ?: 0L) + 1
        val entry = LedgerEntry(newId, title, amountMinor, kind, category, note, dateMs, System.currentTimeMillis(), false)
        val all = getAllEntries() + entry
        saveAll(all)
        return entry.id
    }

    fun deleteEntry(id: Long) {
        val all = getAllEntries().map { if (it.id == id) it.copy(deleted = true) else it }
        saveAll(all)
    }

    fun restoreEntry(id: Long) {
        val all = getAllEntries().map { if (it.id == id) it.copy(deleted = false) else it }
        saveAll(all)
    }

    fun deleteForever(id: Long) {
        val all = getAllEntries().filter { it.id != id }
        saveAll(all)
    }

    fun updateEntry(id: Long, title: String, amountMinor: Int, kind: Int, category: String, note: String, dateMs: Long) {
        val all = getAllEntries().map { e ->
            if (e.id == id) e.copy(title = title, amountMinor = amountMinor, kind = kind, category = category, note = note, dateMs = dateMs)
            else e
        }
        saveAll(all)
    }

    fun emptyBin() {
        val all = getAllEntries().filter { !it.deleted }
        saveAll(all)
    }
}