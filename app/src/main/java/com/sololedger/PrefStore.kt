package com.sololedger

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.sololedger.data.Category
import com.sololedger.data.EntryKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PrefStore(ctx: Context) {
    private val sp: SharedPreferences = ctx.getSharedPreferences("solo_prefs_v2", Context.MODE_PRIVATE)

    private fun stringFlow(key: String, default: String) = MutableStateFlow(sp.getString(key, default) ?: default).also {
        it.value = sp.getString(key, default) ?: default
    }

    private fun boolFlow(key: String, default: Boolean) = MutableStateFlow(sp.getBoolean(key, default)).also {
        it.value = sp.getBoolean(key, default)
    }

    private fun intFlow(key: String, default: Int) = MutableStateFlow(sp.getInt(key, default)).also {
        it.value = sp.getInt(key, default)
    }

    val name             = stringFlow("user_name_v2", "")
    val currencySymbol   = stringFlow("currency_sym_v2", "INR")
    val darkTheme        = boolFlow("dark_theme_v2", false)
    val aestheticIdx     = intFlow("aesthetic_idx_v2", 0)
    val showTitle        = boolFlow("show_title_v2", true)
    val showCategory     = boolFlow("show_category_v2", true)
    val showNotes        = boolFlow("show_notes_v2", true)
    val showDate         = boolFlow("show_date_v2", true)

    fun setName(v: String)             = sp.edit { putString("user_name_v2", v) }
    fun setCurrencySymbol(v: String)   = sp.edit { putString("currency_sym_v2", v) }
    fun setDarkTheme(v: Boolean)       = sp.edit { putBoolean("dark_theme_v2", v) }
    fun setAestheticIdx(v: Int)        = sp.edit { putInt("aesthetic_idx_v2", v) }
    fun setShowTitle(v: Boolean)       = sp.edit { putBoolean("show_title_v2", v) }
    fun setShowCategory(v: Boolean)    = sp.edit { putBoolean("show_category_v2", v) }
    fun setShowNotes(v: Boolean)       = sp.edit { putBoolean("show_notes_v2", v) }
    fun setShowDate(v: Boolean)        = sp.edit { putBoolean("show_date_v2", v) }

    private var seeded = false
    suspend fun seedDefaultCategories() {
        if (seeded) return
        seeded = true
        // Categories seeded directly into DB on first launch
    }
}
