package com.plotsx.systems.flags

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.WeatherType
import org.bukkit.entity.Player
import java.util.*

class FlagManager(private val plugin: PlotsX) {
    private val plotFlags = mutableMapOf<UUID, MutableMap<PlotFlag, Boolean>>()
    private val plotWeather = mutableMapOf<UUID, WeatherType>()
    private val plotTime = mutableMapOf<UUID, Long>()
    private val welcomeMessages = mutableMapOf<UUID, String>()
    private val farewellMessages = mutableMapOf<UUID, String>()

    fun setFlag(plot: Plot, flag: PlotFlag, value: Boolean) {
        plotFlags.getOrPut(plot.id) { mutableMapOf() }[flag] = value
    }

    fun getFlag(plot: Plot, flag: PlotFlag): Boolean {
        return plotFlags[plot.id]?.get(flag) ?: flag.defaultValue
    }

    fun setWeather(plot: Plot, weather: WeatherType) {
        if (!getFlag(plot, PlotFlag.WEATHER)) return
        plotWeather[plot.id] = weather
        
        // Update weather for all players in the plot
        plugin.server.onlinePlayers
            .filter { plugin.plotManager.isInPlot(it.location, plot) }
            .forEach { it.setPlayerWeather(weather) }
    }

    fun setTime(plot: Plot, time: Long) {
        if (!getFlag(plot, PlotFlag.TIME)) return
        plotTime[plot.id] = time
        
        // Update time for all players in the plot
        plugin.server.onlinePlayers
            .filter { plugin.plotManager.isInPlot(it.location, plot) }
            .forEach { it.setPlayerTime(time, false) }
    }

    fun setWelcomeMessage(plot: Plot, message: String) {
        if (!getFlag(plot, PlotFlag.WELCOME_MESSAGE)) return
        welcomeMessages[plot.id] = message
    }

    fun setFarewellMessage(plot: Plot, message: String) {
        if (!getFlag(plot, PlotFlag.FAREWELL_MESSAGE)) return
        farewellMessages[plot.id] = message
    }

    fun handlePlayerEnterPlot(player: Player, plot: Plot) {
        // Apply plot-specific settings
        if (getFlag(plot, PlotFlag.WEATHER)) {
            plotWeather[plot.id]?.let { player.setPlayerWeather(it) }
        }

        if (getFlag(plot, PlotFlag.TIME)) {
            plotTime[plot.id]?.let { player.setPlayerTime(it, false) }
        }

        // Show welcome message
        if (getFlag(plot, PlotFlag.WELCOME_MESSAGE)) {
            welcomeMessages[plot.id]?.let { player.sendMessage(it) }
        }
    }

    fun handlePlayerLeavePlot(player: Player, plot: Plot) {
        // Reset player-specific settings
        player.resetPlayerWeather()
        player.resetPlayerTime()

        // Show farewell message
        if (getFlag(plot, PlotFlag.FAREWELL_MESSAGE)) {
            farewellMessages[plot.id]?.let { player.sendMessage(it) }
        }
    }

    fun startAutoFeed(plot: Plot) {
        if (!getFlag(plot, PlotFlag.AUTO_FEED)) return
        
        plugin.server.scheduler.runTaskTimer(plugin, { task ->
            if (!getFlag(plot, PlotFlag.AUTO_FEED)) {
                task.cancel()
                return@runTaskTimer
            }

            plugin.server.onlinePlayers
                .filter { plugin.plotManager.isInPlot(it.location, plot) }
                .forEach { it.foodLevel = 20 }
        }, 0L, 20L) // Check every second
    }

    // Save/load methods for persistence
    fun saveData() {
        // TODO: Implement saving flags data to config
    }

    fun loadData() {
        // TODO: Implement loading flags data from config
    }
} 