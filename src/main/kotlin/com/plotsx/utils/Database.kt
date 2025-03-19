package com.plotsx.utils

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class Database(private val plugin: PlotsX) {
    private val dataFolder = plugin.dataFolder
    private val plotsFile = File(dataFolder, "plots.yml")
    private val config = YamlConfiguration()

    init {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        if (!plotsFile.exists()) {
            plotsFile.createNewFile()
        }
        config.load(plotsFile)
    }

    fun savePlot(plot: Plot) {
        try {
            config.set("plots.${plot.id}.owner", plot.owner.toString())
            config.set("plots.${plot.id}.coowners", plot.coowners.map { it.toString() })
            config.set("plots.${plot.id}.world", plot.world)
            config.set("plots.${plot.id}.x1", plot.x1)
            config.set("plots.${plot.id}.y1", plot.y1)
            config.set("plots.${plot.id}.z1", plot.z1)
            config.set("plots.${plot.id}.x2", plot.x2)
            config.set("plots.${plot.id}.y2", plot.y2)
            config.set("plots.${plot.id}.z2", plot.z2)
            config.set("plots.${plot.id}.flags", plot.flags)
            config.save(plotsFile)
            plugin.metricsManager.recordSuccess("plot_save")
        } catch (e: Exception) {
            plugin.metricsManager.recordError("plot_save_error")
            plugin.notificationManager.addNotification(
                "database",
                "Błąd podczas zapisywania działki: ${e.message}",
                plugin.notificationManager.NotificationPriority.HIGH
            )
            plugin.logger.severe("Błąd podczas zapisywania działki: ${e.message}")
        }
    }

    fun loadPlot(id: String): Plot? {
        return try {
            val plotSection = config.getConfigurationSection("plots.$id") ?: return null
            val owner = UUID.fromString(plotSection.getString("owner"))
            val coowners = plotSection.getStringList("coowners").map { UUID.fromString(it) }.toSet()
            val world = plotSection.getString("world") ?: return null
            val x1 = plotSection.getInt("x1")
            val y1 = plotSection.getInt("y1")
            val z1 = plotSection.getInt("z1")
            val x2 = plotSection.getInt("x2")
            val y2 = plotSection.getInt("y2")
            val z2 = plotSection.getInt("z2")
            val flags = plotSection.getConfigurationSection("flags")?.getValues(false) ?: emptyMap()

            Plot(
                id = id,
                owner = owner,
                coowners = coowners,
                world = world,
                x1 = x1,
                y1 = y1,
                z1 = z1,
                x2 = x2,
                y2 = y2,
                z2 = z2,
                flags = flags
            ).also {
                plugin.metricsManager.recordSuccess("plot_load")
            }
        } catch (e: Exception) {
            plugin.metricsManager.recordError("plot_load_error")
            plugin.notificationManager.addNotification(
                "database",
                "Błąd podczas ładowania działki: ${e.message}",
                plugin.notificationManager.NotificationPriority.HIGH
            )
            plugin.logger.severe("Błąd podczas ładowania działki: ${e.message}")
            null
        }
    }

    fun loadAllPlots(): List<Plot> {
        return try {
            val plots = mutableListOf<Plot>()
            val plotsSection = config.getConfigurationSection("plots")
            
            if (plotsSection != null) {
                for (id in plotsSection.getKeys(false)) {
                    loadPlot(id)?.let { plots.add(it) }
                }
            }
            
            plugin.metricsManager.recordSuccess("plots_load_all")
            plots
        } catch (e: Exception) {
            plugin.metricsManager.recordError("plots_load_all_error")
            plugin.notificationManager.addNotification(
                "database",
                "Błąd podczas ładowania wszystkich działek: ${e.message}",
                plugin.notificationManager.NotificationPriority.HIGH
            )
            plugin.logger.severe("Błąd podczas ładowania wszystkich działek: ${e.message}")
            emptyList()
        }
    }

    fun deletePlot(id: String): Boolean {
        return try {
            config.set("plots.$id", null)
            config.save(plotsFile)
            plugin.metricsManager.recordSuccess("plot_delete")
            true
        } catch (e: Exception) {
            plugin.metricsManager.recordError("plot_delete_error")
            plugin.notificationManager.addNotification(
                "database",
                "Błąd podczas usuwania działki: ${e.message}",
                plugin.notificationManager.NotificationPriority.HIGH
            )
            plugin.logger.severe("Błąd podczas usuwania działki: ${e.message}")
            false
        }
    }
} 