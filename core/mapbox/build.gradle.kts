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

import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) load(localPropertiesFile.inputStream())
}

android {
    namespace = "de.tomssoftware.aeroglide.core.mapbox"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"${localProperties.getProperty("MAPBOX_ACCESS_TOKEN", "")}\""
        )
    }

    buildFeatures {
        aidl = false
        buildConfig = true
        compose = true

        shaders = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":core:model"))
    api(project(":core:firebase"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kontlinx.serialization.core)
    implementation(libs.kontlinx.serialization.json)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.retrofitConverterScalars)
    implementation(libs.okhttp)
    implementation(libs.okhttpLoggingInterceptor)

    // Mapbox
    implementation(libs.mapbox.android)
    implementation(libs.mapbox.compose)
}

