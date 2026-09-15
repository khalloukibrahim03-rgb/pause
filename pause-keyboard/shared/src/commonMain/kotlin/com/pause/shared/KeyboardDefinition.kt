package com.pause.shared

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A complete keyboard layout for one (Language, KeyboardMode) combination.
 * Produced by KeyboardProvider implementations.
 */
@Parcelize
data class KeyboardDefinition(
    val name: String,
    val language: Language,
    val mode: KeyboardMode,
    val rows: List<List<KeySpec>>,
    val edgePaddingRatio: Float = 0f,
    val bottomEdgePaddingRatio: Float = 0f
) : Parcelable
