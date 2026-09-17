# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { <fields>; }

# Kotlin metadata not needed at runtime (no reflection/serialization)
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Coroutines debug agent not used
-dontwarn kotlinx.coroutines.debug.**
