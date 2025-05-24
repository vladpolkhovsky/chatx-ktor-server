plugins {
    id("maven-publish")
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "by.vpolkhovsy"
version = "0.0.1"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("KtorChatDto") {
            from(components["java"])
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}