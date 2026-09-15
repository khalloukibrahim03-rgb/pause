package com.pause.app.onboarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pause.app.PAUSEAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Dedicated onboarding Activity.
 * Shows setup instructions for enabling the keyboard in Settings.
 */
@AndroidEntryPoint
class EnableKeyboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAUSEAppTheme {
                EnableKeyboardScreen()
            }
        }
    }
}
