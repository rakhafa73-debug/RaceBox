# Add project specific ProGuard rules here.
# Room uses generated classes, keep them.
-keep class * extends androidx.room.RoomDatabase

# retrofit / gson reflection
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.racebox.app.data.sync.** { *; }