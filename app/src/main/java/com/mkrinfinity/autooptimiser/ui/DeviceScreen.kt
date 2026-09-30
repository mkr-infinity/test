package com.mkrinfinity.autooptimiser.ui

import android.os.Build
import android.os.Process
import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mkrinfinity.autooptimiser.AutoOptimiserViewModel
import com.mkrinfinity.autooptimiser.data.formatTimestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DeviceScreen(vm: AutoOptimiserViewModel) {
    val status by vm.device.collectAsStateWithLifecycle()
    var cpuPercent by remember { mutableStateOf<Double?>(null) }
    var sampling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { job?.cancel(); sampling = false }
    DisposableEffect(Unit) { onDispose { job?.cancel() } }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Text("Device information", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("On-demand measurements, not a performance score.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            DeviceSection("Hardware & Android") {
                DeviceFact("Manufacturer", Build.MANUFACTURER)
                DeviceFact("Model", Build.MODEL)
                DeviceFact("Android", "${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
                DeviceFact("Security patch", Build.VERSION.SECURITY_PATCH.ifBlank { "Not exposed" })
                DeviceFact("Supported architecture", Build.SUPPORTED_ABIS.joinToString())
                DeviceFact("Available CPU cores", Runtime.getRuntime().availableProcessors().toString())
            }
        }
        item {
            DeviceSection("Memory") {
                status?.let { snapshot ->
                    DeviceFact("Available", bytes(snapshot.memoryAvailableBytes))
                    DeviceFact("Used", bytes(snapshot.memoryUsedBytes))
                    DeviceFact("Total", bytes(snapshot.memoryTotalBytes))
                    Text("Measured ${formatTimestamp(snapshot.measuredAtMillis)}", style = MaterialTheme.typography.bodySmall)
                } ?: Text("No snapshot yet. Use Refresh below.")
                Text("Android intentionally uses available memory for caching and performance. Free RAM is not itself a performance score.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            DeviceSection("Storage volume") {
                status?.let { snapshot ->
                    DeviceFact("Available", bytes(snapshot.storageAvailableBytes))
                    DeviceFact("Used", bytes(snapshot.storageUsedBytes))
                    DeviceFact("Total", bytes(snapshot.storageTotalBytes))
                } ?: Text("Not measured")
                Text("Internal data volume. Not a list of removable files.", style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            DeviceSection("CPU information") {
                Text("Android does not expose reliable device-wide or other-app CPU usage to this app.")
                cpuPercent?.let { Text("Auto Optimiser only: ${String.format(Locale.getDefault(), "%.1f", it)}% of one core over the last sample.") }
                Text("One bounded 750 ms observation, including this app's own work. No background CPU polling.", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(enabled = !sampling, onClick = {
                    job = scope.launch {
                        sampling = true
                        try {
                            val cpu = Process.getElapsedCpuTime()
                            val wall = SystemClock.elapsedRealtime()
                            delay(750)
                            val elapsed = SystemClock.elapsedRealtime() - wall
                            cpuPercent = (Process.getElapsedCpuTime() - cpu).coerceAtLeast(0) * 100.0 / elapsed.coerceAtLeast(1)
                        } finally { sampling = false }
                    }
                }) { Text(if (sampling) "Measuring this app…" else "Sample Auto Optimiser CPU") }
            }
        }
        item { OutlinedButton(vm::refreshDevice, Modifier.fillMaxWidth()) { Text("Refresh device snapshot") } }
    }
}

@Composable
fun DeviceSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        HorizontalDivider()
        content()
    }
}

@Composable
fun DeviceFact(label: String, value: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

private fun bytes(value: Long): String {
    if (value < 1024) return "$value B"
    var count = value.toDouble()
    var unit = -1
    val units = listOf("KiB", "MiB", "GiB", "TiB")
    do { count /= 1024; unit++ } while (count >= 1024 && unit < units.lastIndex)
    return String.format(Locale.getDefault(), "%.1f %s", count, units[unit])
}
