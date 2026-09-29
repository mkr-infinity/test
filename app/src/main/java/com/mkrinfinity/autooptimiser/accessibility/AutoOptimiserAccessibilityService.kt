package com.mkrinfinity.autooptimiser.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/** A user-initiated, bounded automation. Never traverses an unrelated application window. */
enum class StopStep { IDLE, OPENING_APP_INFO, WAITING_FOR_APP_INFO, FINDING_STOP_BUTTON, WAITING_FOR_CONFIRMATION, VERIFYING, RETURNING, COMPLETED, FAILED, CANCELLED, TIMED_OUT }

data class StopItem(val packageName: String, val label: String)
data class StopResult(val item: StopItem, val outcome: StopOutcome, val detail: String)
enum class StopOutcome { SUCCESS, SKIPPED, FAILED }
data class StopProgress(
    val step: StopStep = StopStep.IDLE,
    val item: StopItem? = null,
    val current: Int = 0,
    val total: Int = 0,
    val results: List<StopResult> = emptyList(),
    val cancelled: Boolean = false
)

object StopSession {
    private val mutable = MutableStateFlow(StopProgress())
    val progress: StateFlow<StopProgress> = mutable
    internal fun set(value: StopProgress) { mutable.value = value }
    fun start(items: List<StopItem>): Boolean {
        val service = AutoOptimiserAccessibilityService.instance ?: return false
        return service.start(items)
    }
    fun cancel() { AutoOptimiserAccessibilityService.instance?.cancel() }
}

class AutoOptimiserAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var queue = ArrayDeque<StopItem>()
    private var current: StopItem? = null
    private var results = ArrayList<StopResult>()
    private var total = 0
    private var cancelRequested = false
    private var stopClicked = false
    private var timedStep: Runnable? = null
    private var lastEventTime = 0L
    private var step = StopStep.IDLE
    private val automationEngine = AppStopAutomationEngine()

    override fun onServiceConnected() {
        instance = this
        automationEngine.reset()
        setEvents(false)
    }

    @Synchronized
    fun start(items: List<StopItem>): Boolean {
        if (step != StopStep.IDLE || items.isEmpty() || items.any { it.packageName == packageName }) return false
        queue = ArrayDeque(items.distinctBy { it.packageName })
        total = queue.size
        results = ArrayList(total)
        cancelRequested = false
        next()
        return true
    }

    @Synchronized
    fun cancel() {
        if (step == StopStep.IDLE) return
        cancelRequested = true
        queue.clear()
        finishCurrent(StopOutcome.SKIPPED, "Cancelled before a stop could be verified", true)
    }

    private fun next() {
        if (cancelRequested || queue.isEmpty()) {
            finishSession()
            return
        }
        current = queue.removeFirst()
        stopClicked = false
        setEvents(true)
        setStep(StopStep.OPENING_APP_INFO)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${current!!.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (runCatching { startActivity(intent) }.isFailure) {
            finishCurrent(StopOutcome.FAILED, "Android could not open App Info")
            return
        }
        setStep(StopStep.WAITING_FOR_APP_INFO)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (step == StopStep.IDLE || cancelRequested || current == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastEventTime < 120L) return
        lastEventTime = now
        val windowPackage = event.packageName?.toString().orEmpty()
        if (!isTrustedSettings(windowPackage)) return
        val root = rootInActiveWindow ?: return
        if (!isTrustedSettings(root.packageName?.toString().orEmpty())) return
        when (step) {
            StopStep.WAITING_FOR_APP_INFO, StopStep.FINDING_STOP_BUTTON -> inspectAppInfo(root)
            StopStep.WAITING_FOR_CONFIRMATION -> inspectConfirmation(root)
            StopStep.VERIFYING -> inspectVerification(root)
            else -> Unit
        }
    }

    private fun inspectAppInfo(root: AccessibilityNodeInfo) {
        val target = current ?: return
        // Verify target identity in App Info before clicking. OEM layouts without an
        // identifiable target are skipped, not clicked blindly.
        if (!hasText(root, target.label) && !hasText(root, target.packageName)) return
        setStep(StopStep.FINDING_STOP_BUTTON)
        val stop = findClickable(root) { node ->
            val text = node.text?.toString()?.trim()?.lowercase(Locale.ROOT).orEmpty()
            val description = node.contentDescription?.toString()?.trim()?.lowercase(Locale.ROOT).orEmpty()
            text in STOP_LABELS || description in STOP_LABELS
        } ?: return
        if (!stop.isEnabled) {
            finishCurrent(StopOutcome.SKIPPED, "Android disabled the Stop action for this application")
            return
        }
        if (stop.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            stopClicked = true
            setStep(StopStep.WAITING_FOR_CONFIRMATION)
        }
    }

    private fun inspectConfirmation(root: AccessibilityNodeInfo) {
        val positive = findClickable(root) { node ->
            val id = node.viewIdResourceName.orEmpty()
            val text = node.text?.toString()?.trim()?.lowercase(Locale.ROOT).orEmpty()
            id.endsWith(":id/button1") || id.endsWith(":id/positive_button") || text in CONFIRM_LABELS
        }
        if (positive != null && positive.isEnabled && positive.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            setStep(StopStep.VERIFYING)
            return
        }
        // Some OEMs perform the stop without a confirmation. Only accept it if
        // the original action changed to a disabled control in the same target page.
        if (stopClicked && hasText(root, current?.label.orEmpty())) inspectVerification(root)
    }

    private fun inspectVerification(root: AccessibilityNodeInfo) {
        val target = current ?: return
        if (!hasText(root, target.label) && !hasText(root, target.packageName)) return
        val stop = findClickable(root) { node ->
            node.text?.toString()?.trim()?.lowercase(Locale.ROOT) in STOP_LABELS
        }
        if (stop != null && !stop.isEnabled && stopClicked) {
            finishCurrent(StopOutcome.SUCCESS, "Android disabled the Stop control after the action")
        }
    }

    private fun hasText(root: AccessibilityNodeInfo, text: String): Boolean {
        if (text.isBlank()) return false
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes.any {
            it.text?.toString()?.equals(text, ignoreCase = true) == true ||
                it.contentDescription?.toString()?.equals(text, ignoreCase = true) == true
        }
    }

    private fun findClickable(
        root: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        val nodes = ArrayDeque<AccessibilityNodeInfo>()
        nodes.add(root)
        var visited = 0
        while (nodes.isNotEmpty() && visited++ < 200) {
            val node = nodes.removeFirst()
            if (predicate(node)) {
                var clickable: AccessibilityNodeInfo? = node
                repeat(3) {
                    if (clickable?.isClickable == false) clickable = clickable?.parent
                }
                if (clickable?.isClickable == true) return clickable
            }
            for (index in 0 until node.childCount) node.getChild(index)?.let(nodes::addLast)
        }
        return null
    }

    private fun setStep(value: StopStep) {
        automationEngine.enter(value, android.os.SystemClock.uptimeMillis())
        step = value
        timedStep?.let(handler::removeCallbacks)
        timedStep = null
        if (value != StopStep.IDLE) {
            val timeout = Runnable {
                if (step != value) return@Runnable
                when (value) {
                    StopStep.WAITING_FOR_CONFIRMATION, StopStep.VERIFYING -> finishCurrent(
                        StopOutcome.FAILED, "Android did not confirm that the stop completed"
                    )
                    else -> finishCurrent(StopOutcome.SKIPPED, "Timed out waiting for Android App Info")
                }
            }
            timedStep = timeout
            handler.postDelayed(timeout, 10_000L)
        }
        StopSession.set(StopProgress(value, current, results.size + if (current != null) 1 else 0,
            total, results.toList(), cancelRequested))
    }

    private fun finishCurrent(outcome: StopOutcome, detail: String, cancelled: Boolean = false) {
        current?.let { results.add(StopResult(it, outcome, detail)) }
        setStep(if (cancelled) StopStep.CANCELLED else StopStep.RETURNING)
        // App Info is a system screen opened by us; return to the app for safe handoff.
        performGlobalAction(GLOBAL_ACTION_BACK)
        current = null
        handler.postDelayed({ if (cancelled) finishSession() else next() }, 400L)
    }

    private fun finishSession() {
        queue.clear()
        current = null
        timedStep?.let(handler::removeCallbacks)
        timedStep = null
        setEvents(false)
        StopSession.set(StopProgress(if (cancelRequested) StopStep.CANCELLED else StopStep.COMPLETED,
            null, results.size, total, results.toList(), cancelRequested))
        step = StopStep.IDLE
        automationEngine.reset()
        results = ArrayList()
        total = 0
    }

    private fun setEvents(active: Boolean) {
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = if (active) AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED else 0
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = if (active) AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS else 0
        info.notificationTimeout = if (active) 100L else 0L
        setServiceInfo(info)
    }

    override fun onInterrupt() { cancel() }
    override fun onUnbind(intent: Intent): Boolean {
        if (step != StopStep.IDLE) {
            cancel()
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
        return super.onUnbind(intent)
    }
    override fun onDestroy() {
        if (step != StopStep.IDLE) cancel()
        handler.removeCallbacksAndMessages(null)
        instance = null
        super.onDestroy()
    }

    private fun isTrustedSettings(pkg: String): Boolean = pkg == "com.android.settings" ||
        pkg == "com.samsung.android.settings" || pkg == "com.miui.securitycenter" ||
        pkg == "com.coloros.safecenter" || pkg == "com.oplus.safecenter" ||
        pkg == "com.oneplus.settings" || pkg == "com.vivo.settings" ||
        pkg == "com.motorola.android.settings" || pkg.contains("settings", ignoreCase = true)

    companion object {
        @Volatile var instance: AutoOptimiserAccessibilityService? = null
            private set
        private val STOP_LABELS = setOf("force stop", "stop", "force arrêt", "forcer l'arrêt", "detener", "forzar detención")
        private val CONFIRM_LABELS = setOf("ok", "force stop", "stop", "confirm", "force arrêt", "detener")
    }
}
