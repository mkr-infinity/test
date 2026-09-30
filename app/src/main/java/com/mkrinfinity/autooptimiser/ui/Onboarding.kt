package com.mkrinfinity.autooptimiser.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.mkrinfinity.autooptimiser.AutoOptimiserViewModel
import com.mkrinfinity.autooptimiser.R

/** Optional permissions are checked again when returning from Android Settings. */
@Composable
fun OnboardingFlow(vm: AutoOptimiserViewModel) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableIntStateOf(0) }
    var accessibility by remember { mutableStateOf(false) }
    var storageGranted by remember { mutableStateOf(vm.selectedTreeUri != null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { vm.saveTreeUri(it); storageGranted = vm.selectedTreeUri != null }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        accessibility = AutoOptimiserViewModel.isAccessibilityEnabled(context)
        storageGranted = vm.selectedTreeUri != null
    }
    val titles = listOf("Your device. Your decisions.", "Automation you control", "Only files you choose", "No notification access needed", "Lightweight by design", "Ready when you are")
    Scaffold { insets ->
        Column(Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Image(painterResource(R.drawable.ic_brand), "Auto Optimiser logo", Modifier.size(64.dp))
                Text("${page + 1} / ${titles.size}", style = MaterialTheme.typography.labelLarge)
            }
            LinearProgressIndicator(progress = { (page + 1f) / titles.size }, modifier = Modifier.fillMaxWidth())
            Text(titles[page], style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            when (page) {
                0 -> {
                    Text("A local utility for reviewing apps, inspecting device information, and cleaning files you select.")
                    Text("Analyse · understand · choose · verify", color = MaterialTheme.colorScheme.primary)
                    Text("No account. No speed scores. No silent deletion.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                1 -> {
                    Explanation("Why this access?", "When you explicitly start a stop queue, Auto Optimiser navigates Android App Info and uses the supported Force stop controls for your selected apps.")
                    Explanation("What it does not do", "It does not read messages, passwords or other personal app screens. Window events are disabled when the queue is idle. You can cancel from the progress window.")
                    Text(if (accessibility) "Accessibility enabled" else "Optional · required only for stop automation")
                    OutlinedButton(vm::openAccessibilitySettings, Modifier.fillMaxWidth()) { Text("Open Accessibility settings") }
                }
                2 -> {
                    Explanation("Why folder access?", "Android's folder picker lets you choose exactly where scanning and file deletion are allowed. Select a subfolder if Android disallows the Downloads root.")
                    Explanation("What it does not do", "Auto Optimiser cannot scan other apps' private data or delete their hidden caches. Deleting your files always requires a separate confirmation.")
                    OutlinedButton({ picker.launch(vm.selectedTreeUri) }, Modifier.fillMaxWidth()) { Text(if (storageGranted) "Change folder" else "Choose a folder") }
                    Text(if (storageGranted) "Folder access granted" else "You can choose a folder later in Deep clean.")
                }
                3 -> {
                    Text("This version does not read notifications or publish continuous memory/battery notifications. Notification access is not requested.")
                    Text("Android may show system UI during actions you start. This is separate from notification-listener access.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                4 -> {
                    Text("Optional daily or weekly WorkManager checks record a small device snapshot. Android decides the exact execution time and may defer work when the battery is low.")
                    Text("No background app stopping, file deletion, screen-off trigger, memory-threshold timer or Home-button interception. Enable scheduled analysis later in Settings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                5 -> {
                    Checklist("Accessibility", if (accessibility) "Enabled" else "Not enabled · optional")
                    Checklist("Storage", if (storageGranted) "Selected folder only" else "Not granted · optional")
                    Checklist("Notifications", "Not required")
                    Checklist("Scheduled analysis", if (vm.automaticOptimisation) "Enabled" else "Off")
                }
            }
            Button(onClick = { if (page == titles.lastIndex) vm.completeOnboarding() else page++ }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text(if (page == titles.lastIndex) "Start using Auto Optimiser" else if (page in 1..2) "Continue with current access" else "Continue")
            }
            if (page > 0) TextButton(onClick = { page-- }) { Text("Back") }
        }
    }
}

@Composable
private fun Explanation(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Checklist(title: String, value: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}
