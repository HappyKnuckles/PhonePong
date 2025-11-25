package com.example.insanecrossmobilepingpongapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform