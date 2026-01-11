package com.example.phonepong

class JsPlatform : Platform {
    override val name: String = "Web Browser"
}

actual fun getPlatform(): Platform = JsPlatform()
