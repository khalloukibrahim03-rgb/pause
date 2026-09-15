package com.pause.shared

/**
 * Reserved key codes using negative values to avoid collision with
 * Unicode / KeyEvent positive codes.
 */
object KeyCodes {
    const val SHIFT: Int = -1
    const val CAPS_LOCK: Int = -2
    const val BACKSPACE: Int = -3
    const val ENTER: Int = -4
    const val SPACE: Int = -5
    const val LANGUAGE_SWITCH: Int = -6
    const val MODE_TOGGLE: Int = -7
    const val ARROW_LEFT: Int = -8
    const val ARROW_RIGHT: Int = -9
    const val ARROW_UP: Int = -10
    const val ARROW_DOWN: Int = -11
    const val DOT: Int = -12
    const val COMMA: Int = -13
    const val EMOJI: Int = -14
    const val DONE: Int = -100
}
