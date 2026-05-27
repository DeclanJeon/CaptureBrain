plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

import java.util.Properties

android {
    namespace = "com.ponslink.capturebrain"
    compileSdk = 35

    // Read version from version.properties (CI auto-bumps this file)
    val versionProps = Properties().apply {
        val vf = file("../version.properties")
        if (vf.exists()) vf.inputStream().use { load(it) }
    }
    val versionCodeVal = (versionProps.getProperty("VERSION_CODE") ?: "1").toInt()
    val versionNameVal = versionProps.getProperty("VERSION_NAME") ?: "0.1.0"

    val releaseStoreFile = providers.environmentVariable("CAPTUREBRAIN_UPLOAD_STORE_FILE").orNull
    val releaseStorePassword = providers.environmentVariable("CAPTUREBRAIN_UPLOAD_STORE_PASSWORD").orNull
    val releaseKeyAlias = providers.environmentVariable("CAPTUREBRAIN_UPLOAD_KEY_ALIAS").orNull
    val releaseKeyPassword = providers.environmentVariable("CAPTUREBRAIN_UPLOAD_KEY_PASSWORD").orNull
    val hasReleaseSigning = listOf(
        releaseStoreFile,
        releaseStorePassword,
        releaseKeyAlias,
        releaseKeyPassword
    ).all { !it.isNullOrBlank() }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.ponslink.capturebrain"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = versionNameVal

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/AL2.0"
            excludes += "/META-INF/LGPL2.1"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

kotlin {
    // Match Android Studio snap bundled JBR used by CLI build setup.
    jvmToolchain(21)
}

dependencies {
    val roomVersion = "2.6.1"
    val workVersion = "2.10.0"
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")

    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.22.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Google Drive API client should be wired after OAuth consent/client-id setup.
    // Keep credentials out of the repository; provide client IDs through secure runtime config.
    implementation("com.google.api-client:google-api-client-android:2.7.1")
    implementation("com.google.http-client:google-http-client-android:1.45.3")
    implementation("com.google.http-client:google-http-client-gson:1.45.3")
    implementation("com.google.apis:google-api-services-drive:v3-rev20260428-2.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
