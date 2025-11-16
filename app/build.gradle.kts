/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

val gitVersionName = providers.exec {
    commandLine("git", "describe", "--tags", "--abbrev=0")
}.standardOutput.asText.get().trim()

val gitVersionCode = providers.exec {
    commandLine("git", "tag", "--list")
}.standardOutput.asText.get().split("\n").size

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.alpsfly.aeroglide"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.alpsfly.aeroglide"
        minSdk = 26
        targetSdk = 35
        versionCode = gitVersionCode
        versionName = gitVersionName

        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "FIREBASE_FUNCTIONS_URL", "\"https://us-central1-thermalscout.cloudfunctions.net/\"")
        buildConfigField("String", "FIREBASE_EMULATOR_HOST_ADDRESS", "\"10.0.2.2\"")
        buildConfigField("Integer", "FIREBASE_EMULATOR_PORT_AUTH", "9099")
        buildConfigField("Integer", "FIREBASE_EMULATOR_PORT_FUNCTIONS", "5001")
        buildConfigField("Integer", "FIREBASE_EMULATOR_PORT_FIRESTORE", "8080")
        buildConfigField("Integer", "FIREBASE_EMULATOR_PORT_PUBSUB", "8085")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        aidl = false
        buildConfig = true
        renderScript = false
        shaders = false
    }
}

dependencies {
    api(project(":core:common"))
    api(project(":core:domain"))
    api(project(":core:model"))
    api(project(":core:data"))
    api(project(":core:database"))
    api(project(":core:firebase"))
    api(project(":core:ui"))

    api(project(":feature:activityhistory"))
    api(project(":feature:billing"))
    api(project(":feature:dataexchange"))
    api(project(":feature:devicestatus"))
    api(project(":feature:diagnosis"))
    api(project(":feature:disclaimer"))
    api(project(":feature:livetracking"))
    api(project(":feature:mapmanager"))
    api(project(":feature:settings"))
    api(project(":feature:variometer"))

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.kontlinx.serialization.core)
    implementation(libs.kontlinx.serialization.json)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Arch Components
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Android billing
    implementation(libs.billing.ktx)

    // Import the BoM for Firebase
    implementation(platform(libs.firebase.bom))

    // Add Firebase libraries
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.playintegrity)
    implementation(libs.firebase.appcheck.debug)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.retrofitConverterScalars)
    implementation(libs.okhttp)
    implementation(libs.okhttpLoggingInterceptor)

    // Vico chart library
    implementation(libs.vico.core)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // Logging
    implementation(libs.timber)

    // mapsforge map core
    // https://opendatacommons.org/licenses/dbcl/1-0/
    implementation(libs.mapsforge.core)
    implementation(libs.mapsforge.map)
    implementation(libs.mapsforge.map.reader)
    implementation(libs.mapsforge.themes)
    implementation(libs.kxml2)

    // mapsforge map android
    implementation(libs.mapsforge.map.android)
    implementation(libs.androidsvg)

    // Tooling
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
