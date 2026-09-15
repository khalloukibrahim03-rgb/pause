package com.pause.keyboard.di

import android.content.Context
import android.os.Vibrator
import com.pause.keyboard.KeyboardState
import com.pause.keyboard.settings.SettingsManager
import com.pause.intelligence.LocalIntelligenceEngine
import com.pause.intelligence.TypingSignalCollector
import com.pause.intelligence.InterventionScheduler
import com.pause.shared.KeyboardSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for the keyboard layer.
 *
 * Provides:
 * - SettingsManager (wraps DataStore)
 * - KeyboardState (mutable keyboard state)
 * - LocalIntelligenceEngine (observes typing, produces interventions)
 * - InterventionScheduler (throttles interventions)
 * - TypingSignalCollector (implements IntelligenceObserver)
 * - VibrationController deps
 */
@Module
@InstallIn(SingletonComponent::class)
object KeyboardModule {

    @Provides
    @Singleton
    fun provideKeyboardState(): KeyboardState = KeyboardState()

    @Provides
    @Singleton
    fun provideSettingsManager(
        context: Context
    ): SettingsManager = SettingsManager(context)

    @Provides
    @Singleton
    fun provideVibrator(
        context: Context
    ): Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    @Provides
    @Singleton
    fun provideSettingsProvider(
        settingsManager: SettingsManager
    ): () -> KeyboardSettings = { settingsManager.currentSettings() }

    // Intelligence engine is provided in the intelligence module's DI,
    // but we expose the wiring here for clarity.
}
