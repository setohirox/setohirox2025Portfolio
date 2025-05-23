package com.example.routes

import com.example.models.getBlogPosts
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import java.io.File

fun Route.blogRoutes() {
    get("/") {
        val posts = getBlogPosts()
        call.respondHtml {
            head {
                title("My Blog")
                style {
                    unsafe {
                        +"""
                        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
                        .post { margin-bottom: 30px; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }
                        .post-title { color: #333; text-decoration: none; }
                        .post-date { color: #666; font-size: 0.9em; }
                        .post-excerpt { margin-top: 10px; }
                        """
                    }
                }
            }
            body {
                h1 { +"My Blog" }
                posts.forEach { post ->
                    div("post") {
                        h2 {
                            a(href = "/blog/${post.slug}", classes = "post-title") {
                                +post.title
                            }
                        }
                        div("post-date") { +post.date }
                        div("post-excerpt") { +post.excerpt }
                    }
                }
            }
        }
    }

    get("/blog/{slug}") {
        val slug = call.parameters["slug"] ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
        val mdFile = File("src/main/resources/content/$slug.md")
        if (!mdFile.exists()) {
            call.respondText("Not Found", status = HttpStatusCode.NotFound)
            return@get
        }

        val mdContent = mdFile.readText()
        val parser = Parser.builder()
            .extensions(listOf(TablesExtension.create(), StrikethroughExtension.create()))
            .build()
        val document = parser.parse(mdContent)
        val renderer = HtmlRenderer.builder()
            .extensions(listOf(TablesExtension.create(), StrikethroughExtension.create()))
            .build()
        val html = renderer.render(document)

        call.respondHtml {
            head {
                title("Blog Post")
                style {
                    unsafe {
                        +"""
                        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
                        .post { margin-bottom: 30px; }
                        .post-title { color: #333; }
                        .post-date { color: #666; font-size: 0.9em; }
                        .post-content { line-height: 1.6; }
                        """
                    }
                }
            }
            body {
                div("post") {
                    h1("post-title") { +slug.replace("-", " ").replaceFirstChar { it.uppercase() } }
                    div("post-content") {
                        unsafe { +html }
                    }
                }
            }
        }
    }
} 