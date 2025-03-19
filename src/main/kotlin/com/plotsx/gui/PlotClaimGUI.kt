package com.plotsx.gui

import com.plotsx.PlotsX
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta

class PlotClaimGUI(
    private val plugin: PlotsX,
    private val player: Player,
    private val plotCenter: Location
) {
    fun open() {
        val gui = Bukkit.createInventory(null, 27, "§6Potwierdź założenie działki")

        // Confirm button
        gui.setItem(11, createConfirmItem())

        // Cancel button
        gui.setItem(15, createCancelItem())

        // Plot info
        gui.setItem(13, createInfoItem())

        player.openInventory(gui)
    }

    private fun createConfirmItem(): ItemStack {
        return ItemStack(Material.LIME_STAINED_GLASS_PANE).apply {
            itemMeta = (itemMeta ?: Bukkit.getItemFactory().getItemMeta(type))?.apply {
                setDisplayName("§aPotwierdź")
                lore = listOf(
                    "§7Kliknij, aby potwierdzić",
                    "§7założenie działki",
                    "",
                    "§7Środek działki:",
                    String.format("§7X: %.0f, Y: %.0f, Z: %.0f", 
                        plotCenter.x, plotCenter.y, plotCenter.z),
                    "§7Promień: ${plugin.plotManager.getDefaultRadius()} bloków"
                )
            }
        }
    }

    private fun createCancelItem(): ItemStack {
        return ItemStack(Material.RED_STAINED_GLASS_PANE).apply {
            itemMeta = (itemMeta ?: Bukkit.getItemFactory().getItemMeta(type))?.apply {
                setDisplayName("§cAnuluj")
                lore = listOf(
                    "§7Kliknij, aby anulować",
                    "§7założenie działki"
                )
            }
        }
    }

    private fun createInfoItem(): ItemStack {
        return ItemStack(Material.PLAYER_HEAD).apply {
            itemMeta = (itemMeta as? SkullMeta)?.apply {
                setDisplayName("§eInformacje o działce")
                lore = listOf(
                    "§7Właściciel: §f${player.name}",
                    "§7Rozmiar: §f${plugin.plotManager.getDefaultRadius() * 2 + 1}x${plugin.plotManager.getDefaultRadius() * 2 + 1}",
                    "§7Promień: §f${plugin.plotManager.getDefaultRadius()} bloków",
                    "",
                    "§7Środek działki:",
                    String.format("§7X: %.0f, Y: %.0f, Z: %.0f", 
                        plotCenter.x, plotCenter.y, plotCenter.z)
                )
            }
        }
    }

    fun handleClick(slot: Int) {
        when (slot) {
            11 -> { // Confirm
                val plot = plugin.plotManager.createPlot(player, plotCenter)
                if (plot != null) {
                    player.sendMessage("§aDziałka została założona pomyślnie!")
                    player.closeInventory()
                } else {
                    player.sendMessage("§cNie udało się założyć działki! Możliwe, że masz już działkę lub ta lokalizacja jest zajęta.")
                }
            }
            15 -> { // Cancel
                player.sendMessage("§cZakładanie działki zostało anulowane.")
                player.closeInventory()
            }
        }
    }
} 