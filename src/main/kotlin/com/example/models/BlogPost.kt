package com.example.models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class BlogPost(
    val title: String,
    val slug: String,
    val date: String,
    val excerpt: String
)

fun getBlogPosts(): List<BlogPost> {
    val contentDir = java.io.File("src/main/resources/content")
    if (!contentDir.exists()) return emptyList()

    return contentDir.listFiles { file -> file.extension == "md" }
        ?.map { file ->
            val content = file.readText()
            val firstLine = content.lines().firstOrNull() ?: ""
            val title = if (firstLine.startsWith("# ")) firstLine.substring(2) else file.nameWithoutExtension
            val excerpt = content.lines()
                .drop(1)
                .take(3)
                .joinToString(" ")
                .take(150) + "..."
            
            BlogPost(
                title = title,
                slug = file.nameWithoutExtension,
                date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                excerpt = excerpt
            )
        }
        ?.sortedByDescending { it.date }
        ?: emptyList()
} 