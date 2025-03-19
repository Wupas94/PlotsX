package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.WeatherType
import org.bukkit.entity.Player
import java.util.*

class WeatherManager(private val plugin: PlotsX) {
    private val plotWeather = mutableMapOf<UUID, WeatherType>()
    private val playerWeather = mutableMapOf<UUID, UUID>() // Player UUID -> Plot UUID

    fun setPlotWeather(plot: Plot, weather: WeatherType) {
        plotWeather[plot.id] = weather
        savePlotWeather(plot.id)
        updatePlayersInPlot(plot)
    }

    fun getPlotWeather(plot: Plot): WeatherType? {
        return plotWeather[plot.id]
    }

    fun updatePlayerWeather(player: Player) {
        val plot = plugin.plotManager.getPlotAt(player.location)
        if (plot == null) {
            player.setPlayerWeather(WeatherType.CLEAR)
            playerWeather.remove(player.uniqueId)
            return
        }

        val weather = plotWeather[plot.id] ?: WeatherType.CLEAR
        player.setPlayerWeather(weather)
        playerWeather[player.uniqueId] = plot.id
    }

    private fun updatePlayersInPlot(plot: Plot) {
        val weather = plotWeather[plot.id] ?: return
        plugin.server.onlinePlayers.forEach { player ->
            if (plugin.plotManager.isLocationInPlot(player.location, plot)) {
                player.setPlayerWeather(weather)
                playerWeather[player.uniqueId] = plot.id
            }
        }
    }

    fun loadPlotWeather(plotId: UUID) {
        val config = plugin.config
        val path = "weather.$plotId"
        if (!config.contains(path)) return

        val weatherName = config.getString(path)
        if (weatherName != null) {
            try {
                plotWeather[plotId] = WeatherType.valueOf(weatherName)
            } catch (e: IllegalArgumentException) {
                plugin.logger.warning("Invalid weather type for plot $plotId: $weatherName")
            }
        }
    }

    private fun savePlotWeather(plotId: UUID) {
        val config = plugin.config
        val path = "weather.$plotId"
        plotWeather[plotId]?.let { weather ->
            config.set(path, weather.name)
        } ?: config.set(path, null)
        plugin.saveConfig()
    }

    fun onPlayerMove(player: Player) {
        val currentPlotId = playerWeather[player.uniqueId]
        val newPlot = plugin.plotManager.getPlotAt(player.location)

        if (currentPlotId != newPlot?.id) {
            updatePlayerWeather(player)
        }
    }
} 