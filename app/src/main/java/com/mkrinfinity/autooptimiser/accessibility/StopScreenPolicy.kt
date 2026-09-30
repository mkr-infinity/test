package com.mkrinfinity.autooptimiser.accessibility

import java.util.Locale

/** Immutable facts: the decision layer has no Android objects and never owns a node. */
internal data class StopNodeFacts(
    val id: String = "",
    val text: String = "",
    val parent: Int = -1,
    val clickable: Boolean = false,
    val enabled: Boolean = true,
    val visible: Boolean = true
)

internal data class StopScreenFacts(
    val owner: String,
    val windowId: Int,
    val nodes: List<StopNodeFacts>,
    val complete: Boolean = true
)

/**
 * Intentionally conservative English/AOSP vocabulary. Labels alone are not identity;
 * generic Stop/OK controls are not evidence. Unsupported OEM/localized layouts time out.
 */
internal object StopScreenPolicy {
    private fun String.normalized() = trim().lowercase(Locale.ROOT)
    private val stopLabels = setOf("force stop", "force stop?")
    private val warnings = setOf(
        "if you force stop an app, it may misbehave.",
        "if you force stop an app, it may misbehave"
    )

    fun identifies(screen: StopScreenFacts, item: StopItem, settingsPackage: String): Boolean {
        if (!screen.complete || screen.owner != settingsPackage) return false
        val visible = screen.nodes.filter { it.visible }
        return visible.any { it.text.trim() == item.packageName } &&
            visible.any { it.text.trim() == item.label } &&
            visible.any { it.text.normalized() in setOf("app info", "application info") } &&
            visible.none { it.id == "android:id/message" }
    }

    /** Return only one explicit, labelled Force stop action, never an arbitrary ancestor. */
    fun stopButton(screen: StopScreenFacts, item: StopItem, settingsPackage: String): Int? {
        if (!identifies(screen, item, settingsPackage)) return null
        val allowedIds = setOf(
            "$settingsPackage:id/force_stop_button", "$settingsPackage:id/force_stop",
            "$settingsPackage:id/button3"
        )
        val candidates = screen.nodes.indices.mapNotNull { index ->
            val label = screen.nodes[index]
            if (!label.visible || label.text.normalized() != "force stop") return@mapNotNull null
            var candidate = index
            repeat(3) {
                val node = screen.nodes.getOrNull(candidate) ?: return@mapNotNull null
                if (node.visible && node.id in allowedIds && (node.clickable || !node.enabled)) {
                    return@mapNotNull candidate
                }
                candidate = node.parent
            }
            null
        }.distinct()
        return candidates.singleOrNull()
    }

    fun confirmationButton(
        screen: StopScreenFacts,
        settingsPackage: String,
        sourceWindowId: Int,
        sourceStillIdentified: Boolean,
        stopWasClicked: Boolean
    ): Int? {
        if (!stopWasClicked || !sourceStillIdentified || !screen.complete ||
            screen.owner != settingsPackage || screen.windowId == sourceWindowId) return null
        val nodes = screen.nodes
        val messages = nodes.indices.filter {
            nodes[it].visible && nodes[it].id == "android:id/message" &&
                nodes[it].text.normalized() in warnings
        }
        val positives = nodes.indices.filter {
            val node = nodes[it]
            node.visible && node.enabled && node.clickable && node.id == "android:id/button1" &&
                (node.text.normalized() == "ok" || node.text.normalized() in stopLabels)
        }
        val negatives = nodes.indices.filter {
            val node = nodes[it]
            node.visible && node.enabled && node.clickable && node.id == "android:id/button2" &&
                node.text.normalized() == "cancel"
        }
        val message = messages.singleOrNull() ?: return null
        val positive = positives.singleOrNull() ?: return null
        val negative = negatives.singleOrNull() ?: return null
        // The warning and both buttons must belong to the same standard dialog panel.
        fun ancestors(index: Int): Set<Int> {
            val found = linkedSetOf<Int>()
            var parent = nodes[index].parent
            while (parent in nodes.indices && found.add(parent)) parent = nodes[parent].parent
            return found
        }
        val common = ancestors(message).intersect(ancestors(positive)).intersect(ancestors(negative))
        if (common.none { nodes[it].id == "android:id/parentPanel" ||
                nodes[it].id == "$settingsPackage:id/parentPanel" }) return null
        return positive
    }

    fun verified(
        screen: StopScreenFacts,
        item: StopItem,
        settingsPackage: String,
        sourceWindowId: Int,
        stopWasClicked: Boolean,
        wasStoppedBefore: Boolean?,
        isStoppedNow: Boolean?
    ): Boolean {
        if (!stopWasClicked || wasStoppedBefore != false || isStoppedNow != true ||
            screen.windowId != sourceWindowId) return false
        val button = stopButton(screen, item, settingsPackage) ?: return false
        return !screen.nodes[button].enabled
    }
}
