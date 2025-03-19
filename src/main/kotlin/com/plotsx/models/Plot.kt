package com.plotsx.models

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs

data class Plot(
    val owner: UUID,
    val center: Location,
    val radius: Int
) {
    val id: UUID = UUID.randomUUID()
    val coOwners: MutableSet<UUID> = mutableSetOf()
    val creationDate: Long = System.currentTimeMillis()
    
    var isActive: Boolean = true
    var isPvpEnabled: Boolean = false
    var isMobDamageEnabled: Boolean = false
    var isBasicInteractionsEnabled: Boolean = true

    val corners: List<Location> by lazy {
        listOf(
            center.clone().add(radius.toDouble(), 0.0, radius.toDouble()),
            center.clone().add(-radius.toDouble(), 0.0, radius.toDouble()),
            center.clone().add(radius.toDouble(), 0.0, -radius.toDouble()),
            center.clone().add(-radius.toDouble(), 0.0, -radius.toDouble())
        )
    }

    operator fun contains(location: Location): Boolean = isInPlot(location)
    
    operator fun contains(player: Player): Boolean = isInPlot(player.location)

    fun isOwner(playerId: UUID): Boolean = owner == playerId

    fun isCoOwner(playerId: UUID): Boolean = playerId in coOwners

    fun hasAccess(playerId: UUID): Boolean = isOwner(playerId) || isCoOwner(playerId)

    fun addCoOwner(playerId: UUID) = coOwners.add(playerId)

    fun removeCoOwner(playerId: UUID) = coOwners.remove(playerId)

    fun isInPlot(location: Location): Boolean {
        if (location.world != center.world) return false
        
        return abs(location.x - center.x) <= radius && 
               abs(location.z - center.z) <= radius
    }

    companion object {
        fun fromConfig(config: Map<String, Any>): Plot {
            return Plot(
                owner = UUID.fromString(config["owner"] as String),
                center = config["center"] as Location,
                radius = config["radius"] as Int
            ).apply {
                isActive = config["isActive"] as Boolean
                isPvpEnabled = config["isPvpEnabled"] as Boolean
                isMobDamageEnabled = config["isMobDamageEnabled"] as Boolean
                isBasicInteractionsEnabled = config["isBasicInteractionsEnabled"] as Boolean
                (config["coOwners"] as List<String>).forEach { 
                    coOwners.add(UUID.fromString(it)) 
                }
            }
        }
    }
}

// Extension functions
fun Plot.sendMessageToOwners(message: String) {
    center.world?.let { world ->
        world.onlinePlayers
            .filter { player -> hasAccess(player.uniqueId) }
            .forEach { player -> player.sendMessage(message) }
    }
}

fun Plot.getOnlineOwners(): List<Player> {
    return center.world?.onlinePlayers
        ?.filter { player -> hasAccess(player.uniqueId) }
        ?: emptyList()
}

fun Plot.toConfig(): Map<String, Any> {
    return mapOf(
        "owner" to owner.toString(),
        "center" to center,
        "radius" to radius,
        "isActive" to isActive,
        "isPvpEnabled" to isPvpEnabled,
        "isMobDamageEnabled" to isMobDamageEnabled,
        "isBasicInteractionsEnabled" to isBasicInteractionsEnabled,
        "coOwners" to coOwners.map { it.toString() }
    )
} 