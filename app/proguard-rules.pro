# Add project specific ProGuard rules here.
# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.macbridge.android.data.models.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
