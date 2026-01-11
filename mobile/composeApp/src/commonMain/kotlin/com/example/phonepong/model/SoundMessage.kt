package com.example.phonepong.model

import kotlinx.serialization.Serializable

@Serializable
data class SoundMessage(
    val type: String,
    val sound: String
)
