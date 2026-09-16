package com.pause.app.onboarding

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pause.app.PAUSEAppTheme
import com.pause.app.R

/**
 * Separate Activity that guides the user through enabling the keyboard.
 * Can be launched from Settings or MainActivity.
 */
class EnableKeyboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAUSEAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EnableKeyboardScreen(
                        onEnableClicked = {
                            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EnableKeyboardScreen(onEnableClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_page3_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.onboarding_page3_desc),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onEnableClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.enable_keyboard_button),
                fontSize = 16.sp
            )
        }
    }
}
