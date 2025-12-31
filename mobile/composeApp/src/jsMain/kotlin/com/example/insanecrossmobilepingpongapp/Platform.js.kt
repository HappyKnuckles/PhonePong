package com.example.insanecrossmobilepingpongapp

class JsPlatform : Platform {
    override val name: String = "Web Browser"
}

actual fun getPlatform(): Platform = JsPlatform()
