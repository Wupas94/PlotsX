plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.2" // Plugin do testowania
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Generator plugin.yml
}

group = "com.plotsx"
version = "1.0-SNAPSHOT"
description = "A modern plot management plugin for Minecraft"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // Paper API
    maven("https://repo.codemc.org/repository/maven-public/") // CoreProtect
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    
    // Paper API (zamiast Spigot)
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    
    // Adventure API (nowoczesny system wiadomości)
    implementation("net.kyori:adventure-api:4.15.0")
    implementation("net.kyori:adventure-text-minimessage:4.15.0")
    
    // CoreProtect API
    compileOnly("net.coreprotect:coreprotect:22.2")
    
    // LuckPerms API
    compileOnly("net.luckperms:api:5.4")
    
    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.5")
}

// Konfiguracja plugin.yml
bukkit {
    name = "PlotsX"
    version = project.version.toString()
    main = "com.plotsx.PlotsX"
    apiVersion = "1.20"
    authors = listOf("Wupas94")
    
    depend = listOf("CoreProtect")
    softDepend = listOf("LuckPerms", "PlaceholderAPI")
    
    commands {
        register("plot") {
            description = "Główna komenda pluginu PlotsX"
            aliases = listOf("dzialka", "działka", "p")
            permission = "plotsx.use"
            usage = "/plot <akcja>"
        }
    }
    
    permissions {
        register("plotsx.use") {
            description = "Pozwala na używanie podstawowych komend pluginu"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("plotsx.admin") {
            description = "Dostęp do komend administracyjnych"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
    }
}

tasks {
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }
    
    shadowJar {
        archiveClassifier.set("")
        minimize()
        relocate("kotlin", "com.plotsx.kotlin")
        relocate("net.kyori", "com.plotsx.libs.kyori")
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
    
    // Konfiguracja serwera testowego
    runServer {
        minecraftVersion("1.20.4")
        jvmArgs("-Xms2G", "-Xmx2G")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
} 