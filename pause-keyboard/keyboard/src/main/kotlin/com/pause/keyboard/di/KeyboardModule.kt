package com.pause.keyboard.di

import android.content.Context
import android.os.Vibrator
import com.pause.keyboard.KeyboardState
import com.pause.keyboard.settings.SettingsManager
import com.pause.shared.KeyboardSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * Hilt DI module for the keyboard layer.
 *
 * Provides:
 * - SettingsManager (wraps DataStore, shared across IME + app processes)
 * - KeyboardState (mutable keyboard state holder)
 * - Vibrator (for haptic feedback)
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
        @ApplicationContext context: Context
    ): SettingsManager = SettingsManager(context)

    @Provides
    @Singleton
    fun provideVibrator(
        @ApplicationContext context: Context
    ): Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
}
