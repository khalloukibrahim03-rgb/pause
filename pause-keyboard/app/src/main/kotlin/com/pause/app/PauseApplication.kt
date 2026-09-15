package com.pause.app

import android.app.Application
import dagger.hilt.android.HiltApplication

/**
 * Application class for the PAUSE companion app.
 * Sets up Hilt for dependency injection across all modules.
 */
@HiltApplication
class PauseApplication : Application()
