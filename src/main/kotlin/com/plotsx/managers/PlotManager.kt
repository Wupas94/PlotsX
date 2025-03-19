package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

class PlotManager(private val plugin: PlotsX) {
    private val plots: MutableMap<UUID, Plot> = mutableMapOf()
    private val plotFile: File = File(plugin.dataFolder, "plots.yml")
    private val plotConfig: FileConfiguration = YamlConfiguration.loadConfiguration(plotFile)
    private val defaultWidth = plugin.config.getInt("plot.default-width", 32)
    private val defaultHeight = plugin.config.getInt("plot.default-height", 32)

    init {
        loadPlots()
    }

    private fun loadPlots() {
        if (!plotFile.exists()) {
            plugin.saveResource("plots.yml", false)
        }

        val plotsSection = plotConfig.getConfigurationSection("plots")
        if (plotsSection != null) {
            for (plotId in plotsSection.getKeys(false)) {
                val section = plotsSection.getConfigurationSection(plotId) ?: continue
                val plot = loadPlot(section)
                plot?.let { plots[it.id] = it }
            }
        }
    }

    private fun loadPlot(section: ConfigurationSection): Plot? {
        return try {
            val owner = UUID.fromString(section.getString("owner"))
            val world = section.getString("world") ?: return null
            val x = section.getDouble("x")
            val y = section.getDouble("y")
            val z = section.getDouble("z")
            val center = Location(plugin.server.getWorld(world), x, y, z)
            val x1 = section.getInt("x1")
            val y1 = section.getInt("y1")
            val z1 = section.getInt("z1")
            val x2 = section.getInt("x2")
            val y2 = section.getInt("y2")
            val z2 = section.getInt("z2")
            val coOwners = section.getStringList("co-owners").map { UUID.fromString(it) }.toSet()
            val active = section.getBoolean("active", true)
            val pvp = section.getBoolean("pvp", false)
            val mobDamage = section.getBoolean("mob-damage", false)
            val basicInteractions = section.getBoolean("basic-interactions", true)
            val createdAt = section.getLong("created-at", System.currentTimeMillis())
            val lastModified = section.getLong("last-modified", System.currentTimeMillis())

            Plot(
                owner = owner,
                center = center,
                x1 = x1,
                y1 = y1,
                z1 = z1,
                x2 = x2,
                y2 = y2,
                z2 = z2,
                coowners = coOwners,
                isActive = active,
                isPvpEnabled = pvp,
                isMobDamageEnabled = mobDamage,
                isBasicInteractionsEnabled = basicInteractions,
                createdAt = createdAt,
                lastModified = lastModified
            )
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load plot: ${e.message}")
            null
        }
    }

    private fun savePlot(plot: Plot) {
        plotConfig.set("plots.${plot.id}.owner", plot.owner.toString())
        plotConfig.set("plots.${plot.id}.world", plot.center.world.name)
        plotConfig.set("plots.${plot.id}.x", plot.center.x)
        plotConfig.set("plots.${plot.id}.y", plot.center.y)
        plotConfig.set("plots.${plot.id}.z", plot.center.z)
        plotConfig.set("plots.${plot.id}.x1", plot.x1)
        plotConfig.set("plots.${plot.id}.y1", plot.y1)
        plotConfig.set("plots.${plot.id}.z1", plot.z1)
        plotConfig.set("plots.${plot.id}.x2", plot.x2)
        plotConfig.set("plots.${plot.id}.y2", plot.y2)
        plotConfig.set("plots.${plot.id}.z2", plot.z2)
        plotConfig.set("plots.${plot.id}.co-owners", plot.coowners.map { it.toString() })
        plotConfig.set("plots.${plot.id}.active", plot.isActive)
        plotConfig.set("plots.${plot.id}.pvp", plot.isPvpEnabled)
        plotConfig.set("plots.${plot.id}.mob-damage", plot.isMobDamageEnabled)
        plotConfig.set("plots.${plot.id}.basic-interactions", plot.isBasicInteractionsEnabled)
        plotConfig.set("plots.${plot.id}.created-at", plot.createdAt)
        plotConfig.set("plots.${plot.id}.last-modified", plot.lastModified)
        plotConfig.save(plotFile)
    }

    fun saveAllPlots() {
        plots.values.forEach { savePlot(it) }
    }

    fun createPlot(player: Player, center: Location): Result<Plot> {
        return when {
            getPlayerPlot(player.uniqueId) != null -> 
                failure(IllegalStateException("Player already has a plot"))
            getPlotAt(center) != null -> 
                failure(IllegalStateException("Location is already in a plot"))
            else -> {
                val x1 = center.blockX - defaultWidth / 2
                val y1 = 0
                val z1 = center.blockZ - defaultHeight / 2
                val x2 = center.blockX + defaultWidth / 2
                val y2 = 255
                val z2 = center.blockZ + defaultHeight / 2
                
                Plot(
                    id = UUID.randomUUID().toString(),
                    owner = player.uniqueId,
                    center = center,
                    x1 = x1,
                    y1 = y1,
                    z1 = z1,
                    x2 = x2,
                    y2 = y2,
                    z2 = z2
                ).let { plot ->
                    plots[plot.id] = plot
                    saveAllPlots()
                    success(plot)
                }
            }
        }
    }

    fun deletePlot(plotId: String): Boolean {
        return plots.remove(plotId)?.let {
            saveAllPlots()
            true
        } ?: false
    }

    fun getPlot(plotId: String): Plot? = plots[plotId]

    fun getPlotAt(location: Location): Plot? =
        plots.values.firstOrNull { it.isInPlot(location) }

    fun getPlayerPlot(playerId: UUID): Plot? =
        plots.values.firstOrNull { it.owner == playerId }

    fun getAllPlots(): List<Plot> = plots.values.toList()

    fun getNearbyPlots(location: Location, distance: Int): List<Plot> =
        plots.values.filter { plot ->
            val plotCenter = plot.center
            val plotWidth = plot.x2 - plot.x1
            val plotHeight = plot.z2 - plot.z1
            val maxDimension = maxOf(plotWidth, plotHeight) / 2
            location.distance(plotCenter) <= (distance + maxDimension)
        }

    fun getDefaultDimensions(): Pair<Int, Int> = Pair(defaultWidth, defaultHeight)

    fun expandPlot(plot: Plot, newWidth: Int, newHeight: Int): Result<Plot> {
        if (newWidth < plot.x2 - plot.x1 || newHeight < plot.z2 - plot.z1) {
            return failure(IllegalArgumentException("New dimensions must be larger than current dimensions"))
        }

        val center = plot.center
        val x1 = center.blockX - newWidth / 2
        val z1 = center.blockZ - newHeight / 2
        val x2 = center.blockX + newWidth / 2
        val z2 = center.blockZ + newHeight / 2

        // Check if expansion would overlap with other plots
        if (plots.values.any { other ->
            other.id != plot.id &&
            !(x2 < other.x1 || x1 > other.x2 || z2 < other.z1 || z1 > other.z2)
        }) {
            return failure(IllegalStateException("Expansion would overlap with another plot"))
        }

        return success(plot.copy(
            x1 = x1,
            z1 = z1,
            x2 = x2,
            z2 = z2
        )).also {
            plots[plot.id] = it.getOrNull()!!
            saveAllPlots()
        }
    }

    fun shrinkPlot(plot: Plot, newWidth: Int, newHeight: Int): Result<Plot> {
        if (newWidth > plot.x2 - plot.x1 || newHeight > plot.z2 - plot.z1) {
            return failure(IllegalArgumentException("New dimensions must be smaller than current dimensions"))
        }

        val minWidth = plugin.config.getInt("plot.min-width", 16)
        val minHeight = plugin.config.getInt("plot.min-height", 16)

        if (newWidth < minWidth || newHeight < minHeight) {
            return failure(IllegalArgumentException("New dimensions must be at least ${minWidth}x${minHeight}"))
        }

        val center = plot.center
        val x1 = center.blockX - newWidth / 2
        val z1 = center.blockZ - newHeight / 2
        val x2 = center.blockX + newWidth / 2
        val z2 = center.blockZ + newHeight / 2

        return success(plot.copy(
            x1 = x1,
            z1 = z1,
            x2 = x2,
            z2 = z2
        )).also {
            plots[plot.id] = it.getOrNull()!!
            saveAllPlots()
        }
    }

    fun movePlot(plot: Plot, newCenter: Location): Result<Plot> {
        val width = plot.x2 - plot.x1
        val height = plot.z2 - plot.z1
        val x1 = newCenter.blockX - width / 2
        val z1 = newCenter.blockZ - height / 2
        val x2 = newCenter.blockX + width / 2
        val z2 = newCenter.blockZ + height / 2

        // Check if move would overlap with other plots
        if (plots.values.any { other ->
            other.id != plot.id &&
            !(x2 < other.x1 || x1 > other.x2 || z2 < other.z1 || z1 > other.z2)
        }) {
            return failure(IllegalStateException("Move would overlap with another plot"))
        }

        return success(plot.copy(
            center = newCenter,
            x1 = x1,
            z1 = z1,
            x2 = x2,
            z2 = z2
        )).also {
            plots[plot.id] = it.getOrNull()!!
            saveAllPlots()
        }
    }

    fun mergePlots(plot1: Plot, plot2: Plot): Result<Plot> {
        if (plot1.owner != plot2.owner) {
            return failure(IllegalStateException("Plots must have the same owner"))
        }

        if (plot1.world != plot2.world) {
            return failure(IllegalStateException("Plots must be in the same world"))
        }

        val x1 = minOf(plot1.x1, plot2.x1)
        val y1 = minOf(plot1.y1, plot2.y1)
        val z1 = minOf(plot1.z1, plot2.z1)
        val x2 = maxOf(plot1.x2, plot2.x2)
        val y2 = maxOf(plot1.y2, plot2.y2)
        val z2 = maxOf(plot1.z2, plot2.z2)

        // Check if merged area would overlap with other plots
        if (plots.values.any { other ->
            other.id != plot1.id && other.id != plot2.id &&
            !(x2 < other.x1 || x1 > other.x2 || z2 < other.z1 || z1 > other.z2)
        }) {
            return failure(IllegalStateException("Merged plot would overlap with another plot"))
        }

        val center = Location(
            plot1.world,
            (x1 + x2) / 2.0,
            (y1 + y2) / 2.0,
            (z1 + z2) / 2.0
        )

        val mergedPlot = Plot(
            id = UUID.randomUUID().toString(),
            owner = plot1.owner,
            center = center,
            x1 = x1,
            y1 = y1,
            z1 = z1,
            x2 = x2,
            y2 = y2,
            z2 = z2,
            coowners = plot1.coowners + plot2.coowners,
            flags = plot1.flags + plot2.flags
        )

        plots.remove(plot1.id)
        plots.remove(plot2.id)
        plots[mergedPlot.id] = mergedPlot
        saveAllPlots()

        return success(mergedPlot)
    }

    fun splitPlot(plot: Plot, splitPoint: Location): Result<Pair<Plot, Plot>> {
        if (!plot.isInPlot(splitPoint)) {
            return failure(IllegalArgumentException("Split point must be inside the plot"))
        }

        val minWidth = plugin.config.getInt("plot.min-width", 16)
        val minHeight = plugin.config.getInt("plot.min-height", 16)

        val plot1 = Plot(
            id = UUID.randomUUID().toString(),
            owner = plot.owner,
            center = Location(plot.world, (plot.x1 + splitPoint.x) / 2, plot.y1.toDouble(), (plot.z1 + splitPoint.z) / 2),
            x1 = plot.x1,
            y1 = plot.y1,
            z1 = plot.z1,
            x2 = splitPoint.blockX,
            y2 = plot.y2,
            z2 = splitPoint.blockZ,
            coowners = plot.coowners,
            flags = plot.flags
        )

        val plot2 = Plot(
            id = UUID.randomUUID().toString(),
            owner = plot.owner,
            center = Location(plot.world, (splitPoint.x + plot.x2) / 2, plot.y1.toDouble(), (splitPoint.z + plot.z2) / 2),
            x1 = splitPoint.blockX,
            y1 = plot.y1,
            z1 = splitPoint.blockZ,
            x2 = plot.x2,
            y2 = plot.y2,
            z2 = plot.z2,
            coowners = plot.coowners,
            flags = plot.flags
        )

        if (plot1.x2 - plot1.x1 < minWidth || plot1.z2 - plot1.z1 < minHeight ||
            plot2.x2 - plot2.x1 < minWidth || plot2.z2 - plot2.z1 < minHeight) {
            return failure(IllegalArgumentException("Split would create plots smaller than minimum size"))
        }

        plots.remove(plot.id)
        plots[plot1.id] = plot1
        plots[plot2.id] = plot2
        saveAllPlots()

        return success(Pair(plot1, plot2))
    }

    companion object {
        const val SAVE_INTERVAL = 5 * 60 * 20L // 5 minut w tickach
    }
} 
} 