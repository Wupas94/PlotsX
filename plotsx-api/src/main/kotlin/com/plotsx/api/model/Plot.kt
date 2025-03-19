package com.plotsx.api.model

import com.plotsx.api.PlotsXPlugin
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

data class Plot(
    val id: String,
    val owner: UUID,
    val center: Location,
    val x1: Int,
    val y1: Int,
    val z1: Int,
    val x2: Int,
    val y2: Int,
    val z2: Int,
    val coowners: Set<UUID> = emptySet(),
    val flags: Map<String, Any> = emptyMap()
) {
    val creationDate: Long = System.currentTimeMillis()
    
    var isActive: Boolean = true
    var isPvpEnabled: Boolean = false
    var isMobDamageEnabled: Boolean = false
    var isBasicInteractionsEnabled: Boolean = true

    val corners: List<Location> by lazy {
        listOf(
            Location(center.world, x1.toDouble(), y1.toDouble(), z1.toDouble()),
            Location(center.world, x2.toDouble(), y1.toDouble(), z1.toDouble()),
            Location(center.world, x1.toDouble(), y1.toDouble(), z2.toDouble()),
            Location(center.world, x2.toDouble(), y1.toDouble(), z2.toDouble())
        )
    }

    operator fun contains(location: Location): Boolean = isInPlot(location)
    operator fun contains(player: Player): Boolean = isInPlot(player.location)

    fun isOwner(playerId: UUID): Boolean = owner == playerId
    fun isCoOwner(playerId: UUID): Boolean = playerId in coowners
    fun hasAccess(playerId: UUID): Boolean =
        owner == playerId || coowners.contains(playerId)

    fun addCoOwner(playerId: UUID) = coowners.add(playerId)
    fun removeCoOwner(playerId: UUID) = coowners.remove(playerId)

    fun isInPlot(location: Location): Boolean =
        location.world == center.world &&
        location.blockX in x1..x2 &&
        location.blockY in y1..y2 &&
        location.blockZ in z1..z2

    companion object {
        fun fromConfig(config: Map<String, Any>): Plot {
            val owner = UUID.fromString(config["owner"] as String)
            val center = config["center"] as Location
            val x1 = config["x1"] as Int
            val y1 = config["y1"] as Int
            val z1 = config["z1"] as Int
            val x2 = config["x2"] as Int
            val y2 = config["y2"] as Int
            val z2 = config["z2"] as Int
            val coowners = (config["coowners"] as List<String>).map { UUID.fromString(it) }.toSet()
            val flags = config["flags"] as? Map<String, Any> ?: emptyMap()

            return Plot(
                id = config["id"] as String,
                owner = owner,
                center = center,
                x1 = x1,
                y1 = y1,
                z1 = z1,
                x2 = x2,
                y2 = y2,
                z2 = z2,
                coowners = coowners,
                flags = flags
            )
        }
    }

    fun toConfig(): Map<String, Any> = mapOf(
        "id" to id,
        "owner" to owner.toString(),
        "center" to center,
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

// Extension functions
fun Plot.toConfig(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "owner" to owner.toString(),
        "center" to center,
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