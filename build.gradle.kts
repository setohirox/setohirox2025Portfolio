plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    application
}

group = "com.setohirox"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.html.builder)
    implementation("org.commonmark:commonmark:0.21.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.21.0")
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.21.0")
    implementation("org.commonmark:commonmark-ext-autolink:0.21.0")
    implementation("org.commonmark:commonmark-ext-task-list-items:0.21.0")
    implementation("io.ktor:ktor-server-rate-limit:2.3.7")
    implementation("io.ktor:ktor-server-sessions:2.3.7")
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

// .envファイルを読み込む
val envFile = file(".env")
if (!envFile.exists()) {
    throw GradleException(".env file not found. Please create .env file with ADMIN_USER and ADMIN_PASSWORD")
}

envFile.readLines().forEach { line ->
    if (line.isNotBlank() && !line.startsWith("#")) {
        val (key, value) = line.split("=", limit = 2)
        System.setProperty(key.trim(), value.trim())
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
    
    create("stage") {
        dependsOn("installDist")
    }

    // 本番用のfat jarを作成するタスク
    register<Jar>("fatJar") {
        group = "build"
        description = "Assembles a fat jar"
        manifest {
            attributes["Main-Class"] = "com.setohirox.ApplicationKt"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map { zipTree(it) }
        })
        archiveFileName.set("setohirox2025Portfolio-fat.jar")
    }

    withType<JavaExec> {
        environment(System.getProperties().mapKeys { it.key.toString() })
    }
}

ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
    docker {
        localImageName.set("setohirox2025Portfolio")
        imageTag.set("0.0.1")
    }
}
