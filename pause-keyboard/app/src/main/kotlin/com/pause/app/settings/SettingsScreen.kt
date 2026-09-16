package com.pause.app.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pause.app.R
import com.pause.shared.KeyboardSettings
import com.pause.shared.Language

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.settingsState.collectAsState()
    val settings = uiState.settings

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(title = { Text(stringResource(R.string.settings_title)) })

        if (settings != null) {
            SettingsContent(
                settings = settings,
                onLanguageChange = { viewModel.updateLanguage(it) },
                onHeightChange = { viewModel.updateHeightRatio(it) },
                onHapticChange = { viewModel.updateHaptic(it) },
                onSoundChange = { viewModel.updateSound(it) },
                onVibrationChange = { viewModel.updateVibration(it) },
                onThemeChange = { viewModel.updateTheme(it) },
                onInterventionsChange = { viewModel.updateInterventions(it) }
            )
        }
    }
}

@Composable
fun SettingsContent(
    settings: KeyboardSettings,
    onLanguageChange: (Language) -> Unit,
    onHeightChange: (Float) -> Unit,
    onHapticChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onVibrationChange: (Int) -> Unit,
    onThemeChange: (Boolean) -> Unit,
    onInterventionsChange: (Boolean) -> Unit
) {
    SettingsSectionHeader(stringResource(R.string.settings_appearance))

    SettingRow("Language", settings.activeLanguage.name, onLanguageClick = { })
    SettingSlider(
        label = stringResource(R.string.settings_keyboard_height_title),
        value = settings.heightRatio,
        valueRange = 0.25f..0.65f,
        onValueChange = onHeightChange
    )
    SettingSwitch(
        label = stringResource(R.string.settings_theme_title),
        value = settings.isDarkTheme,
        onValueChange = onThemeChange
    )
    Divider()

    SettingsSectionHeader(stringResource(R.string.settings_feedback))
    SettingSwitch(
        label = stringResource(R.string.settings_haptic_title),
        value = settings.hapticEnabled,
        onValueChange = onHapticChange
    )
    SettingSwitch(
        label = stringResource(R.string.settings_sound_title),
        value = settings.soundEnabled,
        onValueChange = onSoundChange
    )
    SettingSliderInt(
        label = stringResource(R.string.settings_vibration_title),
        value = settings.vibrationIntensity,
        valueRange = 0..255,
        onValueChange = onVibrationChange
    )
    Divider()

    SettingsSectionHeader(stringResource(R.string.settings_privacy))
    SettingSwitch(
        label = stringResource(R.string.settings_interventions_title),
        value = settings.interventionsEnabled,
        onValueChange = onInterventionsChange
    )
    Text(
        text = stringResource(R.string.privacy_policy_summary),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(16.dp)
    )
    Divider()

    SettingsSectionHeader(stringResource(R.string.settings_about))
    Text(
        text = stringResource(R.string.version_label),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingRow(
    label: String,
    value: String,
    onLanguageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLanguageClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SettingSwitch(label: String, value: Boolean, onValueChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = value, onCheckedChange = onValueChange)
    }
}

@Composable
fun SettingSlider(label: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingSliderInt(label: String, value: Int, valueRange: IntRange, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = "$label: $value", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
