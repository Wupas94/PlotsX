package com.plotsx.commands

import com.plotsx.PlotsX
import com.plotsx.gui.PlotClaimGUI
import com.plotsx.gui.PlotFlagsGUI
import com.plotsx.models.Plot
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class PlotCommand(private val plugin: PlotsX) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}Ta komenda może być użyta tylko przez gracza!")
            return true
        }

        if (args.isEmpty()) {
            // Default claim command
            plugin.plotManager.getPlayerPlot(sender.uniqueId)?.let {
                sender.sendMessage("${ChatColor.RED}Masz już założoną działkę!")
                return true
            }

            val center = sender.location
            if (plugin.plotManager.isLocationInAnyPlot(center)) {
                sender.sendMessage("${ChatColor.RED}Ta lokalizacja jest już zajęta przez inną działkę!")
                return true
            }

            PlotClaimGUI(plugin, sender, center).open()
            return true
        }

        when (args[0].lowercase()) {
            "info" -> handleInfo(sender)
            "delete" -> handleDelete(sender)
            "add" -> {
                if (args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Użyj: /plot add <gracz>")
                    return true
                }
                handleAdd(sender, args[1])
            }
            "remove" -> {
                if (args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Użyj: /plot remove <gracz>")
                    return true
                }
                handleRemove(sender, args[1])
            }
            "flags" -> handleFlags(sender)
            else -> sendHelp(sender)
        }

        return true
    }

    private fun handleInfo(player: Player) {
        val plot = plugin.plotManager.getPlayerPlot(player.uniqueId) ?: run {
            player.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        player.sendMessage("${ChatColor.GOLD}=== Informacje o działce ===")
        player.sendMessage("${ChatColor.YELLOW}Właściciel: ${ChatColor.WHITE}${player.name}")
        player.sendMessage("${ChatColor.YELLOW}Rozmiar: ${ChatColor.WHITE}${plot.radius * 2 + 1}x${plot.radius * 2 + 1}")
        player.sendMessage("${ChatColor.YELLOW}Promień: ${ChatColor.WHITE}${plot.radius} bloków")
        player.sendMessage("${ChatColor.YELLOW}Środek: ${ChatColor.WHITE}" +
            String.format("X: %.0f, Y: %.0f, Z: %.0f", 
                plot.center.x, plot.center.y, plot.center.z))
        
        player.sendMessage("${ChatColor.YELLOW}Flagi:")
        player.sendMessage("${ChatColor.WHITE}- PvP: ${if (plot.isPvpEnabled) "${ChatColor.GREEN}Włączone" else "${ChatColor.RED}Wyłączone"}")
        player.sendMessage("${ChatColor.WHITE}- Ochrona przed mobami: ${if (plot.isMobDamageEnabled) "${ChatColor.GREEN}Włączone" else "${ChatColor.RED}Wyłączone"}")
        player.sendMessage("${ChatColor.WHITE}- Podstawowe interakcje: ${if (plot.isBasicInteractionsEnabled) "${ChatColor.GREEN}Włączone" else "${ChatColor.RED}Wyłączone"}")
        
        if (plot.coOwners.isNotEmpty()) {
            player.sendMessage("${ChatColor.YELLOW}Współwłaściciele:")
            plot.coOwners.forEach { coOwnerId ->
                val coOwnerName = plugin.server.getOfflinePlayer(coOwnerId).name
                player.sendMessage("${ChatColor.WHITE}- $coOwnerName")
            }
        }
    }

    private fun handleDelete(player: Player) {
        val plot = plugin.plotManager.getPlayerPlot(player.uniqueId) ?: run {
            player.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        plugin.plotManager.deletePlot(plot.id)
        player.sendMessage("${ChatColor.GREEN}Działka została usunięta!")
    }

    private fun handleAdd(player: Player, targetName: String) {
        val plot = plugin.plotManager.getPlayerPlot(player.uniqueId) ?: run {
            player.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        val target = plugin.server.getPlayer(targetName) ?: run {
            player.sendMessage("${ChatColor.RED}Nie znaleziono gracza!")
            return
        }

        if (plot.isCoOwner(target.uniqueId)) {
            player.sendMessage("${ChatColor.RED}Ten gracz jest już współwłaścicielem działki!")
            return
        }

        plot.addCoOwner(target.uniqueId)
        player.sendMessage("${ChatColor.GREEN}Dodano ${target.name} jako współwłaściciela działki!")
        target.sendMessage("${ChatColor.GREEN}Zostałeś dodany jako współwłaściciel działki gracza ${player.name}!")
    }

    private fun handleRemove(player: Player, targetName: String) {
        val plot = plugin.plotManager.getPlayerPlot(player.uniqueId) ?: run {
            player.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        val target = plugin.server.getPlayer(targetName) ?: run {
            player.sendMessage("${ChatColor.RED}Nie znaleziono gracza!")
            return
        }

        if (!plot.isCoOwner(target.uniqueId)) {
            player.sendMessage("${ChatColor.RED}Ten gracz nie jest współwłaścicielem działki!")
            return
        }

        plot.removeCoOwner(target.uniqueId)
        player.sendMessage("${ChatColor.GREEN}Usunięto ${target.name} ze współwłaścicieli działki!")
        target.sendMessage("${ChatColor.RED}Zostałeś usunięty ze współwłaścicieli działki gracza ${player.name}!")
    }

    private fun handleFlags(player: Player) {
        val plot = plugin.plotManager.getPlayerPlot(player.uniqueId) ?: run {
            player.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        PlotFlagsGUI(plugin, player, plot).open()
    }

    private fun sendHelp(player: Player) {
        player.sendMessage("${ChatColor.GOLD}=== PlotsX - Pomoc ===")
        player.sendMessage("${ChatColor.YELLOW}/plot ${ChatColor.WHITE}- Załóż nową działkę")
        player.sendMessage("${ChatColor.YELLOW}/plot info ${ChatColor.WHITE}- Wyświetl informacje o swojej działce")
        player.sendMessage("${ChatColor.YELLOW}/plot delete ${ChatColor.WHITE}- Usuń swoją działkę")
        player.sendMessage("${ChatColor.YELLOW}/plot add <gracz> ${ChatColor.WHITE}- Dodaj współwłaściciela działki")
        player.sendMessage("${ChatColor.YELLOW}/plot remove <gracz> ${ChatColor.WHITE}- Usuń współwłaściciela działki")
        player.sendMessage("${ChatColor.YELLOW}/plot flags ${ChatColor.WHITE}- Zarządzaj flagami działki")
    }
} 