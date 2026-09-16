package com.pause.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for the companion app.
 *
 * Provides Context-level dependencies shared across modules.
 * The SettingsManager (in :keyboard module) is already provided by
 * [com.pause.keyboard.di.KeyboardModule].
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context
    ): Context = context
}
