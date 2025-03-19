package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import com.plotsx.models.TrustLevel
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

class TrustManager(private val plugin: PlotsX) {
    private val trustLevels = mutableMapOf<UUID, MutableMap<UUID, TrustLevel>>()

    fun trustPlayer(plot: Plot, playerId: UUID, level: TrustLevel) {
        val plotTrusts = trustLevels.getOrPut(plot.id) { mutableMapOf() }
        plotTrusts[playerId] = level
        saveTrusts(plot.id)
    }

    fun untrustPlayer(plot: Plot, playerId: UUID) {
        val plotTrusts = trustLevels[plot.id] ?: return
        plotTrusts.remove(playerId)
        if (plotTrusts.isEmpty()) {
            trustLevels.remove(plot.id)
        }
        saveTrusts(plot.id)
    }

    fun getTrustLevel(plot: Plot, playerId: UUID): TrustLevel {
        if (playerId == plot.owner) return TrustLevel.OWNER
        return trustLevels[plot.id]?.get(playerId) ?: TrustLevel.NONE
    }

    fun hasTrust(plot: Plot, playerId: UUID, requiredLevel: TrustLevel): Boolean {
        return getTrustLevel(plot, playerId).ordinal >= requiredLevel.ordinal
    }

    fun getTrustedPlayers(plot: Plot): Map<UUID, TrustLevel> {
        return trustLevels[plot.id] ?: emptyMap()
    }

    fun loadTrusts(plotId: UUID) {
        val config = plugin.config
        val path = "trusts.$plotId"
        if (!config.contains(path)) return

        val plotTrusts = mutableMapOf<UUID, TrustLevel>()
        config.getConfigurationSection(path)?.getKeys(false)?.forEach { playerId ->
            val level = config.getString("$path.$playerId")?.let { TrustLevel.valueOf(it) } ?: TrustLevel.NONE
            plotTrusts[UUID.fromString(playerId)] = level
        }
        trustLevels[plotId] = plotTrusts
    }

    private fun saveTrusts(plotId: UUID) {
        val config = plugin.config
        val path = "trusts.$plotId"
        config.set(path, null)

        trustLevels[plotId]?.forEach { (playerId, level) ->
            config.set("$path.$playerId", level.name)
        }
        plugin.saveConfig()
    }

    fun sendTrustList(player: Player, plot: Plot) {
        val trustedPlayers = getTrustedPlayers(plot)
        if (trustedPlayers.isEmpty()) {
            player.sendMessage("${ChatColor.YELLOW}Ta działka nie ma zaufanych graczy.")
            return
        }

        player.sendMessage("${ChatColor.GOLD}=== Zaufani gracze działki ===")
        trustedPlayers.forEach { (playerId, level) ->
            val trustedPlayer = plugin.server.getOfflinePlayer(playerId)
            player.sendMessage("${ChatColor.YELLOW}- ${trustedPlayer.name}: ${ChatColor.WHITE}${level.name}")
        }
    }
} 