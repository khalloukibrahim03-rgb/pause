plugins {
    id 'com.android.library'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-parcelize'
}

android {
    namespace 'com.pause.shared'
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

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            all { it.useJUnitPlatform() }
        }
    }
}

dependencies {
    implementation libs.kotlinx.coroutines.core

    testImplementation libs.junit.jupiter.api
    testImplementation libs.junit.jupiter.params
    testImplementation libs.truth
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}
