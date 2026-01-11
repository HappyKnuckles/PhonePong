package com.example.phonepong

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
