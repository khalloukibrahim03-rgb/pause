package com.pause.shared

/**
 * Type of Pause intervention to display.
 * Each type has a progressively more visible UI treatment.
 */
enum class InterventionType {
    BREATHING_PROMPT,    // Subtle pulse on spacebar — just a visual cue
    SUBTLE_HIGHLIGHT,    // Gentle highlight of the spacebar with a soft icon
    MINIMAL_TIP          // A very short, neutral informational tip (1-2 words)
}
