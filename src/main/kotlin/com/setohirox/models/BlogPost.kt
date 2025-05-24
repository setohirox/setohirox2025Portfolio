package com.setohirox.models

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class BlogPost(
    val title: String,
    val slug: String,
    val date: String,
    val excerpt: String,
    val isFeatured: Boolean = false
)

fun getBlogPosts(): List<BlogPost> {
    val contentDir = File("src/main/resources/content")
    if (!contentDir.exists()) {
        contentDir.mkdirs()
        return emptyList()
    }
    
    return contentDir.listFiles { file -> file.isFile && file.extension == "md" }
        ?.map { file ->
            val content = file.readText()
            val lines = content.lines()
            val title = lines.firstOrNull()?.removePrefix("# ") ?: file.nameWithoutExtension
            val excerpt = lines.drop(1).take(3).joinToString(" ")
            
            // メタデータファイルからおすすめフラグを読み込む
            val metaFile = File("src/main/resources/content/${file.nameWithoutExtension}.meta")
            val isFeatured = if (metaFile.exists()) {
                metaFile.readText().contains("featured: true")
            } else {
                false
            }
            
            BlogPost(
                title = title,
                slug = file.nameWithoutExtension,
                date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                excerpt = excerpt,
                isFeatured = isFeatured
            )
        }
        ?.sortedByDescending { it.date }
        ?: emptyList()
}

fun getFeaturedPosts(): List<BlogPost> {
    return getBlogPosts().filter { it.isFeatured }
} 