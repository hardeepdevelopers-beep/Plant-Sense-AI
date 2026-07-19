# Retrofit Rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson Rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter

# OkHttp Rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Room Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Dagger Hilt Rules
-keep class dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Keep DTOs and Response Schema Models for Gson Serialization
-keep class com.plantsense.ai.data.remote.** { *; }
-keep class com.plantsense.ai.domain.model.** { *; }
-keep class com.plantsense.ai.presentation.navigation.AppRoute** { *; }

# Keep domain entity mapping methods
-keepclassmembers class * {
    *** toDomain(...);
    *** fromDomain(...);
}
