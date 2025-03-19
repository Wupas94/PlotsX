package com.plotsx.gui

import com.plotsx.PlotsX
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class PlotClaimGUI(
    private val plugin: PlotsX,
    private val player: Player,
    private val center: org.bukkit.Location
) {
    fun open() {
        val inventory = Bukkit.createInventory(null, 27, "${ChatColor.DARK_PURPLE}Zakładanie działki")

        // Confirm button
        val confirmItem = ItemStack(Material.LIME_WOOL).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("${ChatColor.GREEN}Potwierdź")
                lore = listOf(
                    "${ChatColor.GRAY}Kliknij, aby założyć działkę",
                    "",
                    "${ChatColor.YELLOW}Środek działki:",
                    "${ChatColor.WHITE}X: ${center.blockX}",
                    "${ChatColor.WHITE}Y: ${center.blockY}",
                    "${ChatColor.WHITE}Z: ${center.blockZ}"
                )
            }
        }
        inventory.setItem(11, confirmItem)

        // Cancel button
        val cancelItem = ItemStack(Material.RED_WOOL).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("${ChatColor.RED}Anuluj")
                lore = listOf("${ChatColor.GRAY}Kliknij, aby anulować")
            }
        }
        inventory.setItem(15, cancelItem)

        player.openInventory(inventory)
    }

    fun handleClick(slot: Int): Boolean {
        when (slot) {
            11 -> {
                if (plugin.plotManager.createPlot(player.uniqueId, center)) {
                    player.sendMessage("${ChatColor.GREEN}Działka została założona!")
                } else {
                    player.sendMessage("${ChatColor.RED}Nie udało się założyć działki!")
                }
                player.closeInventory()
                return true
            }
            15 -> {
                player.closeInventory()
                return true
            }
        }
        return false
    }
} 