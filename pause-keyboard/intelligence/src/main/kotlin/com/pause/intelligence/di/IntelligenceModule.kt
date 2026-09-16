package com.pause.intelligence.di

import com.pause.intelligence.InterventionScheduler
import com.pause.intelligence.LocalIntelligenceEngine
import com.pause.intelligence.TypingSignalCollector
import com.pause.shared.KeyboardSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the LocalIntelligence layer.
 *
 * The settings provider lambda (which depends on SettingsManager from
 * the keyboard module) is provided by KeyboardModule in the keyboard
 * module to avoid a circular dependency.
 */
@Module
@InstallIn(SingletonComponent::class)
object IntelligenceModule {

    // TypingSignalCollector, InterventionScheduler, LocalIntelligenceEngine
    // are all @Inject-constructed and auto-provided by Hilt.
}
