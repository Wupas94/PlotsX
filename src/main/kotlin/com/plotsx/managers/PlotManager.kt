package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import com.plotsx.models.toConfig
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

class PlotManager(private val plugin: PlotsX) {
    private val plots = mutableMapOf<UUID, Plot>()
    private val plotsFile = File(plugin.dataFolder, "plots.yml").apply {
        if (!exists()) {
            plugin.saveResource("plots.yml", false)
        }
    }
    private val plotsConfig = YamlConfiguration.loadConfiguration(plotsFile)
    private val defaultRadius = plugin.config.getInt("default-plot-radius", 16)

    init {
        loadPlots()
    }

    private fun loadPlots() {
        plotsConfig.getKeys(false).forEach { key ->
            runCatching {
                val plotId = UUID.fromString(key)
                val config = mutableMapOf<String, Any>().apply {
                    put("owner", plotsConfig.getString("$key.owner")!!)
                    put("center", plotsConfig.get("$key.center") as Location)
                    put("radius", plotsConfig.getInt("$key.radius"))
                    put("isActive", plotsConfig.getBoolean("$key.active"))
                    put("isPvpEnabled", plotsConfig.getBoolean("$key.flags.pvp", false))
                    put("isMobDamageEnabled", plotsConfig.getBoolean("$key.flags.mob_damage", false))
                    put("isBasicInteractionsEnabled", plotsConfig.getBoolean("$key.flags.basic_interactions", true))
                    put("coOwners", plotsConfig.getStringList("$key.coOwners"))
                }
                
                Plot.fromConfig(config).also { plots[plotId] = it }
            }.onFailure { e ->
                plugin.logger.warning("Failed to load plot $key: ${e.message}")
            }
        }
    }

    fun saveAllPlots() {
        plots.forEach { (id, plot) ->
            val path = id.toString()
            val config = plot.toConfig()
            
            plotsConfig.apply {
                set("$path.owner", config["owner"])
                set("$path.center", config["center"])
                set("$path.radius", config["radius"])
                set("$path.creationDate", plot.creationDate)
                set("$path.active", config["isActive"])
                set("$path.flags.pvp", config["isPvpEnabled"])
                set("$path.flags.mob_damage", config["isMobDamageEnabled"])
                set("$path.flags.basic_interactions", config["isBasicInteractionsEnabled"])
                set("$path.coOwners", config["coOwners"])
            }
        }

        runCatching {
            plotsConfig.save(plotsFile)
        }.onFailure { e ->
            plugin.logger.severe("Could not save plots.yml: ${e.message}")
        }
    }

    fun createPlot(player: Player, center: Location): Result<Plot> {
        return when {
            getPlayerPlot(player.uniqueId) != null -> 
                failure(IllegalStateException("Player already has a plot"))
            getPlotAt(center) != null -> 
                failure(IllegalStateException("Location is already in a plot"))
            else -> Plot(player.uniqueId, center, defaultRadius)
                .let { plot ->
                    plots[plot.id] = plot
                    saveAllPlots()
                    success(plot)
                }
        }
    }

    fun deletePlot(plotId: UUID): Boolean {
        return plots.remove(plotId)?.let {
            saveAllPlots()
            true
        } ?: false
    }

    fun getPlotAt(location: Location): Plot? =
        plots.values.firstOrNull { location in it }

    fun getPlayerPlot(playerId: UUID): Plot? =
        plots.values.firstOrNull { it.isOwner(playerId) }

    fun getPlayerPlots(playerId: UUID): List<Plot> =
        plots.values.filter { it.hasAccess(playerId) }

    fun isLocationInAnyPlot(location: Location): Boolean =
        getPlotAt(location) != null

    fun getNearbyPlots(location: Location, radius: Int): List<Plot> =
        plots.values.filter { plot ->
            val distance = plot.center.distance(location)
            distance <= (radius + plot.radius)
        }

    fun getDefaultRadius(): Int = defaultRadius

    companion object {
        const val SAVE_INTERVAL = 5 * 60 * 20L // 5 minut w tickach
    }
} 
} 