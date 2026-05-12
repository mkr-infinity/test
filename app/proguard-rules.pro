# ProGuard rules for Solo Ledger

# Keep Room entities
-keep class com.sololedger.data.** { *; }

# Keep Kotlin metadata
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }

# Keep Compose
-dontwarn androidx.compose.**

# Keep DataStore
-keep class androidx.datastore.** { *; }