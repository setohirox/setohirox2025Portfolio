package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*

data class UserSession(val userId: String)
data class CsrfSession(val token: String)

fun Application.configureSessions() {
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 1800  // 30分
            cookie.httpOnly = true  // JavaScriptからのアクセスを防止
        }
        cookie<CsrfSession>("csrf_token") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 1800  // 30分
            cookie.httpOnly = true  // JavaScriptからのアクセスを防止
        }
    }
} 