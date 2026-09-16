package com.pause.app.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pause.app.PAUSEAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Dashboard Activity — shows aggregate typing metrics from the
 * local intelligence engine. No message content is displayed.
 */
@AndroidEntryPoint
class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAUSEAppTheme {
                DashboardScreen(
                    onSettingsClick = {
                        startActivity(
                            android.content.Intent(this, com.pause.app.settings.SettingsActivity::class.java)
                        )
                    }
                )
            }
        }
    }
}
