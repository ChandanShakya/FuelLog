plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.chandanshakya.fuellog"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chandanshakya.fuellog"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }

        resourceConfigurations += listOf("en")
    }

    signingConfigs {
        // Credentials come from environment / CI secrets — never hardcode passwords.
        // Required when building release: FUELLOG_STORE_FILE, FUELLOG_STORE_PASSWORD,
        // FUELLOG_KEY_ALIAS, FUELLOG_KEY_PASSWORD
        val storeFilePath = System.getenv("FUELLOG_STORE_FILE") ?: "${rootDir}/release.keystore"
        create("release") {
            storeFile = file(storeFilePath)
            storePassword = System.getenv("FUELLOG_STORE_PASSWORD")
            keyAlias = System.getenv("FUELLOG_KEY_ALIAS") ?: "fuellog"
            keyPassword = System.getenv("FUELLOG_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Only attach signing when credentials are present (CI or local env).
            if (System.getenv("FUELLOG_STORE_PASSWORD") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "DebugProbesKt.bin"
            excludes += "kotlin/**"
            excludes += "META-INF/*.version"
            excludes += "META-INF/services/*"
        }
    }
}

dependencies {
    // Android
    implementation("androidx.core:core-ktx:1.15.0") {
        exclude(group = "androidx.emoji2")
    }
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7") {
        exclude(group = "androidx.profileinstaller")
    }
    implementation("androidx.activity:activity-compose:1.9.3")

    // Material Components (for XML themes) - commented out to minimize app size (using platform theme instead)
    // implementation("com.google.android.material:material:1.12.0")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Lifecycle (for viewModel() in Compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // KSP annotation processors
    ksp("androidx.room:room-compiler:2.6.1")
}
