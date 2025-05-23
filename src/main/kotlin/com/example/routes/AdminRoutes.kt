package com.example.routes

import com.example.plugins.UserSession
import com.example.plugins.CsrfSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import java.io.File
import java.util.*

fun Route.adminRoutes() {
    authenticate("admin") {
        get("/admin") {
            val principal = call.principal<UserIdPrincipal>()
            call.sessions.set(UserSession(principal?.name ?: ""))
            
            val csrfToken = UUID.randomUUID().toString()
            call.sessions.set(CsrfSession(csrfToken))
            
            call.respondHtml {
                head {
                    title("Admin Page")
                }
                body {
                    h1 { +"Admin Page" }
                    p { +"Welcome to the admin area!" }
                    form(action = "/admin", method = FormMethod.post) {
                        p { +"新規ブログ記事の作成" }
                        input(type = InputType.hidden) {
                            name = "csrfToken"
                            value = csrfToken
                        }
                        p {
                            label { +"タイトル: " }
                            textInput {
                                attributes["name"] = "title"
                                attributes["required"] = "required"
                            }
                        }
                        p {
                            label { +"スラッグ(URL): " }
                            textInput {
                                attributes["name"] = "slug"
                                attributes["required"] = "required"
                            }
                            span { style = "font-size:0.9em;color:#888;"; +" 例: hello-world" }
                        }
                        p {
                            label { +"本文(Markdown): " }
                            textArea(rows = "15", cols = "60") {
                                attributes["name"] = "content"
                                attributes["required"] = "required"
                            }
                        }
                        p {
                            submitInput { value = "投稿" }
                        }
                    }
                }
            }
        }

        post("/admin") {
            val session = call.sessions.get<UserSession>()
            if (session == null) {
                call.respondHtml {
                    body {
                        h1 { +"エラー" }
                        p { +"セッションが無効です。" }
                        a(href = "/admin") { +"戻る" }
                    }
                }
                return@post
            }

            val params = call.receiveParameters()
            val csrfToken = params["csrfToken"]
            val sessionToken = call.sessions.get<CsrfSession>()?.token
            
            if (csrfToken != sessionToken) {
                call.respondHtml {
                    body {
                        h1 { +"エラー" }
                        p { +"CSRFトークンが無効です。" }
                        a(href = "/admin") { +"戻る" }
                    }
                }
                return@post
            }
            
            val title = params["title"]?.trim() ?: ""
            val slug = params["slug"]?.trim()?.replace(" ", "-") ?: ""
            val content = params["content"]?.trim() ?: ""
            
            if (title.isBlank() || slug.isBlank() || content.isBlank()) {
                call.respondHtml {
                    body {
                        h1 { +"エラー" }
                        p { +"全ての項目を入力してください。" }
                        a(href = "/admin") { +"戻る" }
                    }
                }
                return@post
            }
            
            val file = File("src/main/resources/content/$slug.md")
            if (file.exists()) {
                call.respondHtml {
                    body {
                        h1 { +"エラー" }
                        p { +"同じスラッグの記事が既に存在します。" }
                        a(href = "/admin") { +"戻る" }
                    }
                }
                return@post
            }
            
            val md = "# $title\n\n$content"
            file.writeText(md)
            call.respondHtml {
                body {
                    h1 { +"投稿完了" }
                    p { +"記事を保存しました。" }
                    a(href = "/") { +"トップへ戻る" }
                    br {}
                    a(href = "/admin") { +"管理画面へ戻る" }
                }
            }
        }
    }
} 