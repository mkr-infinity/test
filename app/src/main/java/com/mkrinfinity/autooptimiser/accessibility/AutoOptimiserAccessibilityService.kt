package com.mkrinfinity.autooptimiser.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.mkrinfinity.autooptimiser.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable

// Kept source-compatible with MainActivity and its progress/result presentation.
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
    fun start(items: List<StopItem>): Boolean = AutoOptimiserAccessibilityService.instance?.start(items) ?: false
    fun cancel() { AutoOptimiserAccessibilityService.instance?.cancel() }
}

/**
 * A foreground, explicitly requested, bounded queue. All mutation and Android actions
 * run on the main looper. No global Back, background trigger, or retained node exists.
 */
class AutoOptimiserAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val engine = AppStopAutomationEngine()
    private val guard by lazy { StopTargetGuard(this) }
    private var queue = ArrayDeque<StopItem>()
    private var current: StopItem? = null
    private var results = arrayListOf<StopResult>()
    private var total = 0
    private var active = false
    private var cancelled = false
    private var generation = 0L
    @Volatile private var sessionId = 0L
    private var stepEventTime = 0L
    private var sessionDeadline = 0L
    private var itemDeadline = 0L
    private var settingsPackage = ""
    private var sourceWindowId = -1
    private var stopClicked = false
    private var stoppedBefore: Boolean? = null
    private var tick: Runnable? = null
    private var overlay: LinearLayout? = null
    private var overlayText: TextView? = null

    override fun onServiceConnected() {
        if (active) endSession(true, "Accessibility service reconnected")
        instance = this
        engine.reset()
        setEvents(false)
    }

    fun start(items: List<StopItem>): Boolean {
        // Existing UI callers are on main. Do not post a deferred start that could outlive a gesture.
        if (Looper.myLooper() != Looper.getMainLooper() || active || items.isEmpty() ||
            items.size > MAX_ITEMS || !ownWindowIsActive()) return false
        queue = ArrayDeque(items.distinctBy { it.packageName })
        total = queue.size
        results = arrayListOf()
        cancelled = false
        engine.reset()
        generation++
        sessionId++
        active = true
        sessionDeadline = SystemClock.elapsedRealtime() + SESSION_TIMEOUT
        // Never open Settings unless the user has a working, always-visible cancel control.
        if (!showOverlay()) {
            active = false
            queue.clear()
            removeOverlay()
            return false
        }
        next()
        return true
    }

    fun cancel() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            val expectedSession = sessionId
            handler.post { if (sessionId == expectedSession && active) cancel() }
            return
        }
        if (active) endSession(true, "Cancelled; any already-issued Android action cannot be undone")
    }

    private fun next() {
        if (!active) return
        if (SystemClock.elapsedRealtime() >= sessionDeadline) {
            endSession(true, "Session time limit reached")
            return
        }
        while (queue.isNotEmpty()) {
            val queued = queue.removeFirst()
            val rejection = guard.rejection(queued.packageName)
            val resolved = if (rejection == null) guard.item(queued.packageName) else null
            if (rejection != null || resolved == null) {
                results.add(StopResult(queued, StopOutcome.SKIPPED, rejection ?: "Application no longer available"))
                continue
            }
            current = resolved
            break
        }
        val target = current ?: run { endSession(false); return }
        generation++
        stopClicked = false
        sourceWindowId = -1
        stoppedBefore = guard.isStopped(target.packageName)
        itemDeadline = SystemClock.elapsedRealtime() + ITEM_TIMEOUT
        if (!transition(StopStep.OPENING_APP_INFO)) {
            endSession(true, "Invalid automation state")
            return
        }
        if (stoppedBefore != false) {
            finishCurrent(StopOutcome.SKIPPED, if (stoppedBefore == true) "Application is already stopped" else "Cannot read Android stopped state")
            return
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${target.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val component = guard.settingsComponent(intent)
        if (component == null) {
            finishCurrent(StopOutcome.SKIPPED, "No supported system App Info activity")
            return
        }
        settingsPackage = component.packageName
        intent.component = component
        if (expireIfNeeded()) return
        if (!setEvents(true)) {
            endSession(true, "Android could not enable the restricted Settings subscription")
            return
        }
        if (!transition(StopStep.WAITING_FOR_APP_INFO)) {
            endSession(true, "Invalid automation state")
            return
        }
        if (runCatching { startActivity(intent) }.isFailure) {
            finishCurrent(StopOutcome.FAILED, "Android could not open App Info")
            return
        }
        scheduleTick()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!active || current == null || event.packageName?.toString() != settingsPackage ||
            event.eventTime < stepEventTime) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return
        inspect()
    }

    @Suppress("DEPRECATION")
    private fun inspect() {
        if (!active || current == null || engine.state == StopStep.RETURNING) return
        if (expireIfNeeded()) return
        val target = current ?: return
        val rejection = guard.rejection(target.packageName)
        if (rejection != null) {
            finishCurrent(if (stopClicked) StopOutcome.FAILED else StopOutcome.SKIPPED, rejection)
            return
        }
        val root = runCatching { rootInActiveWindow }.getOrNull() ?: return
        // Inspect only the owner field of unrelated roots, never their contents.
        if (root.packageName?.toString() != settingsPackage) {
            root.recycle()
            if (engine.state != StopStep.WAITING_FOR_APP_INFO ||
                SystemClock.elapsedRealtime() - engine.enteredAtMillis >= 1_500L) {
                endSession(true, "The user left the expected Settings window")
            }
            return
        }
        NodeSnapshot.capture(root, settingsPackage)?.use { snapshot ->
            val screen = snapshot.facts
            if (screen.owner != settingsPackage || !screen.complete) return
            when (engine.state) {
                StopStep.WAITING_FOR_APP_INFO, StopStep.FINDING_STOP_BUTTON -> {
                    if (!StopScreenPolicy.identifies(screen, target, settingsPackage)) return
                    if (engine.state == StopStep.WAITING_FOR_APP_INFO && !transition(StopStep.FINDING_STOP_BUTTON)) return
                    val button = StopScreenPolicy.stopButton(screen, target, settingsPackage) ?: return
                    if (!screen.nodes[button].enabled) {
                        finishCurrent(StopOutcome.SKIPPED, "Android disabled Force stop; no action was taken")
                        return
                    }
                    // Recheck immediately before each destructive click, not only when queued.
                    if (!mayClick(target)) return
                    if (guard.isStopped(target.packageName) != false) {
                        finishCurrent(StopOutcome.SKIPPED, "Stopped state changed before the action")
                        return
                    }
                    sourceWindowId = screen.windowId
                    // Advance first so synchronous/reentrant notifications cannot click twice.
                    if (!transition(StopStep.WAITING_FOR_CONFIRMATION)) return
                    stopClicked = snapshot.click(button, clickDeadline())
                    if (!stopClicked) finishCurrent(StopOutcome.FAILED, "Android rejected the Force stop action")
                }
                StopStep.WAITING_FOR_CONFIRMATION -> {
                    // A no-dialog OEM path still requires both a flag transition and UI evidence.
                    if (isVerified(screen, target)) {
                        if (transition(StopStep.VERIFYING)) verifiedResult()
                        return
                    }
                    val positive = StopScreenPolicy.confirmationButton(screen, settingsPackage,
                        sourceWindowId, sourceStillIdentified(target), stopClicked) ?: return
                    if (!mayClick(target)) return
                    if (!transition(StopStep.VERIFYING)) return
                    if (!snapshot.click(positive, clickDeadline())) finishCurrent(StopOutcome.FAILED, "Android rejected stop confirmation")
                }
                StopStep.VERIFYING -> if (isVerified(screen, target)) verifiedResult()
                else -> Unit
            }
        }
    }

    private fun clickDeadline() = minOf(engine.deadlineMillis, itemDeadline, sessionDeadline)

    private fun mayClick(target: StopItem): Boolean {
        if (expireIfNeeded()) return false
        val rejection = guard.rejection(target.packageName) ?: return true
        finishCurrent(if (stopClicked) StopOutcome.FAILED else StopOutcome.SKIPPED, rejection)
        return false
    }

    private fun isVerified(screen: StopScreenFacts, target: StopItem) = StopScreenPolicy.verified(
        screen, target, settingsPackage, sourceWindowId, stopClicked, stoppedBefore,
        guard.isStopped(target.packageName)
    )

    private fun verifiedResult() {
        if (!expireIfNeeded()) finishCurrent(StopOutcome.SUCCESS,
            "Android FLAG_STOPPED changed to true and Force stop became disabled on the identified App Info page")
    }

    @Suppress("DEPRECATION")
    private fun sourceStillIdentified(target: StopItem): Boolean {
        val available = runCatching { windows }.getOrNull() ?: return false
        try {
            val source = available.singleOrNull { it.id == sourceWindowId } ?: return false
            val root = source.root ?: return false
            return NodeSnapshot.capture(root, settingsPackage)?.use {
                StopScreenPolicy.identifies(it.facts, target, settingsPackage) &&
                    StopScreenPolicy.stopButton(it.facts, target, settingsPackage) != null
            } ?: false
        } catch (_: Exception) {
            return false
        } finally {
            available.forEach { it.recycle() }
        }
    }

    private fun transition(next: StopStep): Boolean {
        // Elapsed time includes deep sleep; event timestamps use a separate uptime clock.
        if (!engine.enter(next, SystemClock.elapsedRealtime())) return false
        stepEventTime = SystemClock.uptimeMillis()
        publish(next)
        return true
    }

    private fun publish(step: StopStep) {
        StopSession.set(StopProgress(step, current, results.size + if (current == null) 0 else 1,
            total, results.toList(), cancelled))
        overlayText?.text = "${results.size + if (current == null) 0 else 1}/$total · ${current?.label ?: "Finishing"}"
    }

    private fun expireIfNeeded(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now >= sessionDeadline) {
            endSession(true, "Session time limit reached; an issued stop may already have taken effect")
            return true
        }
        if (now >= itemDeadline || engine.timedOut(now)) {
            finishCurrent(if (stopClicked) StopOutcome.FAILED else StopOutcome.SKIPPED,
                if (stopClicked) "Timed out: stop was attempted but could not be verified"
                else "Timed out: App Info identity or supported Force stop controls were not available")
            return true
        }
        return false
    }

    private fun scheduleTick() {
        tick?.let(handler::removeCallbacks)
        val expected = generation
        tick = Runnable {
            if (!active || generation != expected || current == null) return@Runnable
            inspect()
            if (active && generation == expected && current != null) scheduleTick()
        }.also { handler.postDelayed(it, 250L) }
    }

    private fun finishCurrent(outcome: StopOutcome, detail: String) {
        val target = current ?: return // Idempotent, including delayed callbacks.
        current = null
        generation++
        tick?.let(handler::removeCallbacks)
        tick = null
        setEvents(false)
        results.add(StopResult(target, outcome, detail))
        if (!transition(StopStep.RETURNING)) {
            endSession(true, "Invalid automation state")
            return
        }
        val expected = generation
        // No blind Back. Each next item is launched explicitly; final handoff opens MainActivity.
        tick = Runnable { if (active && generation == expected) next() }
            .also { handler.postDelayed(it, 250L) }
    }

    private fun endSession(wasCancelled: Boolean, reason: String = "") {
        if (!active) return
        active = false // Invalidate all work before cleanup or navigation.
        cancelled = wasCancelled
        generation++
        sessionId++
        handler.removeCallbacksAndMessages(null)
        tick = null
        current?.let {
            results.add(StopResult(it, if (stopClicked) StopOutcome.FAILED else StopOutcome.SKIPPED,
                reason.ifBlank { "Stop was not verified" }))
        }
        current = null
        queue.clear()
        setEvents(false)
        removeOverlay()
        val terminal = if (wasCancelled) StopStep.CANCELLED else StopStep.COMPLETED
        publish(terminal)
        engine.reset()
        // Explicit, one-time return avoids escaping the intended screen with double Back.
        runCatching {
            startActivity(Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }.onFailure {
            val previous = StopSession.progress.value
            StopSession.set(previous.copy(results = previous.results.mapIndexed { index, result ->
                if (index == previous.results.lastIndex) result.copy(detail = result.detail + "; return to Auto Optimiser manually")
                else result
            }))
        }
    }

    private fun setEvents(enabled: Boolean): Boolean = runCatching {
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = if (enabled) AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED else 0
        info.packageNames = if (enabled) arrayOf(settingsPackage) else emptyArray()
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = if (enabled) AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
            AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS else 0
        info.notificationTimeout = if (enabled) 100L else 0L
        serviceInfo = info
        true
    }.getOrDefault(false)

    @Suppress("DEPRECATION")
    private fun ownWindowIsActive(): Boolean {
        val root = runCatching { rootInActiveWindow }.getOrNull() ?: return false
        return try { root.packageName?.toString() == packageName } finally { root.recycle() }
    }

    private fun showOverlay(): Boolean = runCatching {
        val manager = requireNotNull(getSystemService(WindowManager::class.java))
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 8, 16, 8)
            setBackgroundColor(Color.rgb(32, 33, 36))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        overlay = layout // Also removable if addView partially fails.
        overlayText = TextView(this).apply {
            text = "Preparing app stop"
            setTextColor(Color.WHITE)
            maxLines = 2
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        layout.addView(overlayText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        layout.addView(Button(this).apply {
            text = "Cancel"
            contentDescription = "Cancel app stop and return to Auto Optimiser"
            setOnClickListener { cancel() }
        })
        val width = (320 * resources.displayMetrics.density).toInt()
            .coerceAtMost(resources.displayMetrics.widthPixels)
        manager.addView(layout, WindowManager.LayoutParams(width, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END })
        true
    }.getOrDefault(false)

    private fun removeOverlay() {
        overlay?.let { view -> runCatching { getSystemService(WindowManager::class.java)?.removeViewImmediate(view) } }
        overlay = null
        overlayText = null
    }

    override fun onInterrupt() { cancel() }
    override fun onUnbind(intent: Intent): Boolean {
        cancel()
        if (instance === this) instance = null
        return super.onUnbind(intent)
    }
    override fun onDestroy() {
        cancel()
        handler.removeCallbacksAndMessages(null)
        removeOverlay()
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile var instance: AutoOptimiserAccessibilityService? = null
            private set
        private const val MAX_ITEMS = 20
        private const val ITEM_TIMEOUT = 30_000L
        private const val SESSION_TIMEOUT = 120_000L
    }
}

/** Owns every acquired node, including the root. No references survive the inspection. */
@Suppress("DEPRECATION")
private class NodeSnapshot private constructor(
    private val nodes: List<AccessibilityNodeInfo>,
    private val childCounts: List<Int>,
    val facts: StopScreenFacts
) : Closeable {
    fun click(index: Int, deadline: Long): Boolean = runCatching {
        // Protection queries may take time. Refuse a changed/stale tree, including when
        // a labelled child changed but its clickable parent's text was always empty.
        val unchanged = nodes.indices.all { position ->
            val node = nodes[position]
            val before = facts.nodes[position]
            node.refresh() && node.windowId == facts.windowId &&
                node.packageName?.toString() == facts.owner && node.childCount == childCounts[position] &&
                node.isVisibleToUser == before.visible && node.isEnabled == before.enabled &&
                node.isClickable == before.clickable && node.viewIdResourceName.orEmpty() == before.id &&
                node.text?.toString().orEmpty() == before.text
        }
        if (!unchanged) return@runCatching false
        val node = nodes[index]
        val window = node.window ?: return@runCatching false
        val isActive = try {
            window.isActive && window.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION
        } finally { window.recycle() }
        isActive && node.isVisibleToUser && node.isEnabled && node.isClickable &&
            SystemClock.elapsedRealtime() < deadline && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }.getOrDefault(false)

    override fun close() { nodes.forEach { it.recycle() } }

    companion object {
        fun capture(root: AccessibilityNodeInfo, expectedOwner: String): NodeSnapshot? {
            val nodes = arrayListOf(root)
            val parents = arrayListOf(-1)
            val facts = arrayListOf<StopNodeFacts>()
            val childCounts = arrayListOf<Int>()
            try {
                val owner = root.packageName?.toString().orEmpty()
                if (owner != expectedOwner) {
                    root.recycle()
                    return null
                }
                val windowId = root.windowId
                var complete = true
                var index = 0
                while (index < nodes.size) {
                    val node = nodes[index]
                    if (node.packageName?.toString() != owner || node.windowId != windowId) complete = false
                    facts.add(StopNodeFacts(node.viewIdResourceName.orEmpty(), node.text?.toString().orEmpty(),
                        parents[index], node.isClickable, node.isEnabled, node.isVisibleToUser))
                    val count = node.childCount
                    childCounts.add(count)
                    if (nodes.size + count > 300) {
                        complete = false // A partial tree must never authorize an action.
                    } else {
                        for (childIndex in 0 until count) {
                            val child = node.getChild(childIndex)
                            if (child == null) complete = false else {
                                nodes.add(child)
                                parents.add(index)
                            }
                        }
                    }
                    index++
                }
                return NodeSnapshot(nodes, childCounts, StopScreenFacts(owner, windowId, facts, complete))
            } catch (_: Exception) {
                nodes.forEach { it.recycle() }
                return null
            }
        }
    }
}
