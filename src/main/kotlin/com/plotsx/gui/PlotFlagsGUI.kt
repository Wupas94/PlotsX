package com.plotsx.gui

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class PlotFlagsGUI(
    private val plugin: PlotsX,
    private val player: Player,
    private val plot: Plot
) {
    fun open() {
        val inventory = Bukkit.createInventory(null, 27, "${ChatColor.DARK_PURPLE}Flagi działki")

        // PvP flag
        val pvpItem = createToggleItem(
            Material.DIAMOND_SWORD,
            "PvP",
            plot.isPvpEnabled,
            "Włącz/wyłącz PvP na działce"
        )
        inventory.setItem(10, pvpItem)

        // Mob damage flag
        val mobDamageItem = createToggleItem(
            Material.ZOMBIE_HEAD,
            "Ochrona przed mobami",
            plot.isMobDamageEnabled,
            "Włącz/wyłącz ochronę przed mobami"
        )
        inventory.setItem(11, mobDamageItem)

        // Basic interactions flag
        val interactionsItem = createToggleItem(
            Material.CHEST,
            "Podstawowe interakcje",
            plot.isBasicInteractionsEnabled,
            "Włącz/wyłącz podstawowe interakcje"
        )
        inventory.setItem(12, interactionsItem)

        // Close button
        val closeItem = ItemStack(Material.BARRIER).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("${ChatColor.RED}Zamknij")
                lore = listOf("${ChatColor.GRAY}Kliknij, aby zamknąć")
            }
        }
        inventory.setItem(26, closeItem)

        player.openInventory(inventory)
    }

    private fun createToggleItem(
        material: Material,
        name: String,
        enabled: Boolean,
        description: String
    ): ItemStack {
        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("${ChatColor.YELLOW}$name")
                lore = listOf(
                    "${ChatColor.GRAY}$description",
                    "",
                    if (enabled) "${ChatColor.GREEN}Włączone" else "${ChatColor.RED}Wyłączone"
                )
            }
        }
    }

    fun handleClick(slot: Int): Boolean {
        when (slot) {
            10 -> togglePvP()
            11 -> toggleMobDamage()
            12 -> toggleBasicInteractions()
            26 -> {
                player.closeInventory()
                return true
            }
        }
        return false
    }

    private fun togglePvP() {
        plot.isPvpEnabled = !plot.isPvpEnabled
        player.sendMessage("${ChatColor.GREEN}PvP zostało ${if (plot.isPvpEnabled) "włączone" else "wyłączone"}!")
        open() // Refresh the GUI
    }

    private fun toggleMobDamage() {
        plot.isMobDamageEnabled = !plot.isMobDamageEnabled
        player.sendMessage("${ChatColor.GREEN}Ochrona przed mobami została ${if (plot.isMobDamageEnabled) "włączona" else "wyłączona"}!")
        open() // Refresh the GUI
    }

    private fun toggleBasicInteractions() {
        plot.isBasicInteractionsEnabled = !plot.isBasicInteractionsEnabled
        player.sendMessage("${ChatColor.GREEN}Podstawowe interakcje zostały ${if (plot.isBasicInteractionsEnabled) "włączone" else "wyłączone"}!")
        open() // Refresh the GUI
    }
} 