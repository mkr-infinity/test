package com.mkrinfinity.autooptimiser.model

enum class ProtectionReason {
    USER_SELECTED,
    SYSTEM_CRITICAL,
    POLICY
}

data class ProtectedApp(
    val packageName: String,
    val addedAtEpochMillis: Long,
    val reason: ProtectionReason = ProtectionReason.USER_SELECTED
) {
    init {
        require(packageName.isNotBlank()) { "packageName must not be blank" }
        require(addedAtEpochMillis >= 0) { "addedAtEpochMillis must not be negative" }
    }
}

/** Repository for package exclusions. It never mutates an inventory item. */
class ProtectedApps(initial: Iterable<ProtectedApp> = emptyList()) {
    private val entries = linkedMapOf<String, ProtectedApp>()

    init {
        initial.forEach(::protect)
    }

    fun protect(app: ProtectedApp): Boolean = entries.put(app.packageName, app) == null

    fun unprotect(packageName: String): Boolean = entries.remove(packageName) != null

    fun isProtected(packageName: String): Boolean = entries.containsKey(packageName)

    fun get(packageName: String): ProtectedApp? = entries[packageName]

    fun all(): List<ProtectedApp> = entries.values.sortedBy { it.packageName }

    fun unprotected(
        inventory: Iterable<AppInventoryItem>
    ): List<AppInventoryItem> = inventory.filterNot { isProtected(it.packageName) }
}
