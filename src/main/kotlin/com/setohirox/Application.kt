package com.setohirox

import com.setohirox.plugins.configureAuthentication
import com.setohirox.plugins.configureSessions
import com.setohirox.routes.adminRoutes
import com.setohirox.routes.blogRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // プラグインの設定
    configureAuthentication()
    configureSessions()

    // デフォルトヘッダーの設定
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("X-XSS-Protection", "1; mode=block")
    }

    // レート制限の設定
    install(RateLimit) {
        register {
            rateLimiter(limit = 5, refillPeriod = 60.seconds)
        }
    }

    // ルーティングの設定
    routing {
        blogRoutes()
        adminRoutes()
    }
} 