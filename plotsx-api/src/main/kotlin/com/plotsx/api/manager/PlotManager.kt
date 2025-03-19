package com.plotsx.api.manager

import com.plotsx.api.model.Plot
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

interface PlotManager {
    fun createPlot(player: Player, center: Location, width: Int, height: Int): Result<Plot>
    fun deletePlot(plotId: String): Boolean
    fun getPlot(plotId: String): Plot?
    fun getPlotAt(location: Location): Plot?
    fun getPlayerPlot(playerId: UUID): Plot?
    fun getAllPlots(): List<Plot>
    fun getNearbyPlots(location: Location, distance: Int): List<Plot>
    fun getDefaultDimensions(): Pair<Int, Int>
    fun expandPlot(plot: Plot, newWidth: Int, newHeight: Int): Result<Plot>
    fun shrinkPlot(plot: Plot, newWidth: Int, newHeight: Int): Result<Plot>
    fun movePlot(plot: Plot, newCenter: Location): Result<Plot>
    fun mergePlots(plot1: Plot, plot2: Plot): Result<Plot>
    fun splitPlot(plot: Plot, splitPoint: Location): Result<Pair<Plot, Plot>>
} 