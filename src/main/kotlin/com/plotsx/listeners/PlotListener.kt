package com.plotsx.listeners

import com.plotsx.PlotsX
import com.plotsx.models.TrustLevel
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlotListener(private val plugin: PlotsX) : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val plot = plugin.plotManager.getPlotAt(event.block.location)
        if (plot == null) return

        val player = event.player
        val trustLevel = plugin.trustManager.getTrustLevel(player.uniqueId, plot)
        
        if (trustLevel < TrustLevel.BUILD) {
            event.isCancelled = true
            player.sendMessage("${ChatColor.RED}Nie masz uprawnień do niszczenia bloków na tej działce!")
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val plot = plugin.plotManager.getPlotAt(event.block.location)
        if (plot == null) return

        val player = event.player
        val trustLevel = plugin.trustManager.getTrustLevel(player.uniqueId, plot)
        
        if (trustLevel < TrustLevel.BUILD) {
            event.isCancelled = true
            player.sendMessage("${ChatColor.RED}Nie masz uprawnień do stawiania bloków na tej działce!")
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.damager !is org.bukkit.entity.Player) return
        
        val plot = plugin.plotManager.getPlotAt(event.entity.location)
        if (plot == null) return

        if (!plot.isPvpEnabled) {
            event.isCancelled = true
            event.damager.sendMessage("${ChatColor.RED}PvP jest wyłączone na tej działce!")
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity !is org.bukkit.entity.Player) return
        
        val plot = plugin.plotManager.getPlotAt(event.entity.location)
        if (plot == null) return

        if (!plot.isMobDamageEnabled) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val plot = plugin.plotManager.getPlotAt(event.block?.location ?: return)
        if (plot == null) return

        val player = event.player
        val trustLevel = plugin.trustManager.getTrustLevel(player.uniqueId, plot)
        
        if (trustLevel < TrustLevel.BUILD && !plot.isBasicInteractionsEnabled) {
            event.isCancelled = true
            player.sendMessage("${ChatColor.RED}Nie masz uprawnień do interakcji na tej działce!")
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val fromPlot = plugin.plotManager.getPlotAt(event.from)
        val toPlot = plugin.plotManager.getPlotAt(event.to)
        
        if (fromPlot != toPlot) {
            if (fromPlot != null) {
                plugin.worldManager.onPlayerMove(event.player, fromPlot, null)
            }
            if (toPlot != null) {
                plugin.worldManager.onPlayerMove(event.player, null, toPlot)
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val plot = plugin.plotManager.getPlotAt(event.player.location)
        if (plot != null) {
            plugin.worldManager.onPlayerMove(event.player, plot, null)
        }
    }
} 