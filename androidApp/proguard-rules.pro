# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the default proguard-android-optimize.txt from the Android SDK.

# ── kotlinx.serialization ──
# Modern kotlinx.serialization ships its own consumer-rules.pro, but keeping
# an explicit rule here as a safety net: losing a DTO's generated
# $serializer companion in release-only R8 stripping is a silent runtime
# crash (missing serializer), not a compile error, so this is cheap insurance.
-keepattributes *Annotation*, InnerClasses
-keepclasseswithmembers class org.alphakids.app.data.remote.dto.**$$serializer {
    *** INSTANCE;
}
-keepclassmembers class org.alphakids.app.data.remote.dto.** {
    *** Companion;
}
-keepclasseswithmembers class org.alphakids.app.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Ktor / kotlinx.coroutines ──
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**

# ── Koin ──
# Koin resolves dependencies by type at runtime; module DSL itself is plain
# Kotlin (no reflection on our classes), so no extra -keep needed beyond
# what Koin's own consumer rules provide. Verified by a real signed-release
# smoke test per the project's mobile-backend-integration-health skill —
# add rules here if that test surfaces a missing-class crash.
