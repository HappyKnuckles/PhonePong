package com.example.phonepong.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * True where Compose text fields can't be typed into on a phone. In the browser Compose draws into a
 * canvas, and mobile browsers often don't open the keyboard for its text fields (JetBrains CMP-8158).
 * There the field is shown read-only and [nativeTextInput] takes the input.
 */
expect val needsNativeTextInput: Boolean

/**
 * In the browser: puts an invisible HTML input over this element. It gets the tap, so the phone's own
 * keyboard opens, and reports every change to [onValueChange] and its focus to [interactionSource]
 * (so the field looks focused). Elsewhere it does nothing.
 */
@Composable
expect fun Modifier.nativeTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    interactionSource: MutableInteractionSource,
    maxLength: Int,
): Modifier
