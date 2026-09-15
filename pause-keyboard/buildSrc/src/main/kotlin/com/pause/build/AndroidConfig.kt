package com.pause.build

/**
 * Shared Android configuration constants.
 * Each module's build.gradle.kts applies plugins directly (see root build.gradle.kts for versions).
 */
object AndroidConfig {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 21
    const val TARGET_SDK = 35
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0.0"

    const val COMPOSE_COMPILER_VERSION = "1.7.4"
    const val JVM_TARGET = "17"
}
