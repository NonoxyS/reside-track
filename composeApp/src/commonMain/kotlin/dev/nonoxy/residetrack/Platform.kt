package dev.nonoxy.residetrack

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform