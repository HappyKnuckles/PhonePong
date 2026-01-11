package org.phonepong.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform