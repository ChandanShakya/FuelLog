# Room - keep only DAOs and entities
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { <fields>; }

# Kotlin - strip builtins metadata (not used - no reflection/serialization)
-dontwarn kotlin.**
