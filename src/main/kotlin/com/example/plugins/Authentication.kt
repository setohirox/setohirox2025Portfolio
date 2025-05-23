package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureAuthentication() {
    val adminUser = System.getenv("ADMIN_USER") ?: throw IllegalStateException("ADMIN_USER environment variable is not set")
    val adminPassword = System.getenv("ADMIN_PASSWORD") ?: throw IllegalStateException("ADMIN_PASSWORD environment variable is not set")
    
    install(Authentication) {
        basic("admin") {
            validate { credentials ->
                if (credentials.name == adminUser && credentials.password == adminPassword) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
} 