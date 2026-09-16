package com.pause.app.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pause.app.PAUSEAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Settings host Activity — launched from the keyboard or dashboard.
 */
@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAUSEAppTheme {
                SettingsScreen()
            }
        }
    }
}
