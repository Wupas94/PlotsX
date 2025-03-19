package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import java.util.*

class WorldManager(private val plugin: PlotsX) {
    private val plotTime = mutableMapOf<UUID, Long>()
    private val plotBiome = mutableMapOf<UUID, Biome>()
    private val playerTime = mutableMapOf<UUID, UUID>() // Player UUID -> Plot UUID
    private val plotWorlds = mutableSetOf<World>()

    init {
        loadPlotWorlds()
    }

    private fun loadPlotWorlds() {
        val worldNames = plugin.config.getStringList("plot-worlds")
        worldNames.forEach { name ->
            plugin.server.getWorld(name)?.let { world ->
                plotWorlds.add(world)
                plugin.logger.info("Loaded plot world: ${world.name}")
            } ?: plugin.logger.warning("Could not load plot world: $name")
        }
    }

    fun isPlotWorld(world: World): Boolean = plotWorlds.contains(world)

    fun getPlotWorlds(): Set<World> = plotWorlds.toSet()

    fun setPlotTime(plot: Plot, time: Long) {
        plotTime[plot.id] = time
        savePlotTime(plot.id)
        updatePlayersInPlot(plot)
    }

    fun getPlotTime(plot: Plot): Long? {
        return plotTime[plot.id]
    }

    fun setPlotBiome(plot: Plot, biome: Biome) {
        plotBiome[plot.id] = biome
        savePlotBiome(plot.id)
        updatePlotBiome(plot)
    }

    fun getPlotBiome(plot: Plot): Biome? {
        return plotBiome[plot.id]
    }

    fun updatePlayerTime(player: Player) {
        val plot = plugin.plotManager.getPlotAt(player.location)
        if (plot == null) {
            player.resetPlayerTime()
            playerTime.remove(player.uniqueId)
            return
        }

        val time = plotTime[plot.id] ?: player.world.time
        player.setPlayerTime(time, false)
        playerTime[player.uniqueId] = plot.id
    }

    private fun updatePlayersInPlot(plot: Plot) {
        val time = plotTime[plot.id] ?: return
        plugin.server.onlinePlayers.forEach { player ->
            if (plugin.plotManager.isLocationInPlot(player.location, plot)) {
                player.setPlayerTime(time, false)
                playerTime[player.uniqueId] = plot.id
            }
        }
    }

    private fun updatePlotBiome(plot: Plot) {
        val biome = plotBiome[plot.id] ?: return
        val world = plot.world
        val width = plot.x2 - plot.x1
        val height = plot.z2 - plot.z1

        for (x in plot.x1..plot.x2) {
            for (z in plot.z1..plot.z2) {
                world.setBiome(x, z, biome)
            }
        }
    }

    fun loadPlotTime(plotId: UUID) {
        val config = plugin.config
        val path = "time.$plotId"
        if (!config.contains(path)) return

        plotTime[plotId] = config.getLong(path)
    }

    private fun savePlotTime(plotId: UUID) {
        val config = plugin.config
        val path = "time.$plotId"
        plotTime[plotId]?.let { time ->
            config.set(path, time)
        } ?: config.set(path, null)
        plugin.saveConfig()
    }

    fun loadPlotBiome(plotId: UUID) {
        val config = plugin.config
        val path = "biome.$plotId"
        if (!config.contains(path)) return

        val biomeName = config.getString(path)
        if (biomeName != null) {
            try {
                plotBiome[plotId] = Biome.valueOf(biomeName)
            } catch (e: IllegalArgumentException) {
                plugin.logger.warning("Invalid biome type for plot $plotId: $biomeName")
            }
        }
    }

    private fun savePlotBiome(plotId: UUID) {
        val config = plugin.config
        val path = "biome.$plotId"
        plotBiome[plotId]?.let { biome ->
            config.set(path, biome.name)
        } ?: config.set(path, null)
        plugin.saveConfig()
    }

    fun onPlayerMove(player: Player) {
        val currentPlotId = playerTime[player.uniqueId]
        val newPlot = plugin.plotManager.getPlotAt(player.location)

        if (currentPlotId != newPlot?.id) {
            updatePlayerTime(player)
        }
    }

    fun isLocationInPlot(location: Location, plot: Plot): Boolean {
        if (location.world != plot.world) return false
        
        val x = location.blockX
        val z = location.blockZ
        
        return x in plot.x1..plot.x2 && z in plot.z1..plot.z2
    }

    fun getPlotAt(location: Location): Plot? {
        return plugin.plotManager.getPlotAt(location)
    }

    fun getPlotSize(plot: Plot): Pair<Int, Int> {
        val width = plot.x2 - plot.x1 + 1
        val height = plot.z2 - plot.z1 + 1
        return Pair(width, height)
    }

    fun getPlotArea(plot: Plot): Int {
        val (width, height) = getPlotSize(plot)
        return width * height
    }

    fun getPlotPerimeter(plot: Plot): Int {
        val (width, height) = getPlotSize(plot)
        return 2 * (width + height)
    }

    fun getPlotCenter(plot: Plot): Location {
        val x = (plot.x1 + plot.x2) / 2.0
        val y = plot.y1.toDouble()
        val z = (plot.z1 + plot.z2) / 2.0
        return Location(plot.world, x, y, z)
    }

    fun teleportToPlot(player: Player, plot: Plot) {
        val center = getPlotCenter(plot)
        center.y = plot.world.getHighestBlockYAt(center.blockX, center.blockZ) + 1.0
        player.teleport(center)
    }
} 