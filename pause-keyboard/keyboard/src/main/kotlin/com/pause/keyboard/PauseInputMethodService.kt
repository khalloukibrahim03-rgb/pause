package com.pause.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.pause.intelligence.LocalIntelligenceEngine
import com.pause.shared.ShiftState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The main InputMethodService that backs the PAUSE keyboard.
 *
 * Lifecycle:
 * - onCreate: initializes state and intelligence engine
 * - onCreateInputView: inflates the Compose-based keyboard
 * - onStartInput: resets shift state for a new input session
 * - onDestroy: cleans up resources
 *
 * Privacy: This service NEVER logs keystrokes or sends text online.
 * All intelligence processing is local and in-memory only.
 */
@AndroidEntryPoint
class PauseInputMethodService : InputMethodService() {

    @Inject
    lateinit var keyboardState: KeyboardState

    @Inject
    lateinit var intelligenceEngine: LocalIntelligenceEngine

    @Inject
    lateinit var settingsManager: SettingsManager

    private var composeView: ComposeView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        // Observe settings changes and propagate to KeyboardState
        serviceScope.launch {
            settingsManager.settingsFlow.collect { settings ->
                keyboardState.updateSettings(settings)
            }
        }
    }

    /**
     * Called by the framework to create the keyboard's input view.
     * Returns a ComposeView backed by [PauseKeyboard].
     */
    override fun onCreateInputView(): View {
        if (composeView == null) {
            composeView = ComposeView(applicationContext).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrAppStopped)
            }
        }

        val cv = composeView!!
        cv.setContent {
            val settings = keyboardState.settings.collectAsState()
            val proposal = intelligenceEngine.interventions.collectAsState(null)

            PauseKeyboard(
                keyboardState = keyboardState,
                settings = settings.value,
                interventionProposal = proposal.value
            )
        }

        // Connect the intelligence observer
        keyboardState.intelligenceObserver = intelligenceEngine.signalObserver

        return cv
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        if (!restarting) {
            keyboardState.resetShift()
        }
        keyboardState.updateInputConnection(currentInputConnection)
        keyboardState.setEditorInfo(info)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboardState.updateInputConnection(currentInputConnection)
        keyboardState.setEditorInfo(info)
    }

    override fun onRestartInput() {
        super.onRestartInput()
        keyboardState.updateInputConnection(currentInputConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.coroutineContext.cancel()
        composeView = null
        intelligenceEngine.onSessionEnd()
        keyboardState.clear()
    }

    /**
     * Hardware keyboard support — forward physical key events to the
     * same processing path as soft-key events.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.repeatCount == 0) {
            keyboardState.handleHardwareKey(keyCode)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyUp(keyCode, event) ?: false
    }

    /**
     * Handle the back gesture. If shift is in upper mode, clear it
     * instead of dismissing popups.
     */
    fun handleBackPress() {
        if (keyboardState.shiftState.value is ShiftState.Upper) {
            keyboardState.resetShift()
        } else {
            keyboardState.dismissPopups()
        }
    }
}
