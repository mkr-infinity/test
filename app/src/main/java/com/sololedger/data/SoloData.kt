package com.sololedger.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

private val Context.prefs by preferencesDataStore(name = "solo_prefs_v2")

enum class EntryKind { INCOME, EXPENSE, TRANSFER }

@Entity(tableName = "entries")
data class EntryRow(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val kind: String,
    val amountMinor: Long,
    val title: String,
    val category: String,
    val notes: String,
    val occurredAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val inBin: Boolean = false,
)

data class EntryDraft(
    val id: String? = null,
    val kind: EntryKind = EntryKind.EXPENSE,
    val amountMinor: Long = 0L,
    val title: String = "",
    val category: String = "",
    val notes: String = "",
    val occurredAt: Long = System.currentTimeMillis(),
)

data class PreferencesState(
    val onboarded: Boolean = false,
    val name: String = "",
    val symbol: String = "INR",
    val showTitle: Boolean = true,
    val showCategory: Boolean = true,
    val showNotes: Boolean = false,
    val showDate: Boolean = true,
    val darkMode: Boolean = false,
    val aestheticIdx: Int = 0,  // Aurora by default
)

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries WHERE inBin = 0 ORDER BY occurredAt DESC")
    fun observeActive(): Flow<List<EntryRow>>

    @Query("SELECT * FROM entries WHERE inBin = 1 ORDER BY updatedAt DESC")
    fun observeBin(): Flow<List<EntryRow>>

    @Query("SELECT * FROM entries ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<EntryRow>>

    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): EntryRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: EntryRow)

    @Query("UPDATE entries SET inBin = 1, updatedAt = :ts WHERE id = :id")
    suspend fun moveToBin(id: String, ts: Long = System.currentTimeMillis())

    @Query("UPDATE entries SET inBin = 0, updatedAt = :ts WHERE id = :id")
    suspend fun restore(id: String, ts: Long = System.currentTimeMillis())

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteForever(id: String)

    @Query("DELETE FROM entries WHERE inBin = 1")
    suspend fun clearBin()

    @Query("DELETE FROM entries")
    suspend fun deleteAll()
}

@Database(entities = [EntryRow::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
}

private object Prefs {
    val onboarded = booleanPreferencesKey("onboarded_v2")
    val name = stringPreferencesKey("name_v2")
    val sym = stringPreferencesKey("symbol_v2")
    val showTitle = booleanPreferencesKey("show_title_v2")
    val showCategory = booleanPreferencesKey("show_cat_v2")
    val showNotes = booleanPreferencesKey("show_notes_v2")
    val showDate = booleanPreferencesKey("show_date_v2")
    val darkMode = booleanPreferencesKey("dark_mode_v2")
    val aestheticIdx = intPreferencesKey("aesthetic_idx_v2")
}

class PrefStore(private val ctx: Context) {
    val flow: Flow<PreferencesState> = ctx.prefs.data.map { p ->
        PreferencesState(
            onboarded = p[Prefs.onboarded] ?: false,
            name = p[Prefs.name] ?: "",
            symbol = p[Prefs.sym] ?: "INR",
            showTitle = p[Prefs.showTitle] ?: true,
            showCategory = p[Prefs.showCategory] ?: true,
            showNotes = p[Prefs.showNotes] ?: false,
            showDate = p[Prefs.showDate] ?: true,
            darkMode = p[Prefs.darkMode] ?: false,
            aestheticIdx = p[Prefs.aestheticIdx] ?: 0,
        )
    }

    suspend fun setOnboard(v: Boolean) = edit(Prefs.onboarded, v)
    suspend fun setName(v: String) = edit(Prefs.name, v)
    suspend fun setSym(v: String) = edit(Prefs.sym, v.ifBlank { "INR" })
    suspend fun setShowTitle(v: Boolean) = edit(Prefs.showTitle, v)
    suspend fun setShowCategory(v: Boolean) = edit(Prefs.showCategory, v)
    suspend fun setShowNotes(v: Boolean) = edit(Prefs.showNotes, v)
    suspend fun setShowDate(v: Boolean) = edit(Prefs.showDate, v)
    suspend fun setDark(v: Boolean) = edit(Prefs.darkMode, v)
    suspend fun setAesthetic(v: Int) = edit(Prefs.aestheticIdx, v)

    private suspend fun edit(key: Preferences.Key<*>, value: Any) {
        ctx.prefs.edit { p ->
            @Suppress("UNCHECKED_CAST")
            when (value) {
                is Boolean -> (p as androidx.datastore.preferences.core.MutablePreferences)[key as Preferences.Key<Boolean>] = value
                is String -> (p as androidx.datastore.preferences.core.MutablePreferences)[key as Preferences.Key<String>] = value
                is Int -> (p as androidx.datastore.preferences.core.MutablePreferences)[key as Preferences.Key<Int>] = value
            }
        }
    }
}

class Repo(private val dao: EntryDao, val store: PrefStore) {
    val entries: Flow<List<EntryRow>> = dao.observeActive()
    val binEntries: Flow<List<EntryRow>> = dao.observeBin()
    val prefs: Flow<PreferencesState> = store.flow

    suspend fun save(draft: EntryDraft) {
        val existing = draft.id?.let { dao.findById(it) }
        val now = System.currentTimeMillis()
        dao.upsert(
            EntryRow(
                id = draft.id ?: existing?.id ?: UUID.randomUUID().toString(),
                kind = draft.kind.name.lowercase(Locale.getDefault()),
                amountMinor = draft.amountMinor,
                title = draft.title.trim(),
                category = draft.category.trim(),
                notes = draft.notes.trim(),
                occurredAt = draft.occurredAt,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                inBin = false,
            )
        )
    }

    suspend fun delete(id: String) = dao.moveToBin(id)
    suspend fun restore(id: String) = dao.restore(id)
    suspend fun deleteForever(id: String) = dao.deleteForever(id)
    suspend fun clearBin() = dao.clearBin()
}
