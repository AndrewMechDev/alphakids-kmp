package org.alphakids.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform