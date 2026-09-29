package com.mkrinfinity.autooptimiser

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import com.mkrinfinity.autooptimiser.data.AppFilter
import com.mkrinfinity.autooptimiser.data.AppRecord
import com.mkrinfinity.autooptimiser.data.AppSelectionLogic
import com.mkrinfinity.autooptimiser.data.AppSort
import com.mkrinfinity.autooptimiser.model.AppInventoryItem
import kotlin.test.Test
import kotlin.test.assertEquals

class AppSelectionTest {
    private class TestIcon : Drawable() {
        override fun draw(canvas: Canvas) = Unit
        override fun setAlpha(alpha: Int) = Unit
        override fun setColorFilter(colorFilter: ColorFilter?) = Unit
        override fun getOpacity(): Int = 0
    }

    private fun app(name: String, system: Boolean, running: Boolean, protected: Boolean, size: Long) = AppRecord(
        inventory = AppInventoryItem("com.example.$name", name, "1", 1, 1, 2, system, true, true, size),
        icon = TestIcon(), isRunning = running, isProtected = protected, protectionReason = null
    )

    @Test
    fun filtersAndSortsOnlyTheRequestedVisibleApps() {
        val records = listOf(app("Zulu", false, true, false, 10), app("Alpha", false, false, true, 80), app("System", true, true, true, 50))

        val result = AppSelectionLogic.filter(records, "", AppFilter.USER, AppSort.SIZE)

        assertEquals(listOf("Alpha", "Zulu"), result.map { it.label })
    }
}
