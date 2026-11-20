package com.example.phonepong.ui

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent

actual val needsNativeTextInput: Boolean = true

@Composable
actual fun Modifier.nativeTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    interactionSource: MutableInteractionSource,
    maxLength: Int,
): Modifier {
    val density = LocalDensity.current.density
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val input = remember { createInvisibleInput(maxLength) }

    DisposableEffect(input, interactionSource) {
        var focus: FocusInteraction.Focus? = null
        input.addEventListener("input", { currentOnValueChange(input.value) })
        input.addEventListener("focus", {
            val newFocus = FocusInteraction.Focus()
            focus = newFocus
            interactionSource.tryEmit(newFocus)
        })
        input.addEventListener("blur", {
            focus?.let { interactionSource.tryEmit(FocusInteraction.Unfocus(it)) }
            focus = null
        })
        // "Done" / Enter closes the keyboard
        input.addEventListener("keydown", { event ->
            if ((event as KeyboardEvent).key == "Enter") input.blur()
        })
        document.body?.appendChild(input)
        onDispose {
            focus?.let { interactionSource.tryEmit(FocusInteraction.Unfocus(it)) }
            input.remove()
        }
    }

    // onValueChange may change the text (e.g. uppercase, letters only): show the same in the input
    SideEffect {
        if (input.value != value) input.value = value
    }

    return onGloballyPositioned { coordinates ->
        // Compose lays out in device pixels, CSS positions in CSS pixels
        val bounds = coordinates.boundsInWindow()
        input.style.left = "${bounds.left / density}px"
        input.style.top = "${bounds.top / density}px"
        input.style.width = "${bounds.width / density}px"
        input.style.height = "${bounds.height / density}px"
    }
}

private fun createInvisibleInput(maxLength: Int): HTMLInputElement =
    (document.createElement("input") as HTMLInputElement).apply {
        type = "text"
        this.maxLength = maxLength
        setAttribute("autocapitalize", "characters")
        setAttribute("autocomplete", "off")
        setAttribute("autocorrect", "off")
        setAttribute("spellcheck", "false")
        setAttribute("enterkeyhint", "done")
        // Invisible but above the canvas, so it gets the tap. 16px keeps iOS from zooming in on focus.
        style.cssText = "position: fixed; margin: 0; padding: 0; border: 0; outline: none; background: transparent; " +
            "color: transparent; caret-color: transparent; font-size: 16px; opacity: 0; z-index: 10;"
    }
