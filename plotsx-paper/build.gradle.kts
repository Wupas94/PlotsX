plugins {
    id("com.github.johnrengelman.shadow")
    id("xyz.jpenilla.run-paper")
    id("net.minecrell.plugin-yml.bukkit")
}

description = "PlotsX Paper - Paper implementation of the plugin"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":plotsx-api"))
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
}

bukkit {
    name = "PlotsX"
    version = project.version.toString()
    main = "com.plotsx.paper.PlotsXPaper"
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
    
    runServer {
        minecraftVersion("1.20.4")
        jvmArgs("-Xms2G", "-Xmx2G")
    }
} 