package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class ProtectionManager(private val plugin: PlotsX) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val plot = plugin.plotManager.getPlotAt(event.block.location)
        
        if (plot != null && !plot.hasAccess(player.uniqueId)) {
            event.isCancelled = true
            player.sendMessage("§cNie możesz niszczyć bloków na tej działce!")
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val plot = plugin.plotManager.getPlotAt(event.block.location)
        
        if (plot != null && !plot.hasAccess(player.uniqueId)) {
            event.isCancelled = true
            player.sendMessage("§cNie możesz stawiać bloków na tej działce!")
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val plot = plugin.plotManager.getPlotAt(event.clickedBlock?.location ?: return)
        
        if (plot != null && !plot.hasAccess(player.uniqueId)) {
            // Check if basic interactions are enabled
            if (plot.isBasicInteractionsEnabled && 
                event.action.name.contains("RIGHT_CLICK") && 
                event.clickedBlock?.type?.name?.let { type ->
                    type.contains("DOOR") || 
                    type.contains("GATE") ||
                    type.contains("BUTTON") ||
                    type.contains("LEVER")
                } == true) {
                return
            }
            
            event.isCancelled = true
            player.sendMessage("§cNie możesz używać przedmiotów na tej działce!")
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val plot = plugin.plotManager.getPlotAt(event.entity.location)
        
        if (plot != null) {
            // Check PvP flag
            if (!plot.isPvpEnabled && event.entity is Player) {
                event.isCancelled = true
                damager.sendMessage("§cPvP jest wyłączone na tej działce!")
                return
            }
            
            // Check mob damage flag
            if (!plot.isMobDamageEnabled && event.entity !is Player) {
                event.isCancelled = true
                damager.sendMessage("§cAtaki na moby są wyłączone na tej działce!")
                return
            }
        }
    }

    fun canPlayerAccessPlot(player: Player, plot: Plot?): Boolean =
        plot?.hasAccess(player.uniqueId) ?: true
} 