package com.example.insanecrossmobilepingpongapp.model

import kotlinx.serialization.Serializable

@Serializable
data class SoundMessage(
    val type: String,
    val sound: String
)
