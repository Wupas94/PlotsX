package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class ProtectionManager(private val plugin: PlotsX) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val location = event.block.location
        val plot = plugin.plotManager.getPlotAt(location)

        if (plot != null) {
            if (!plugin.trustManager.canPlayerBuild(plot, player)) {
                event.isCancelled = true
                player.sendMessage("§cNie masz uprawnień do niszczenia bloków na tej działce!")
                return
            }

            // Log block break with CoreProtect
            if (plugin.coreProtect.isEnabled()) {
                plugin.coreProtect.logBlockBreak(player, event.block)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val location = event.block.location
        val plot = plugin.plotManager.getPlotAt(location)

        if (plot != null) {
            if (!plugin.trustManager.canPlayerBuild(plot, player)) {
                event.isCancelled = true
                player.sendMessage("§cNie masz uprawnień do stawiania bloków na tej działce!")
                return
            }

            // Log block place with CoreProtect
            if (plugin.coreProtect.isEnabled()) {
                plugin.coreProtect.logBlockPlace(player, event.block)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val player = event.player
        val plot = plugin.plotManager.getPlotAt(block.location)

        if (plot != null) {
            if (!plugin.trustManager.canPlayerBuild(plot, player)) {
                // Check if the player at least has USE permission
                if (!plugin.trustManager.getTrustLevel(plot, player.uniqueId).ordinal >= TrustLevel.USE.ordinal) {
                    event.isCancelled = true
                    player.sendMessage("§cNie masz uprawnień do interakcji na tej działce!")
                    return
                }
            }

            // Log container interactions with CoreProtect
            if (plugin.coreProtect.isEnabled() && block.state is org.bukkit.block.Container) {
                plugin.coreProtect.logContainerTransaction(player, block)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        if (damager !is Player) return

        val location = event.entity.location
        val plot = plugin.plotManager.getPlotAt(location)

        if (plot != null) {
            // Check PvP flag if both entities are players
            if (event.entity is Player && !plugin.flagManager.getFlag(plot, PlotFlag.PVP)) {
                event.isCancelled = true
                damager.sendMessage("§cPvP jest wyłączone na tej działce!")
                return
            }

            // Check mob damage flag if the entity is a mob
            if (event.entity !is Player && !plugin.flagManager.getFlag(plot, PlotFlag.MOB_DAMAGE)) {
                event.isCancelled = true
                damager.sendMessage("§cNie możesz atakować mobów na tej działce!")
                return
            }
        }
    }

    fun canPlayerAccessPlot(player: Player, plot: Plot?): Boolean =
        plot?.hasAccess(player.uniqueId) ?: true
} 