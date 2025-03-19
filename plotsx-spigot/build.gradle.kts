plugins {
    id("com.github.johnrengelman.shadow")
    id("net.minecrell.plugin-yml.bukkit")
}

description = "PlotsX Spigot - Spigot implementation of the plugin"

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":plotsx-api"))
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
}

bukkit {
    name = "PlotsX"
    version = project.version.toString()
    main = "com.plotsx.spigot.PlotsXSpigot"
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
    shadowJar {
        archiveClassifier.set("")
        minimize()
        relocate("kotlin", "com.plotsx.libs.kotlin")
        relocate("net.kyori", "com.plotsx.libs.kyori")
    }
    
    build {
        dependsOn(shadowJar)
    }
} 