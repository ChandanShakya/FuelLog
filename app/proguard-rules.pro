# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.Dao { *; }
-keep class * extends androidx.room.Entity { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# App models
-keep class com.chandanshakya.fuellog.data.model.** { *; }
