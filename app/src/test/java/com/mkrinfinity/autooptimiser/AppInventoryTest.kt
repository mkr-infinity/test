package com.mkrinfinity.autooptimiser

import com.mkrinfinity.autooptimiser.model.AppInventoryItem
import com.mkrinfinity.autooptimiser.model.AppInventoryStore
import com.mkrinfinity.autooptimiser.model.ProtectedApp
import com.mkrinfinity.autooptimiser.model.ProtectedApps
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppInventoryTest {
    private fun app(packageName: String) = AppInventoryItem(
        packageName = packageName,
        label = packageName.substringAfterLast('.'),
        versionName = "1.0",
        versionCode = 1,
        firstInstallTimeMillis = 1,
        lastUpdateTimeMillis = 2,
        isSystemApp = false,
        isEnabled = true,
        isLaunchable = true,
        sizeBytes = 100
    )

    @Test
    fun snapshotIsSortedAndCanBeReplaced() {
        val store = AppInventoryStore(listOf(app("z.example"), app("a.example")))

        assertEquals(listOf("a.example", "z.example"), store.snapshot(3).apps.map { it.packageName })
        store.replaceAll(listOf(app("b.example")))

        assertEquals(listOf("b.example"), store.snapshot(4).apps.map { it.packageName })
    }

    @Test
    fun protectedAppsFilterInventoryWithoutChangingIt() {
        val protected = ProtectedApps(listOf(ProtectedApp("a.example", 5)))
        val apps = listOf(app("a.example"), app("b.example"))

        assertTrue(protected.isProtected("a.example"))
        assertEquals(listOf("b.example"), protected.unprotected(apps).map { it.packageName })
        assertTrue(protected.unprotect("a.example"))
        assertFalse(protected.isProtected("a.example"))
    }
}
