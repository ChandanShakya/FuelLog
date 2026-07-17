# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

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
-keep class * extends dagger.hilt.android.internal.modules.ApplicationContextModule { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.chandanshakya.fuellog.**$$serializer { *; }
-keepclassmembers class com.chandanshakya.fuellog.** {
    *** Companion;
}
-keepclasseswithmembers class com.chandanshakya.fuellog.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# App models
-keep class com.chandanshakya.fuellog.data.model.** { *; }
