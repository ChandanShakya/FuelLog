# SQLite - keep database helpers
-keep class * extends android.database.sqlite.SQLiteOpenHelper { *; }

# Kotlin - keep metadata for serialization only
-keepattributes *Annotation*
