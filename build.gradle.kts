plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.plotsx"
version = "1.0-SNAPSHOT"
description = "A modern plot management plugin for Minecraft"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.codemc.org/repository/maven-public/") // CoreProtect
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    
    // Spigot API
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    
    // CoreProtect API
    compileOnly("net.coreprotect:coreprotect:22.2")
    
    // LuckPerms API
    compileOnly("net.luckperms:api:5.4")
    
    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.5")
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }
    
    shadowJar {
        archiveClassifier.set("")
        minimize()
        relocate("kotlin", "com.plotsx.kotlin")
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
} 