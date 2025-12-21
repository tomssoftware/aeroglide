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

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.alpsfly.aeroglide.core.firebase"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "com.alpsfly.aeroglide.core.testing.HiltTestRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField(
            "String",
            "FIREBASE_FUNCTIONS_URL",
            "\"https://us-central1-thermalscout.cloudfunctions.net/\""
        )
        buildConfigField("String", "FIREBASE_EMULATOR_HOST_ADDRESS", "\"10.0.2.2\"")
        buildConfigField("Integer", "FIREBASE_EMULATOR_PORT_AUTH", "9099")
        buildConfigField("Integer", "FIREBASE_EMULATOR_PORT_FUNCTIONS", "5001")
        buildConfigField("Integer", "FIREBASE_EMULATOR_PORT_FIRESTORE", "8080")
        buildConfigField("Integer", "FIREBASE_EMULATOR_PORT_PUBSUB", "8085")
    }

    buildFeatures {
        aidl = false
        buildConfig = true
        renderScript = false
        shaders = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":core:model"))

    // Arch Components
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

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

    // Logging
    implementation(libs.timber)
}
