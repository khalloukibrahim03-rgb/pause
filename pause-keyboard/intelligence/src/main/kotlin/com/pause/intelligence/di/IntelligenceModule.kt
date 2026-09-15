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
 * The [com.pause.keyboard.settings.SettingsManager] (in the keyboard module)
 * provides the current settings via a lambda. We expose it here so that
 * [LocalIntelligenceEngine] can be injected across module boundaries
 * without creating a compile-time dependency from intelligence → keyboard.
 */
@Module
@InstallIn(SingletonComponent::class)
object IntelligenceModule {

    @Provides
    @Singleton
    fun provideSettingsProvider(
        settingsManager: com.pause.keyboard.settings.SettingsManager
    ): () -> KeyboardSettings = { settingsManager.currentSettings() }

    // TypingSignalCollector, InterventionScheduler, LocalIntelligenceEngine
    // are all @Inject-constructed and auto-provided by Hilt.
}
