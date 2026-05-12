package com.sololedger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sololedger.ui.theme.SoloLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SoloLedgerTheme { SoloApp() } }
    }
}