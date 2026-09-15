plugins {
    id 'com.android.library'
    id 'org.jetbrains.kotlin.android'
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

    buildFeatures {
        compose true
    }
}

dependencies {
    // No compose or hilt needed in pure shared module;
    // only Kotlin standard library (bundled in AGP)
}
