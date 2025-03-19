package com.plotsx.api.manager

import com.plotsx.api.model.Plot
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

interface PlotManager {
    fun createPlot(player: Player, center: Location): Result<Plot>
    fun deletePlot(plotId: UUID): Boolean
    fun getPlotAt(location: Location): Plot?
    fun getPlayerPlot(playerId: UUID): Plot?
    fun getPlayerPlots(playerId: UUID): List<Plot>
    fun isLocationInAnyPlot(location: Location): Boolean
    fun getNearbyPlots(location: Location, radius: Int): List<Plot>
    fun getDefaultRadius(): Int
    fun saveAllPlots()
} 