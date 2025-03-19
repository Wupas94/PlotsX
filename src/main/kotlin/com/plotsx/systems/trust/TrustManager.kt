package com.plotsx.systems.trust

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.entity.Player
import java.util.*

class TrustManager(private val plugin: PlotsX) {
    private val trustedPlayers = mutableMapOf<UUID, MutableMap<UUID, TrustLevel>>() // Plot UUID to (Player UUID to TrustLevel)
    private val temporaryTrust = mutableMapOf<UUID, MutableMap<UUID, Long>>() // Plot UUID to (Player UUID to Expiration Time)

    fun trustPlayer(plot: Plot, player: UUID, level: TrustLevel) {
        trustedPlayers.getOrPut(plot.id) { mutableMapOf() }[player] = level
    }

    fun trustPlayerTemporarily(plot: Plot, player: UUID, level: TrustLevel, durationMinutes: Long) {
        trustPlayer(plot, player, level)
        temporaryTrust.getOrPut(plot.id) { mutableMapOf() }[player] = 
            System.currentTimeMillis() + (durationMinutes * 60 * 1000)
    }

    fun untrustPlayer(plot: Plot, player: UUID) {
        trustedPlayers[plot.id]?.remove(player)
        temporaryTrust[plot.id]?.remove(player)
    }

    fun getTrustLevel(plot: Plot, player: UUID): TrustLevel {
        // Check if trust has expired
        temporaryTrust[plot.id]?.let { tempTrust ->
            tempTrust[player]?.let { expirationTime ->
                if (System.currentTimeMillis() > expirationTime) {
                    untrustPlayer(plot, player)
                    return TrustLevel.NONE
                }
            }
        }

        return trustedPlayers[plot.id]?.get(player) ?: TrustLevel.NONE
    }

    fun canPlayerBuild(plot: Plot, player: Player): Boolean {
        if (player.uniqueId == plot.owner) return true
        return when (getTrustLevel(plot, player.uniqueId)) {
            TrustLevel.BUILD, TrustLevel.MANAGE -> true
            else -> false
        }
    }

    fun canPlayerManage(plot: Plot, player: Player): Boolean {
        if (player.uniqueId == plot.owner) return true
        return getTrustLevel(plot, player.uniqueId) == TrustLevel.MANAGE
    }

    fun getPlotTrustedPlayers(plot: Plot): Map<UUID, TrustLevel> {
        return trustedPlayers[plot.id]?.filter { (playerId, _) ->
            temporaryTrust[plot.id]?.get(playerId)?.let { expirationTime ->
                System.currentTimeMillis() <= expirationTime
            } ?: true
        } ?: emptyMap()
    }

    fun cleanupExpiredTrust() {
        val currentTime = System.currentTimeMillis()
        temporaryTrust.forEach { (plotId, players) ->
            players.entries.removeIf { (playerId, expirationTime) ->
                if (currentTime > expirationTime) {
                    trustedPlayers[plotId]?.remove(playerId)
                    true
                } else false
            }
        }
    }

    // Save/load methods for persistence
    fun saveData() {
        // TODO: Implement saving trust data to config
    }

    fun loadData() {
        // TODO: Implement loading trust data from config
    }
}

enum class TrustLevel {
    NONE,       // No special permissions
    VISIT,      // Can visit when plot is closed
    USE,        // Can use buttons, levers, etc.
    BUILD,      // Can build and break blocks
    MANAGE      // Can manage plot settings and other players
} 