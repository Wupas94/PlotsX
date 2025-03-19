package com.plotsx.models

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs

data class Plot(
    val id: String,
    val owner: UUID,
    val world: String,
    val x1: Int,
    val y1: Int,
    val z1: Int,
    val x2: Int,
    val y2: Int,
    val z2: Int,
    val coowners: Set<UUID> = emptySet(),
    val flags: Map<String, Any> = emptyMap()
) {
    val center: Location by lazy {
        Location(
            org.bukkit.Bukkit.getWorld(world),
            (x1 + x2) / 2.0,
            (y1 + y2) / 2.0,
            (z1 + z2) / 2.0
        )
    }

    val corners: List<Location> by lazy {
        listOf(
            Location(org.bukkit.Bukkit.getWorld(world), x1.toDouble(), y1.toDouble(), z1.toDouble()),
            Location(org.bukkit.Bukkit.getWorld(world), x2.toDouble(), y1.toDouble(), z1.toDouble()),
            Location(org.bukkit.Bukkit.getWorld(world), x1.toDouble(), y1.toDouble(), z2.toDouble()),
            Location(org.bukkit.Bukkit.getWorld(world), x2.toDouble(), y1.toDouble(), z2.toDouble())
        )
    }

    operator fun contains(location: Location): Boolean = isInPlot(location)
    
    operator fun contains(player: Player): Boolean = isInPlot(player.location)

    fun isOwner(playerId: UUID): Boolean = owner == playerId

    fun isCoOwner(playerId: UUID): Boolean = coowners.contains(playerId)

    fun hasAccess(playerId: UUID): Boolean = isOwner(playerId) || isCoOwner(playerId)

    fun isInPlot(location: Location): Boolean {
        if (location.world?.name != world) return false
        
        return location.blockX in x1..x2 && 
               location.blockY in y1..y2 && 
               location.blockZ in z1..z2
    }

    fun getFlag(key: String): Any? = flags[key]

    fun hasFlag(key: String): Boolean = flags.containsKey(key)

    companion object {
        fun fromConfig(config: Map<String, Any>): Plot {
            return Plot(
                id = config["id"] as String,
                owner = UUID.fromString(config["owner"] as String),
                world = config["world"] as String,
                x1 = config["x1"] as Int,
                y1 = config["y1"] as Int,
                z1 = config["z1"] as Int,
                x2 = config["x2"] as Int,
                y2 = config["y2"] as Int,
                z2 = config["z2"] as Int,
                coowners = (config["coowners"] as List<String>).map { UUID.fromString(it) }.toSet(),
                flags = config["flags"] as? Map<String, Any> ?: emptyMap()
            )
        }
    }
}

// Extension functions
fun Plot.sendMessageToOwners(message: String) {
    org.bukkit.Bukkit.getWorld(world)?.let { world ->
        world.onlinePlayers
            .filter { player -> hasAccess(player.uniqueId) }
            .forEach { player -> player.sendMessage(message) }
    }
}

fun Plot.getOnlineOwners(): List<Player> {
    return org.bukkit.Bukkit.getWorld(world)?.onlinePlayers
        ?.filter { player -> hasAccess(player.uniqueId) }
        ?: emptyList()
}

fun Plot.toConfig(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "owner" to owner.toString(),
        "world" to world,
        "x1" to x1,
        "y1" to y1,
        "z1" to z1,
        "x2" to x2,
        "y2" to y2,
        "z2" to z2,
        "coowners" to coowners.map { it.toString() },
        "flags" to flags
    )
} 