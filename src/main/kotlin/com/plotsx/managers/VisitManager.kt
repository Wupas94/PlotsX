package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class VisitManager(private val plugin: PlotsX) {
    private val visitHistory = mutableMapOf<UUID, MutableList<UUID>>()
    private val ratings = mutableMapOf<UUID, MutableMap<UUID, Int>>()
    private val lastVisit = mutableMapOf<UUID, Long>()

    fun visitPlot(visitor: Player, target: Player) {
        val plot = plugin.plotManager.getPlayerPlot(target.uniqueId)
        if (plot == null) {
            visitor.sendMessage("${ChatColor.RED}Ten gracz nie ma założonej działki!")
            return
        }

        if (!hasTrust(plot, visitor.uniqueId)) {
            visitor.sendMessage("${ChatColor.RED}Nie masz uprawnień do odwiedzenia tej działki!")
            return
        }

        val now = System.currentTimeMillis()
        val lastVisitTime = lastVisit[visitor.uniqueId] ?: 0
        if (now - lastVisitTime < plugin.config.getLong("visit.cooldown", 30000)) {
            val remainingTime = (plugin.config.getLong("visit.cooldown", 30000) - (now - lastVisitTime)) / 1000
            visitor.sendMessage("${ChatColor.RED}Musisz poczekać jeszcze ${remainingTime}s przed kolejną wizytą!")
            return
        }

        val plotCenter = plot.center
        val safeLocation = findSafeLocation(plotCenter)
        if (safeLocation == null) {
            visitor.sendMessage("${ChatColor.RED}Nie znaleziono bezpiecznej lokalizacji na działce!")
            return
        }

        visitor.teleport(safeLocation)
        addVisit(visitor.uniqueId, plot.id)
        lastVisit[visitor.uniqueId] = now

        visitor.sendMessage("${ChatColor.GREEN}Przeteleportowano na działkę gracza ${target.name}!")
        target.sendMessage("${ChatColor.GREEN}Gracz ${visitor.name} odwiedził twoją działkę!")
    }

    fun ratePlot(rater: Player, target: Player, rating: Int) {
        val plot = plugin.plotManager.getPlayerPlot(target.uniqueId)
        if (plot == null) {
            rater.sendMessage("${ChatColor.RED}Ten gracz nie ma założonej działki!")
            return
        }

        if (!hasVisited(rater.uniqueId, plot.id)) {
            rater.sendMessage("${ChatColor.RED}Musisz najpierw odwiedzić działkę, aby ją ocenić!")
            return
        }

        if (rating !in 1..5) {
            rater.sendMessage("${ChatColor.RED}Ocena musi być w zakresie od 1 do 5!")
            return
        }

        val plotRatings = ratings.getOrPut(plot.id) { mutableMapOf() }
        plotRatings[rater.uniqueId] = rating
        saveRatings(plot.id)

        rater.sendMessage("${ChatColor.GREEN}Oceniłeś działkę gracza ${target.name} na $rating gwiazdek!")
        target.sendMessage("${ChatColor.GREEN}Gracz ${rater.name} ocenił twoją działkę na $rating gwiazdek!")
    }

    fun getPlotRating(plot: Plot): Double {
        val plotRatings = ratings[plot.id] ?: return 0.0
        if (plotRatings.isEmpty()) return 0.0
        return plotRatings.values.average()
    }

    fun getVisitCount(playerId: UUID, plotId: UUID): Int {
        return visitHistory[playerId]?.count { it == plotId } ?: 0
    }

    private fun hasVisited(playerId: UUID, plotId: UUID): Boolean {
        return visitHistory[playerId]?.contains(plotId) ?: false
    }

    private fun addVisit(playerId: UUID, plotId: UUID) {
        val visits = visitHistory.getOrPut(playerId) { mutableListOf() }
        visits.add(plotId)
        if (visits.size > plugin.config.getInt("visit.max-history", 100)) {
            visits.removeAt(0)
        }
        saveVisitHistory(playerId)
    }

    private fun hasTrust(plot: Plot, playerId: UUID): Boolean {
        return plugin.trustManager.hasTrust(plot, playerId, TrustLevel.VISIT)
    }

    private fun findSafeLocation(center: Location): Location? {
        val world = center.world ?: return null
        val x = center.blockX
        val z = center.blockZ
        val maxY = world.maxHeight - 1

        // Try to find a safe location within a 5x5 area around the center
        for (dx in -2..2) {
            for (dz in -2..2) {
                for (y in maxY downTo 0) {
                    val loc = Location(world, x + dx + 0.5, y.toDouble(), z + dz + 0.5)
                    if (isSafeLocation(loc)) {
                        return loc
                    }
                }
            }
        }
        return null
    }

    private fun isSafeLocation(loc: Location): Boolean {
        val block = loc.block
        val below = loc.clone().subtract(0.0, 1.0, 0.0).block
        val above = loc.clone().add(0.0, 1.0, 0.0).block

        return !block.type.isSolid && !above.type.isSolid && below.type.isSolid
    }

    fun loadVisitHistory(playerId: UUID) {
        val config = plugin.config
        val path = "visits.$playerId"
        if (!config.contains(path)) return

        val visits = mutableListOf<UUID>()
        config.getStringList(path).forEach { plotId ->
            UUID.fromString(plotId)?.let { visits.add(it) }
        }
        visitHistory[playerId] = visits
    }

    private fun saveVisitHistory(playerId: UUID) {
        val config = plugin.config
        val path = "visits.$playerId"
        config.set(path, visitHistory[playerId]?.map { it.toString() } ?: emptyList())
        plugin.saveConfig()
    }

    fun loadRatings(plotId: UUID) {
        val config = plugin.config
        val path = "ratings.$plotId"
        if (!config.contains(path)) return

        val plotRatings = mutableMapOf<UUID, Int>()
        config.getConfigurationSection(path)?.getKeys(false)?.forEach { playerId ->
            val rating = config.getInt("$path.$playerId")
            plotRatings[UUID.fromString(playerId)] = rating
        }
        ratings[plotId] = plotRatings
    }

    private fun saveRatings(plotId: UUID) {
        val config = plugin.config
        val path = "ratings.$plotId"
        config.set(path, null)

        ratings[plotId]?.forEach { (playerId, rating) ->
            config.set("$path.$playerId", rating)
        }
        plugin.saveConfig()
    }
} 