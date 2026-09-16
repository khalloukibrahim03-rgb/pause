plugins {
    id 'com.android.library'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.dagger.hilt.android'
    id 'com.google.devtools.ksp'
}

android {
    namespace 'com.pause.keyboard'
    compileSdk 35

    defaultConfig {
        minSdk 21
        targetSdk 35

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.4"
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            all { it.useJUnitPlatform() }
        }
    }
}

dependencies {
    implementation project(':shared')
    implementation project(':intelligence')

    implementation libs.androidx.compose.bom
    implementation libs.androidx.compose.ui
    implementation libs.androidx.compose.ui.tooling.preview
    implementation libs.androidx.material3

    implementation libs.hilt.android
    ksp libs.hilt.kapt

    implementation libs.kotlinx.coroutines.core
    implementation libs.kotlinx.coroutines.android

    implementation libs.androidx.lifecycle.runtime.ktx
    implementation libs.androidx.lifecycle.viewmodel.compose

    implementation libs.androidx.datastore.preferences

    debugImplementation libs.androidx.compose.ui.tooling

    testImplementation libs.junit.jupiter.api
    testImplementation libs.junit.jupiter.params
    testImplementation libs.truth
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}
