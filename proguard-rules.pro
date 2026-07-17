# Android default rules
-keep class androidx.** { *; }
-keep class android.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.Database { *; }
-keep class * extends androidx.room.Entity { *; }
-keep class * extends androidx.room.Dao { *; }

# Hilt
-keep class com.google.dagger.** { *; }
-keep class * implements com.google.dagger.hilt.android.internal.modules.ApplicationContextModule { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-keep class kotlin.** { *; }
