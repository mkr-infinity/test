package com.sololedger.data

import androidx.room.*

enum class EntryKind { INCOME, EXPENSE }

@Entity(tableName = "entries")
data class LedgerEntry(
    @PrimaryKey val id: String,
    val kind: EntryKind,
    val title: String,
    val notes: String,
    val amountMinor: Int,
    val category: String,
    val color: Long,
    val icon: String,
    val dateMs: Long,
    val createdAt: Long
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val name: String,
    val kind: EntryKind,
    val color: Long,
    val icon: String,
    val isCustom: Boolean
)

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY dateMs DESC")
    fun getAll(): kotlinx.coroutines.flow.Flow<List<LedgerEntry>>
    @Query("SELECT * FROM entries WHERE id=:id")
    suspend fun getById(id: String): LedgerEntry?
    @Insert(onConflict = REPLACE)
    suspend fun insert(e: LedgerEntry)
    @Update
    suspend fun update(e: LedgerEntry)
    @Delete
    suspend fun delete(e: LedgerEntry)
    @Query("SELECT * FROM entries WHERE id=:id")
    suspend fun deleteById(id: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAll(): kotlinx.coroutines.flow.Flow<List<Category>>
    @Insert(onConflict = REPLACE)
    suspend fun insert(c: Category)
    @Query("DELETE FROM categories WHERE name=:name AND isCustom=1")
    suspend fun deleteCustom(name: String)
}

@Database(entities = [LedgerEntry::class, Category::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun categoryDao(): CategoryDao
}

class SoloRepository(private val db: AppDatabase) {
    private val eDao get() = db.entryDao()
    private val cDao get() = db.categoryDao()

    fun getAll() = eDao.getAll()
    suspend fun get(id: String) = eDao.getById(id)
    suspend fun insert(e: LedgerEntry) = eDao.insert(e)
    suspend fun update(e: LedgerEntry) = eDao.update(e)
    suspend fun delete(id: String) = eDao.deleteById(id)
    fun getAllCategories() = cDao.getAll()
    suspend fun insertCategory(c: Category) = cDao.insert(c)
    suspend fun deleteCategory(name: String) = cDao.deleteCustom(name)
}