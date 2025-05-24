package com.setohirox.routes

import com.setohirox.plugins.UserSession
import com.setohirox.plugins.CsrfSession
import com.setohirox.models.getBlogPosts
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.http.content.*
import io.ktor.http.*
import kotlinx.html.*
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import java.io.File
import java.util.*

fun Route.adminRoutes() {
    authenticate("admin") {
        get("/admin") {
            val principal = call.principal<UserIdPrincipal>()
            call.sessions.set(UserSession(principal?.name ?: ""))
            
            val csrfToken = UUID.randomUUID().toString()
            call.sessions.set(CsrfSession(csrfToken))
            
            val posts = getBlogPosts()
            
            call.respondHtml {
                head {
                    title("Admin Page")
                    style {
                        unsafe {
                            +"""
                            body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; }
                            .editor-container { display: flex; gap: 20px; }
                            .editor-section { flex: 1; }
                            .preview-section { flex: 1; border: 1px solid #ddd; padding: 20px; border-radius: 5px; }
                            textarea { width: 100%; min-height: 400px; padding: 10px; font-family: monospace; }
                            input[type="text"] { width: 100%; padding: 8px; margin-bottom: 10px; }
                            
                            /* プレビュー部分のスタイル */
                            .preview-content {
                                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                                line-height: 1.6;
                                color: #333;
                            }
                            .preview-content h1 {
                                font-size: 2em;
                                margin: 1em 0 0.5em;
                                padding-bottom: 0.3em;
                                border-bottom: 1px solid #eaecef;
                            }
                            .preview-content h2 {
                                font-size: 1.5em;
                                margin: 1em 0 0.5em;
                                padding-bottom: 0.3em;
                                border-bottom: 1px solid #eaecef;
                            }
                            .preview-content h3 {
                                font-size: 1.25em;
                                margin: 1em 0 0.5em;
                            }
                            .preview-content p {
                                margin: 0 0 1em;
                            }
                            .preview-content hr {
                                height: 1px;
                                background-color: #eaecef;
                                border: none;
                                margin: 1.5em 0;
                            }
                            .preview-content code {
                                font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
                                background-color: #f6f8fa;
                                padding: 0.2em 0.4em;
                                border-radius: 3px;
                                font-size: 0.9em;
                            }
                            .preview-content pre {
                                background-color: #f6f8fa;
                                padding: 16px;
                                border-radius: 6px;
                                overflow-x: auto;
                                font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
                                font-size: 0.9em;
                                line-height: 1.45;
                            }
                            .preview-content pre code {
                                background-color: transparent;
                                padding: 0;
                                border-radius: 0;
                            }
                            .preview-content blockquote {
                                margin: 0 0 1em;
                                padding: 0 1em;
                                color: #6a737d;
                                border-left: 0.25em solid #dfe2e5;
                            }
                            .preview-content ul, .preview-content ol {
                                padding-left: 2em;
                                margin: 0 0 1em;
                            }
                            .preview-content li {
                                margin: 0.25em 0;
                            }
                            .preview-content table {
                                border-collapse: collapse;
                                width: 100%;
                                margin: 1em 0;
                            }
                            .preview-content th, .preview-content td {
                                padding: 6px 13px;
                                border: 1px solid #dfe2e5;
                            }
                            .preview-content th {
                                background-color: #f6f8fa;
                                font-weight: 600;
                            }
                            .preview-content tr:nth-child(2n) {
                                background-color: #f6f8fa;
                            }
                            
                            /* 記事一覧のスタイル */
                            .posts-list {
                                margin: 2em 0;
                                border: 1px solid #ddd;
                                border-radius: 5px;
                            }
                            .posts-list h2 {
                                margin: 0;
                                padding: 1em;
                                background: #f6f8fa;
                                border-bottom: 1px solid #ddd;
                            }
                            .post-item {
                                padding: 1em;
                                border-bottom: 1px solid #ddd;
                                display: flex;
                                justify-content: space-between;
                                align-items: center;
                            }
                            .post-item:last-child {
                                border-bottom: none;
                            }
                            .post-title {
                                font-size: 1.1em;
                                color: #0366d6;
                                text-decoration: none;
                            }
                            .post-title:hover {
                                text-decoration: underline;
                            }
                            .post-actions {
                                display: flex;
                                gap: 10px;
                            }
                            .btn {
                                padding: 5px 10px;
                                border: none;
                                border-radius: 3px;
                                cursor: pointer;
                                text-decoration: none;
                                font-size: 0.9em;
                            }
                            .btn-edit {
                                background: #0366d6;
                                color: white;
                            }
                            .btn-delete {
                                background: #d73a49;
                                color: white;
                            }
                            .btn:hover {
                                opacity: 0.9;
                            }
                            .featured-checkbox {
                                margin-right: 1em;
                            }
                            .featured-label {
                                display: flex;
                                align-items: center;
                                gap: 0.5em;
                                cursor: pointer;
                            }
                            .featured-label input[type="checkbox"] {
                                width: 18px;
                                height: 18px;
                            }
                            """
                        }
                    }
                    script {
                        unsafe {
                            +"""
                            document.addEventListener('DOMContentLoaded', function() {
                                function updatePreview() {
                                    const content = document.getElementById('content').value;
                                    const preview = document.getElementById('preview');
                                    
                                    fetch('/admin/preview', {
                                        method: 'POST',
                                        headers: {
                                            'Content-Type': 'application/json',
                                        },
                                        body: JSON.stringify({
                                            content: content
                                        })
                                    })
                                    .then(response => {
                                        if (!response.ok) {
                                            throw new Error('Network response was not ok');
                                        }
                                        return response.text();
                                    })
                                    .then(html => {
                                        preview.innerHTML = html;
                                    })
                                    .catch(error => {
                                        console.error('Error:', error);
                                        preview.innerHTML = '<p style="color: red;">プレビューの更新中にエラーが発生しました。</p>';
                                    });
                                }
                                
                                const contentArea = document.getElementById('content');
                                if (contentArea) {
                                    contentArea.addEventListener('input', updatePreview);
                                    updatePreview();
                                }
                                
                                // 削除確認ダイアログ
                                document.querySelectorAll('.btn-delete').forEach(btn => {
                                    btn.addEventListener('click', function(e) {
                                        if (!confirm('この記事を削除してもよろしいですか？')) {
                                            e.preventDefault();
                                        }
                                    });
                                });

                                // おすすめ記事の設定
                                const checkboxes = document.querySelectorAll('.featured-checkbox');
                                checkboxes.forEach(checkbox => {
                                    checkbox.addEventListener('change', function() {
                                        const slug = this.dataset.slug;
                                        const isFeatured = this.checked;
                                        
                                        fetch('/admin/posts/' + slug + '/featured', {
                                            method: 'POST',
                                            headers: {
                                                'Content-Type': 'application/json',
                                                'Accept': 'application/json'
                                            },
                                            body: JSON.stringify({
                                                featured: isFeatured
                                            })
                                        })
                                        .then(response => {
                                            if (!response.ok) {
                                                throw new Error('Network response was not ok');
                                            }
                                            return response.json();
                                        })
                                        .then(data => {
                                            if (data.success) {
                                                console.log('Success:', data);
                                            } else {
                                                throw new Error('Server returned error');
                                            }
                                        })
                                        .catch(error => {
                                            console.error('Error:', error);
                                            // エラー時はチェックを元に戻さない（サーバー側で処理が成功しているため）
                                        });
                                    });
                                });
                            });
                            """
                        }
                    }
                }
                body {
                    h1 { +"Admin Page" }
                    p { +"Welcome to the admin area!" }
                    
                    // 記事一覧
                    div("posts-list") {
                        h2 { +"記事一覧" }
                        posts.forEach { post ->
                            div("post-item") {
                                label("featured-label") {
                                    input(type = InputType.checkBox) {
                                        classes = setOf("featured-checkbox")
                                        checked = post.isFeatured
                                        attributes["data-slug"] = post.slug
                                    }
                                    +"おすすめ"
                                }
                                a(href = "/blog/${post.slug}", classes = "post-title") {
                                    +post.title
                                }
                                div("post-actions") {
                                    a(href = "/admin/edit/${post.slug}", classes = "btn btn-edit") {
                                        +"編集"
                                    }
                                    a(href = "/admin/delete/${post.slug}", classes = "btn btn-delete") {
                                        +"削除"
                                    }
                                }
                            }
                        }
                    }
                    
                    // 新規記事作成フォーム
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
                        div("editor-container") {
                            div("editor-section") {
                                h3 { +"エディタ" }
                                textArea(rows = "15", cols = "60") {
                                    id = "content"
                                    attributes["name"] = "content"
                                    attributes["required"] = "required"
                                    attributes["placeholder"] = "# タイトル\n\nここに本文を書きます。\n\n## 見出し\n\n- リスト項目1\n- リスト項目2\n\n```kotlin\nfun main() {\n    println(\"Hello, World!\")\n}\n```"
                                }
                            }
                            div("preview-section") {
                                h3 { +"プレビュー" }
                                div("preview-content") {
                                    id = "preview"
                                }
                            }
                        }
                        p {
                            submitInput { value = "投稿" }
                        }
                    }
                }
            }
        }

        // 記事削除
        get("/admin/delete/{slug}") {
            val slug = call.parameters["slug"] ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val file = File("src/main/resources/content/$slug.md")
            
            if (file.exists()) {
                file.delete()
                call.respondRedirect("/admin")
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }

        // 記事編集
        get("/admin/edit/{slug}") {
            val slug = call.parameters["slug"] ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val file = File("src/main/resources/content/$slug.md")
            
            if (!file.exists()) {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
                return@get
            }
            
            val content = file.readText()
            val title = content.lines().firstOrNull()?.removePrefix("# ") ?: slug
            
            call.respondHtml {
                head {
                    title("Edit Post")
                    style {
                        unsafe {
                            +"""
                            body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; }
                            .editor-container { display: flex; gap: 20px; }
                            .editor-section { flex: 1; }
                            .preview-section { flex: 1; border: 1px solid #ddd; padding: 20px; border-radius: 5px; }
                            textarea { width: 100%; min-height: 400px; padding: 10px; font-family: monospace; }
                            input[type="text"] { width: 100%; padding: 8px; margin-bottom: 10px; }
                            """
                        }
                    }
                    script {
                        unsafe {
                            +"""
                            document.addEventListener('DOMContentLoaded', function() {
                                function updatePreview() {
                                    const content = document.getElementById('content').value;
                                    const preview = document.getElementById('preview');
                                    
                                    fetch('/admin/preview', {
                                        method: 'POST',
                                        headers: {
                                            'Content-Type': 'application/json',
                                        },
                                        body: JSON.stringify({
                                            content: content
                                        })
                                    })
                                    .then(response => {
                                        if (!response.ok) {
                                            throw new Error('Network response was not ok');
                                        }
                                        return response.text();
                                    })
                                    .then(html => {
                                        preview.innerHTML = html;
                                    })
                                    .catch(error => {
                                        console.error('Error:', error);
                                        preview.innerHTML = '<p style="color: red;">プレビューの更新中にエラーが発生しました。</p>';
                                    });
                                }
                                
                                const contentArea = document.getElementById('content');
                                if (contentArea) {
                                    contentArea.addEventListener('input', updatePreview);
                                    updatePreview();
                                }
                            });
                            """
                        }
                    }
                }
                body {
                    h1 { +"記事の編集" }
                    form(action = "/admin/edit/$slug", method = FormMethod.post) {
                        p {
                            label { +"タイトル: " }
                            textInput {
                                attributes["name"] = "title"
                                attributes["required"] = "required"
                                attributes["value"] = title
                            }
                        }
                        div("editor-container") {
                            div("editor-section") {
                                h3 { +"エディタ" }
                                textArea(rows = "15", cols = "60") {
                                    id = "content"
                                    attributes["name"] = "content"
                                    attributes["required"] = "required"
                                    +content
                                }
                            }
                            div("preview-section") {
                                h3 { +"プレビュー" }
                                div("preview-content") {
                                    id = "preview"
                                }
                            }
                        }
                        p {
                            submitInput { value = "更新" }
                        }
                    }
                }
            }
        }

        // 記事更新
        post("/admin/edit/{slug}") {
            val slug = call.parameters["slug"] ?: return@post call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val params = call.receiveParameters()
            val title = params["title"]?.trim() ?: ""
            val content = params["content"]?.trim() ?: ""
            
            if (title.isBlank() || content.isBlank()) {
                call.respondHtml {
                    body {
                        h1 { +"エラー" }
                        p { +"全ての項目を入力してください。" }
                        a(href = "/admin/edit/$slug") { +"戻る" }
                    }
                }
                return@post
            }
            
            val file = File("src/main/resources/content/$slug.md")
            val md = "# $title\n\n$content"
            file.writeText(md)
            
            call.respondRedirect("/admin")
        }

        post("/admin/preview") {
            val content = try {
                val json = call.receiveText()
                json.substringAfter("\"content\":\"").substringBefore("\"}")
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
            } catch (e: Exception) {
                ""
            }
            
            val extensions = listOf(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                AutolinkExtension.create(),
                TaskListItemsExtension.create()
            )
            
            val parser = Parser.builder()
                .extensions(extensions)
                .build()
            
            val renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build()
            
            val document = parser.parse(content)
            val html = renderer.render(document)
            
            call.respondText(html, ContentType.Text.Html)
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

        // おすすめ記事の設定
        post("/admin/posts/{slug}/featured") {
            val slug = call.parameters["slug"] ?: return@post call.respondText("Not Found", status = HttpStatusCode.NotFound)
            
            val json = call.receiveText()
            val featured = json.contains("\"featured\":true")
            
            val metaFile = File("src/main/resources/content/$slug.meta")
            if (featured) {
                metaFile.writeText("featured: true")
            } else {
                if (metaFile.exists()) {
                    metaFile.delete()
                }
            }
            
            call.respondText(
                """{"success":true}""",
                ContentType.Application.Json
            )
        }
    }
} 