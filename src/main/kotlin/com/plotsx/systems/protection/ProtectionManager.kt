package com.plotsx.systems.protection

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class ProtectionManager(private val plugin: PlotsX) {
    private val plotManager = plugin.plotManager
    private val messages = plugin.messages
    private val trustedPlayers = mutableMapOf<UUID, Set<UUID>>()

    fun isPlayerTrusted(plot: Plot, player: Player): Boolean {
        return plot.owner == player.uniqueId || 
               plot.coowners.contains(player.uniqueId) ||
               trustedPlayers[plot.owner]?.contains(player.uniqueId) == true
    }

    fun trustPlayer(plot: Plot, player: Player, target: Player) {
        if (!isPlayerTrusted(plot, player)) {
            messages.sendMessage(player, "Nie masz uprawnień do zarządzania zaufanymi graczami na tej działce!")
            return
        }

        val trusted = trustedPlayers.getOrDefault(plot.owner, emptySet()).toMutableSet()
        trusted.add(target.uniqueId)
        trustedPlayers[plot.owner] = trusted
        messages.sendMessage(player, "Dodałeś gracza ${target.name} do zaufanych!")
        messages.sendMessage(target, "Zostałeś dodany do zaufanych graczy na działce ${plot.id}!")
    }

    fun untrustPlayer(plot: Plot, player: Player, target: Player) {
        if (!isPlayerTrusted(plot, player)) {
            messages.sendMessage(player, "Nie masz uprawnień do zarządzania zaufanymi graczami na tej działce!")
            return
        }

        val trusted = trustedPlayers.getOrDefault(plot.owner, emptySet()).toMutableSet()
        trusted.remove(target.uniqueId)
        trustedPlayers[plot.owner] = trusted
        messages.sendMessage(player, "Usunąłeś gracza ${target.name} z zaufanych!")
        messages.sendMessage(target, "Zostałeś usunięty z zaufanych graczy na działce ${plot.id}!")
    }

    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val location = event.block.location
        val plot = plotManager.getPlotAt(location)

        if (plot != null && !isPlayerTrusted(plot, player)) {
            event.isCancelled = true
            messages.sendMessage(player, "Nie możesz niszczyć bloków na tej działce!")
        }
    }

    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val location = event.block.location
        val plot = plotManager.getPlotAt(location)

        if (plot != null && !isPlayerTrusted(plot, player)) {
            event.isCancelled = true
            messages.sendMessage(player, "Nie możesz stawiać bloków na tej działce!")
        }
    }

    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock ?: return
        val location = block.location
        val plot = plotManager.getPlotAt(location)

        if (plot != null && !isPlayerTrusted(plot, player)) {
            event.isCancelled = true
            messages.sendMessage(player, "Nie możesz wchodzić w interakcję z blokami na tej działce!")
        }
    }

    fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        val location = entity.location
        val plot = plotManager.getPlotAt(location)

        if (plot != null) {
            event.isCancelled = true
        }
    }

    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val entity = event.entity
        val location = entity.location
        val plot = plotManager.getPlotAt(location)

        if (plot != null && damager is Player && !isPlayerTrusted(plot, damager)) {
            event.isCancelled = true
            messages.sendMessage(damager, "Nie możesz atakować istot na tej działce!")
        }
    }

    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val from = event.from
        val to = event.to
        val fromPlot = plotManager.getPlotAt(from)
        val toPlot = plotManager.getPlotAt(to)

        if (fromPlot != toPlot) {
            if (toPlot != null && !isPlayerTrusted(toPlot, player)) {
                event.isCancelled = true
                messages.sendMessage(player, "Nie możesz wejść na tę działkę!")
            }
        }
    }

    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        trustedPlayers.values.forEach { it.remove(player.uniqueId) }
    }

    fun saveData() {
        // TODO: Implement saving trusted players data
    }

    fun loadData() {
        // TODO: Implement loading trusted players data
    }
} 