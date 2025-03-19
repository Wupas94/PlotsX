package com.plotsx.systems.visit

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class VisitManager(private val plugin: PlotsX) {
    private val ratings = mutableMapOf<UUID, MutableMap<UUID, Int>>() // Plot UUID to (Player UUID to Rating)
    private val customSpawnPoints = mutableMapOf<UUID, Location>() // Plot UUID to spawn location

    fun visitPlot(player: Player, targetPlayer: Player) {
        val plot = plugin.plotManager.getPlotAt(targetPlayer.location)
        if (plot != null) {
            val spawnPoint = customSpawnPoints[plot.id] ?: targetPlayer.location
            player.teleport(spawnPoint)
            player.sendMessage("§aPrzeteleportowano do działki gracza ${targetPlayer.name}")
        } else {
            player.sendMessage("§cGracz ${targetPlayer.name} nie posiada działki")
        }
    }

    fun ratePlot(player: Player, plot: Plot, rating: Int) {
        if (rating !in 1..5) {
            player.sendMessage("§cOcena musi być w zakresie 1-5")
            return
        }

        ratings.getOrPut(plot.id) { mutableMapOf() }[player.uniqueId] = rating
        player.sendMessage("§aOceniłeś działkę na $rating gwiazdek!")
        
        // Notify plot owner if online
        plugin.server.getPlayer(plot.owner)?.let {
            it.sendMessage("§aGracz ${player.name} ocenił twoją działkę na $rating gwiazdek!")
        }
    }

    fun getPlotRating(plot: Plot): Double {
        val plotRatings = ratings[plot.id] ?: return 0.0
        if (plotRatings.isEmpty()) return 0.0
        return plotRatings.values.average()
    }

    fun getTopPlots(limit: Int = 10): List<Pair<Plot, Double>> {
        return plugin.plotManager.getAllPlots()
            .map { it to getPlotRating(it) }
            .sortedByDescending { it.second }
            .take(limit)
    }

    fun setCustomSpawnPoint(plot: Plot, location: Location) {
        if (!plugin.plotManager.isInPlot(location, plot)) {
            throw IllegalArgumentException("Punkt spawnu musi być na działce")
        }
        customSpawnPoints[plot.id] = location
    }

    fun getCustomSpawnPoint(plot: Plot): Location? {
        return customSpawnPoints[plot.id]
    }

    // Save/load methods for persistence
    fun saveData() {
        // TODO: Implement saving ratings and spawn points to config
    }

    fun loadData() {
        // TODO: Implement loading ratings and spawn points from config
    }
} 