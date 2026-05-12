package com.sololedger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.room.Room
import com.sololedger.data.AppDatabase
import com.sololedger.data.PrefStore
import com.sololedger.data.Repo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "solo_ledger.db"
        ).fallbackToDestructiveMigration().build()

        val repo = Repo(db.entryDao(), PrefStore(applicationContext))

        setContent {
            SoloApp(repository = repo)
        }
    }
}