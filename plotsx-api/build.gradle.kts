description = "PlotsX API - shared code between implementations"

dependencies {
    api("net.kyori:adventure-api:4.15.0")
    api("net.kyori:adventure-text-minimessage:4.15.0")
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
} 