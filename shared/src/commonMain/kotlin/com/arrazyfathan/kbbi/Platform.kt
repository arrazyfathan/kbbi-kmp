package com.arrazyfathan.kbbi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform