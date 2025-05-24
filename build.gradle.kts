import org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain

val h2_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val exposed_dao_version: String by project

plugins {
    id("maven-publish")
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "by.vpolkhovsy"
version = "1.0.0"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
    val isDevelopment: Boolean = project.ext.has("development")

    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}


val service = project.extensions.getByType<JavaToolchainService>()

val customLauncher = service.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(22))
}

project.tasks.withType<UsesKotlinJavaToolchain>().configureEach {
    kotlinJavaToolchain.toolchain.use(customLauncher)
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-sse")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_dao_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_dao_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_dao_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_dao_version")
    implementation("org.springframework.security:spring-security-crypto:6.4.4")
    implementation("commons-logging:commons-logging:1.2")
    implementation(project(":ktor-chat-web-server-dto"))
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
