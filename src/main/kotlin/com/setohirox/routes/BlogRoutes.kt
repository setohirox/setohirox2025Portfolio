package com.setohirox.routes

import com.setohirox.models.getBlogPosts
import com.setohirox.models.getFeaturedPosts
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
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import java.io.File

fun Route.blogRoutes() {
    // トップページ
    get("/") {
        val posts = getBlogPosts()
        val featuredPosts = getFeaturedPosts()
        
        call.respondHtml {
            head {
                title("setohiroxのブログ")
                meta(name = "description", content = "技術ブログと日常の記録")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                meta(charset = "UTF-8")
                meta(name = "author", content = "setohirox")
                meta(name = "keywords", content = "プログラミング,Web開発,技術ブログ")
                link(rel = "icon", type = "image/x-icon", href = "/favicon.ico")
                style {
                    unsafe {
                        +"""
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 1200px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            text-align: center;
                            margin-bottom: 3em;
                            padding: 2em 0;
                            background: #f8f9fa;
                            border-radius: 10px;
                        }
                        .header h1 {
                            font-size: 2.5em;
                            margin: 0;
                            color: #2c3e50;
                        }
                        .header p {
                            font-size: 1.2em;
                            color: #666;
                            margin: 1em 0 0;
                        }
                        .about-section {
                            margin: 3em 0;
                            padding: 2em;
                            background: #fff;
                            border-radius: 10px;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        }
                        .about-section h2 {
                            color: #2c3e50;
                            margin-top: 0;
                        }
                        .featured-posts {
                            margin: 3em 0;
                        }
                        .featured-posts h2 {
                            color: #2c3e50;
                            margin-bottom: 1em;
                        }
                        .post-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                            gap: 2em;
                        }
                        .post-card {
                            background: #fff;
                            border-radius: 10px;
                            overflow: hidden;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                            transition: transform 0.2s;
                        }
                        .post-card:hover {
                            transform: translateY(-5px);
                        }
                        .post-content {
                            padding: 1.5em;
                        }
                        .post-title {
                            color: #2c3e50;
                            text-decoration: none;
                            font-size: 1.3em;
                            font-weight: 600;
                            margin: 0 0 0.5em;
                            display: block;
                        }
                        .post-title:hover {
                            color: #3498db;
                        }
                        .post-date {
                            color: #666;
                            font-size: 0.9em;
                        }
                        .post-excerpt {
                            color: #666;
                            margin: 1em 0;
                        }
                        .read-more {
                            color: #3498db;
                            text-decoration: none;
                            font-weight: 500;
                        }
                        .read-more:hover {
                            text-decoration: underline;
                        }
                        .nav {
                            margin: 2em 0;
                            text-align: center;
                        }
                        .nav a {
                            color: #3498db;
                            text-decoration: none;
                            margin: 0 1em;
                            font-weight: 500;
                        }
                        .nav a:hover {
                            text-decoration: underline;
                        }
                        .no-featured {
                            text-align: center;
                            padding: 2em;
                            color: #666;
                            font-style: italic;
                        }
                        """
                    }
                }
            }
            body {
                div("header") {
                    h1 { +"setohiroxのブログ" }
                    p { +"技術ブログと日常の記録" }
                }
                
                nav("nav") {
                    a(href = "/") { +"ホーム" }
                    a(href = "/blog") { +"ブログ一覧" }
                }
                
                div("about-section") {
                    h2 { +"About" }
                    p { +"このブログでは、技術的なトピックや日々の学びを共有しています。主にプログラミング、Web開発、技術的な知見について書いています。" }
                }
                
                div("featured-posts") {
                    h2 { +"おすすめ記事" }
                    if (featuredPosts.isEmpty()) {
                        div("no-featured") {
                            +"おすすめ記事はまだありません。"
                        }
                    } else {
                        div("post-grid") {
                            featuredPosts.forEach { post ->
                                div("post-card") {
                                    div("post-content") {
                                        a(href = "/blog/${post.slug}", classes = "post-title") {
                                            +post.title
                                        }
                                        div("post-date") {
                                            +post.date
                                        }
                                        p("post-excerpt") {
                                            +post.excerpt
                                        }
                                        a(href = "/blog/${post.slug}", classes = "read-more") {
                                            +"続きを読む"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ブログ一覧ページ
    get("/blog") {
        val posts = getBlogPosts()
        
        call.respondHtml {
            head {
                title("ブログ一覧 - setohiroxのブログ")
                meta(name = "description", content = "技術ブログと日常の記録の一覧ページです。")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                meta(charset = "UTF-8")
                meta(name = "author", content = "setohirox")
                meta(name = "keywords", content = "プログラミング,Web開発,技術ブログ")
                link(rel = "icon", type = "image/x-icon", href = "/favicon.ico")
                style {
                    unsafe {
                        +"""
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 1200px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            text-align: center;
                            margin-bottom: 3em;
                            padding: 2em 0;
                            background: #f8f9fa;
                            border-radius: 10px;
                        }
                        .header h1 {
                            font-size: 2.5em;
                            margin: 0;
                            color: #2c3e50;
                        }
                        .search-container {
                            margin: 2em 0;
                            text-align: center;
                        }
                        .search-input {
                            width: 100%;
                            max-width: 600px;
                            padding: 12px 20px;
                            font-size: 1.1em;
                            border: 2px solid #ddd;
                            border-radius: 25px;
                            outline: none;
                            transition: border-color 0.3s;
                        }
                        .search-input:focus {
                            border-color: #3498db;
                        }
                        .search-input::placeholder {
                            color: #999;
                        }
                        .no-results {
                            text-align: center;
                            padding: 2em;
                            color: #666;
                            font-style: italic;
                        }
                        .posts-list {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                            gap: 2em;
                        }
                        .post-card {
                            background: #fff;
                            border-radius: 10px;
                            overflow: hidden;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                            transition: transform 0.2s;
                        }
                        .post-card:hover {
                            transform: translateY(-5px);
                        }
                        .post-content {
                            padding: 1.5em;
                        }
                        .post-title {
                            color: #2c3e50;
                            text-decoration: none;
                            font-size: 1.3em;
                            font-weight: 600;
                            margin: 0 0 0.5em;
                            display: block;
                        }
                        .post-title:hover {
                            color: #3498db;
                        }
                        .post-date {
                            color: #666;
                            font-size: 0.9em;
                        }
                        .post-excerpt {
                            color: #666;
                            margin: 1em 0;
                        }
                        .read-more {
                            color: #3498db;
                            text-decoration: none;
                            font-weight: 500;
                        }
                        .read-more:hover {
                            text-decoration: underline;
                        }
                        .nav {
                            margin: 2em 0;
                            text-align: center;
                        }
                        .nav a {
                            color: #3498db;
                            text-decoration: none;
                            margin: 0 1em;
                            font-weight: 500;
                        }
                        .nav a:hover {
                            text-decoration: underline;
                        }
                        """
                    }
                }
                script {
                    unsafe {
                        +"""
                        document.addEventListener('DOMContentLoaded', function() {
                            const searchInput = document.getElementById('search-input');
                            const postsList = document.getElementById('posts-list');
                            const posts = Array.from(document.getElementsByClassName('post-card'));
                            
                            function filterPosts() {
                                const searchTerm = searchInput.value.toLowerCase();
                                
                                posts.forEach(post => {
                                    const title = post.querySelector('.post-title').textContent.toLowerCase();
                                    const excerpt = post.querySelector('.post-excerpt').textContent.toLowerCase();
                                    const isVisible = title.includes(searchTerm) || excerpt.includes(searchTerm);
                                    
                                    post.style.display = isVisible ? 'block' : 'none';
                                });
                                
                                // 検索結果がない場合のメッセージ
                                const visiblePosts = posts.filter(post => post.style.display !== 'none');
                                const noResults = document.getElementById('no-results');
                                
                                if (visiblePosts.length === 0 && searchTerm !== '') {
                                    if (!noResults) {
                                        const message = document.createElement('div');
                                        message.id = 'no-results';
                                        message.className = 'no-results';
                                        message.textContent = '検索結果が見つかりませんでした。';
                                        postsList.parentNode.insertBefore(message, postsList);
                                    }
                                } else {
                                    if (noResults) {
                                        noResults.remove();
                                    }
                                }
                            }
                            
                            searchInput.addEventListener('input', filterPosts);
                        });
                        """
                    }
                }
            }
            body {
                div("header") {
                    h1 { +"Blog Posts" }
                }
                
                nav("nav") {
                    a(href = "/") { +"ホーム" }
                    a(href = "/blog") { +"ブログ一覧" }
                }
                
                div("search-container") {
                    input(type = InputType.text) {
                        id = "search-input"
                        placeholder = "記事を検索..."
                        classes = setOf("search-input")
                    }
                }
                
                div("posts-list") {
                    id = "posts-list"
                    for (post in posts) {
                        div("post-card") {
                            div("post-content") {
                                a(href = "/blog/${post.slug}") {
                                    classes = setOf("post-title")
                                    +post.title
                                }
                                div("post-date") {
                                    +post.date
                                }
                                p("post-excerpt") {
                                    +post.excerpt
                                }
                                a(href = "/blog/${post.slug}") {
                                    classes = setOf("read-more")
                                    +"続きを読む"
                                }
                            }
                        }
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
            .extensions(listOf(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                AutolinkExtension.create(),
                TaskListItemsExtension.create()
            ))
            .build()
        val document = parser.parse(mdContent)
        val renderer = HtmlRenderer.builder()
            .extensions(listOf(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                AutolinkExtension.create(),
                TaskListItemsExtension.create()
            ))
            .build()
        val html = renderer.render(document)

        call.respondHtml {
            head {
                title("${slug.replace("-", " ").replaceFirstChar { it.uppercase() }} - setohiroxのブログ")
                meta(name = "description", content = "技術ブログと日常の記録の記事ページです。")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                meta(charset = "UTF-8")
                meta(name = "author", content = "setohirox")
                meta(name = "keywords", content = "プログラミング,Web開発,技術ブログ")
                link(rel = "icon", type = "image/x-icon", href = "/favicon.ico")
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