plugins {
    kotlin("jvm") version "1.9.22" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("xyz.jpenilla.run-paper") version "2.2.2" apply false
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    
    group = "com.plotsx"
    version = "1.0-SNAPSHOT"
    
    repositories {
        mavenCentral()
        maven("https://repo.codemc.org/repository/maven-public/") // CoreProtect
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    }
    
    dependencies {
        "implementation"(kotlin("stdlib"))
        
        // CoreProtect API
        "compileOnly"("net.coreprotect:coreprotect:22.2")
        
        // LuckPerms API
        "compileOnly"("net.luckperms:api:5.4")
        
        // PlaceholderAPI
        "compileOnly"("me.clip:placeholderapi:2.11.5")
    }
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
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