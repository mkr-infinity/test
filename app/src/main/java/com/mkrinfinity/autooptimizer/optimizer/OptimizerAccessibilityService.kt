package com.mkrinfinity.autooptimizer.optimizer

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class OptimizerAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val action = PendingAction.current ?: return
        val root = rootInActiveWindow ?: return
        when (action.type) {
            ActionType.FORCE_STOP -> handleForceStop(root)
            ActionType.CLEAR_CACHE -> handleClearCache(root)
        }
    }

    private fun handleForceStop(root: AccessibilityNodeInfo) {
        val byId = root.findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button")
        val node = byId.firstOrNull { it.isEnabled && it.isClickable }
            ?: findClickableByText(root, "Force stop", "FORCE STOP")
        if (node != null) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            PendingAction.current = null
            Toast.makeText(this, "Force stop requested", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleClearCache(root: AccessibilityNodeInfo) {
        val node = findClickableByText(root, "Clear cache", "CLEAR CACHE")
        if (node != null) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            PendingAction.current = null
            Toast.makeText(this, "Cache clear requested", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findClickableByText(root: AccessibilityNodeInfo, vararg labels: String): AccessibilityNodeInfo? {
        for (label in labels) {
            root.findAccessibilityNodeInfosByText(label)
                .firstOrNull { it.isEnabled && it.isClickable }
                ?.let { return it }
        }
        return null
    }

    override fun onInterrupt() = Unit
}

enum class ActionType { FORCE_STOP, CLEAR_CACHE }
data class Pending(val type: ActionType, val packageName: String? = null)
object PendingAction { @Volatile var current: Pending? = null }
