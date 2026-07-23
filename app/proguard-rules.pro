# Room - keep only DAOs and entities
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { <fields>; }

# Hilt - keep generated component and entry points
-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Kotlin - keep metadata for serialization only
-keepattributes *Annotation*
