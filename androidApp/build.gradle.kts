import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.sharedUI)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.android)

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    // CameraX + ML Kit OCR
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.mlkit.text.recognition)
}

// Resolution order: -PapiBaseUrl=... (CI/local override) > ALPHAKIDS_API_BASE_URL
// env var > the current production default. Never edit ApiConstants.kt by
// hand to point at a different backend again — override it here instead.
val apiBaseUrl: String = (project.findProperty("apiBaseUrl") as String?)
    ?: System.getenv("ALPHAKIDS_API_BASE_URL")
    ?: "https://alphakids-back-production.up.railway.app"

// Release signing — see keystore.properties.example for how to generate the
// keystore and fill this in. keystore.properties is gitignored on purpose;
// this file must never contain real credentials.
val keystorePropertiesFile = file("keystore.properties")
val hasReleaseSigning = keystorePropertiesFile.exists()
val keystoreProperties = Properties().apply {
    if (hasReleaseSigning) load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "org.alphakids.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "org.alphakids.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            // No signingConfig assigned when keystore.properties is missing —
            // the doFirst check below fails assembleRelease/bundleRelease
            // with a clear message instead of silently producing an
            // unsigned artifact that looks like a normal build succeeded.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Fail loudly and early if a release build is attempted without real signing
// credentials, instead of AGP silently emitting an unsigned APK/AAB that
// can't be installed or uploaded to Play Store — see keystore.properties.example.
// Checked via taskGraph.whenReady (runs once the graph is finalized, before
// any task executes) instead of a per-task doFirst — a doFirst closure here
// captures a Gradle script object reference that the configuration cache
// can't serialize, breaking every other build in this project that relies
// on it (assembleDebug included).
gradle.taskGraph.whenReady {
    val releaseTaskRequested = allTasks.any { it.name == "assembleRelease" || it.name == "bundleRelease" }
    if (releaseTaskRequested) {
        check(hasReleaseSigning) {
            "Missing androidApp/keystore.properties. Copy keystore.properties.example, " +
                "generate a release keystore (instructions inside that file), and fill in " +
                "the real values before building a release artifact."
        }
    }
}