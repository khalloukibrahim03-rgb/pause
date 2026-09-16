plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.dagger.hilt.android'
    id 'com.google.devtools.ksp'
}

android {
    namespace 'com.pause.app'
    compileSdk 35

    defaultConfig {
        applicationId "com.pause.app"
        minSdk 21
        targetSdk 35
        versionCode 1
        versionName "1.0.0"

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

    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                "META-INF/AL2_OQL.txt",
                "META-INF/LGPL2.1"
            )
        }
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
    implementation project(':keyboard')

    implementation libs.androidx.compose.bom
    implementation libs.androidx.compose.ui
    implementation libs.androidx.compose.ui.tooling.preview
    implementation libs.androidx.material3
    implementation libs.androidx.material3.window.size

    implementation libs.hilt.android
    ksp libs.hilt.kapt

    implementation libs.kotlinx.coroutines.core
    implementation libs.kotlinx.coroutines.android

    implementation libs.androidx.lifecycle.runtime.ktx
    implementation libs.androidx.lifecycle.viewmodel.compose

    implementation libs.androidx.activity.compose
    implementation libs.androidx.core.splashscreen

    implementation libs.androidx.datastore.preferences
    implementation libs.hilt.navigation.compose

    debugImplementation libs.androidx.compose.ui.tooling

    testImplementation libs.junit.jupiter.api
    testImplementation libs.junit.jupiter.params
    testImplementation libs.truth
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")

    androidTestImplementation libs.androidx.test.ext
    androidTestImplementation libs.androidx.test.runner
    androidTestImplementation libs.androidx.compose.ui.test.junit4
}
