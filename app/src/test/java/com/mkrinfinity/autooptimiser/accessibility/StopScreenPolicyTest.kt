package com.mkrinfinity.autooptimiser.accessibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Mock immutable node facts; these tests need no Android runtime or live Settings UI. */
class StopScreenPolicyTest {
    private val owner = "com.android.settings"
    private val target = StopItem("example.target", "Target")
    private fun appInfo(enabled: Boolean = true) = StopScreenFacts(owner, 12, listOf(
        StopNodeFacts(text = "App info"),
        StopNodeFacts(text = target.packageName),
        StopNodeFacts(text = target.label),
        StopNodeFacts(id = "$owner:id/force_stop_button", text = "Force stop", clickable = true, enabled = enabled)
    ))
    private fun dialog(message: String = "If you force stop an app, it may misbehave.") =
        StopScreenFacts(owner, 13, listOf(
            StopNodeFacts(id = "android:id/parentPanel"),
            StopNodeFacts(id = "android:id/message", text = message, parent = 0),
            StopNodeFacts(id = "android:id/button1", text = "OK", parent = 0, clickable = true),
            StopNodeFacts(id = "android:id/button2", text = "Cancel", parent = 0, clickable = true)
        ))
    private fun confirm(screen: StopScreenFacts, identified: Boolean = true, clicked: Boolean = true) =
        StopScreenPolicy.confirmationButton(screen, owner, 12, identified, clicked)

    @Test fun exactPackageAndLabelAndPageIdentityAreAllRequired() {
        assertEquals(3, StopScreenPolicy.stopButton(appInfo(), target, owner))
        assertNull(StopScreenPolicy.stopButton(appInfo().copy(nodes = appInfo().nodes.filterNot {
            it.text == target.packageName
        }), target, owner))
        assertNull(StopScreenPolicy.stopButton(appInfo(), target.copy(packageName = "another.target"), owner))
        assertNull(StopScreenPolicy.stopButton(appInfo(), target.copy(label = "Other"), owner))
        assertNull(StopScreenPolicy.stopButton(appInfo().copy(nodes = appInfo().nodes.drop(1)), target, owner))
    }

    @Test fun packageSubstringsPartialTreesAndHiddenIdentityAreNeverTrusted() {
        assertNull(StopScreenPolicy.stopButton(appInfo().copy(owner = "evil.com.android.settings"), target, owner))
        assertNull(StopScreenPolicy.stopButton(appInfo().copy(complete = false), target, owner))
        val hidden = appInfo().nodes.map { if (it.text == target.packageName) it.copy(visible = false) else it }
        assertNull(StopScreenPolicy.stopButton(appInfo().copy(nodes = hidden), target, owner))
    }

    @Test fun genericStopAmbiguousButtonsAndUnlabelledAncestorsAreRejected() {
        val nodes = appInfo().nodes
        assertNull(StopScreenPolicy.stopButton(appInfo().copy(nodes = nodes.dropLast(1) +
            nodes.last().copy(text = "Stop")), target, owner))
        assertNull(StopScreenPolicy.stopButton(appInfo().copy(nodes = nodes + nodes.last()), target, owner))
        assertNull(StopScreenPolicy.stopButton(appInfo().copy(nodes = nodes.dropLast(1) +
            nodes.last().copy(id = "android:id/button1")), target, owner))
    }

    @Test fun labelledChildMayUseOnlyAnExplicitForceStopControl() {
        val nodes = appInfo().nodes.dropLast(1) + listOf(
            StopNodeFacts(id = "$owner:id/force_stop_button", clickable = true),
            StopNodeFacts(text = "Force stop", parent = 3)
        )
        assertEquals(3, StopScreenPolicy.stopButton(appInfo().copy(nodes = nodes), target, owner))
        assertNull(StopScreenPolicy.stopButton(appInfo().copy(nodes = nodes.map {
            if (it.clickable) it.copy(id = "unrelated:button") else it
        }), target, owner))
    }

    @Test fun standardForceStopDialogNeedsSourceContinuityAndAnIssuedClick() {
        assertEquals(2, confirm(dialog()))
        assertNull(confirm(dialog(), identified = false))
        assertNull(confirm(dialog(), clicked = false))
        assertNull(confirm(dialog().copy(windowId = 12)))
        assertNull(confirm(dialog().copy(owner = "com.android.packageinstaller")))
        assertNull(confirm(dialog().copy(complete = false)))
    }

    @Test fun genericOkayOrDestructiveUnrelatedDialogDoesNotAuthorizeConfirmation() {
        assertNull(confirm(dialog("Delete this application's data?")))
        assertNull(confirm(dialog("Uninstall Target?")))
        assertNull(confirm(dialog().copy(nodes = dialog().nodes.filterNot { it.id == "android:id/message" })))
        assertNull(confirm(dialog().copy(nodes = dialog().nodes.map {
            if (it.id == "android:id/parentPanel") it.copy(id = "arbitrary-root") else it
        })))
        assertNull(confirm(dialog().copy(nodes = dialog().nodes + dialog().nodes[2])))
        assertNull(confirm(dialog().copy(nodes = dialog().nodes.map {
            if (it.id == "android:id/button2") it.copy(text = "Delete") else it
        })))
    }

    @Test fun confirmationRequiresVisibleEnabledControlsAndSharedDialogStructure() {
        assertNull(confirm(dialog().copy(nodes = dialog().nodes.map {
            if (it.id == "android:id/button1") it.copy(enabled = false) else it
        })))
        assertNull(confirm(dialog().copy(nodes = dialog().nodes.map {
            if (it.id == "android:id/message") it.copy(visible = false) else it
        })))
        assertNull(confirm(dialog().copy(nodes = dialog().nodes.map {
            if (it.id == "android:id/button2") it.copy(parent = -1) else it
        })))
    }

    @Test fun successRequiresAFlagTransitionAndDisabledControlOnTheSameTargetWindow() {
        fun verified(screen: StopScreenFacts = appInfo(false), before: Boolean? = false,
                     after: Boolean? = true, clicked: Boolean = true) =
            StopScreenPolicy.verified(screen, target, owner, 12, clicked, before, after)
        assertTrue(verified())
        assertFalse(verified(appInfo(true)))
        assertFalse(verified(before = true))
        assertFalse(verified(before = null))
        assertFalse(verified(after = false))
        assertFalse(verified(after = null))
        assertFalse(verified(clicked = false))
        assertFalse(verified(appInfo(false).copy(windowId = 99)))
        assertFalse(verified(appInfo(false).copy(nodes = appInfo(false).nodes.drop(2))))
    }
}
