package com.example.phonepong.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual val needsNativeTextInput: Boolean = false

@Composable
actual fun Modifier.nativeTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    interactionSource: MutableInteractionSource,
    maxLength: Int,
): Modifier = this
