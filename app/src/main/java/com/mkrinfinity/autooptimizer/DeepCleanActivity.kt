package com.mkrinfinity.autooptimizer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class DeepCleanActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(0xFF242424.toInt())
        }
        val title = TextView(this).apply {
            text = "Deep Clean"
            textSize = 30f
            setTextColor(0xFFF2F2F2.toInt())
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        root.addView(action("Clear hidden cache", "Open Android storage settings and automate the permitted cache action.") { openUsageSettings() })
        root.addView(action("Force stop", "Open App Info and press Android's Force stop button automatically when possible.") { openAccessibility() })
        root.addView(action("Uninstall", "Use Android's normal uninstall confirmation flow.") { openUninstallHint() })
        setContentView(root)
    }

    private fun action(title: String, desc: String, click: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 24, 0, 24)
            addView(TextView(this@DeepCleanActivity).apply { text = title; textSize = 21f; setTextColor(0xFFF2F2F2.toInt()) })
            addView(TextView(this@DeepCleanActivity).apply { text = desc; textSize = 15f; setTextColor(0xFFB8B8B8.toInt()); setPadding(0, 6, 0, 10) })
            addView(Button(this@DeepCleanActivity).apply { text = "OPEN"; setOnClickListener { click() } })
        }
    }

    private fun openUsageSettings() { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
    private fun openAccessibility() { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    private fun openUninstallHint() { startActivity(Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)) }
}
