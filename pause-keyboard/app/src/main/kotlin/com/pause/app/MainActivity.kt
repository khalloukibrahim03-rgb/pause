package com.pause.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pause.app.dashboard.DashboardScreen
import com.pause.app.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private fun isPauseKeyboardEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            this.contentResolver,
            "enabled_input_methods"
        )
        return enabled?.contains("com.pause.app") == true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val enabled = isPauseKeyboardEnabled()

        setContent {
            PAUSEAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (enabled) {
                        DashboardScreen(
                            onSettingsClick = {
                                startActivity(Intent(this, SettingsActivity::class.java))
                            }
                        )
                    } else {
                        OnboardingContent(
                            onEnableComplete = { recreate() },
                            onSkip = {
                                DashboardScreen {
                                    startActivity(Intent(this, SettingsActivity::class.java))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingContent(
    onEnableComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val ctx = context as? ComponentActivity
        val enabled = ctx?.let {
            val e = Settings.Secure.getString(it.contentResolver, "enabled_input_methods")
            e?.contains("com.pause.app") == true
        } ?: false
        if (enabled) onEnableComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = stringResource(R.string.onboarding_page1_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = stringResource(R.string.onboarding_page1_desc),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Text(
            text = stringResource(R.string.onboarding_page2_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = stringResource(R.string.onboarding_page2_desc),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                launcher.launch(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.enable_keyboard_button),
                fontSize = 16.sp
            )
        }

        TextButton(
            onClick = onSkip,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(R.string.onboarding_page3_desc))
        }
    }
}
