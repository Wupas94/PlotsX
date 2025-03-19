package com.plotsx.gui

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PlotFlagsGUI(
    private val plugin: PlotsX,
    private val player: Player,
    private val plot: Plot
) {
    private val flagSlots = mutableMapOf<Int, String>()

    fun open() {
        val gui = Bukkit.createInventory(null, 27, "§6Zarządzanie flagami działki")

        // PvP flag
        createFlagItem(
            Material.DIAMOND_SWORD,
            "§ePvP",
            "§7Zezwól na walkę PvP na działce",
            plot.isPvpEnabled
        ).also {
            gui.setItem(10, it)
            flagSlots[10] = "pvp"
        }

        // Mob damage flag
        createFlagItem(
            Material.ZOMBIE_HEAD,
            "§eOchrona przed mobami",
            "§7Zezwól na ataki mobów na działce",
            plot.isMobDamageEnabled
        ).also {
            gui.setItem(11, it)
            flagSlots[11] = "mob_damage"
        }

        // Basic interactions flag
        createFlagItem(
            Material.LEVER,
            "§ePodstawowe interakcje",
            "§7Zezwól na używanie drzwi, przycisków itp.",
            plot.isBasicInteractionsEnabled
        ).also {
            gui.setItem(12, it)
            flagSlots[12] = "basic_interactions"
        }

        // Back button
        ItemStack(Material.BARRIER).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("§cPowrót")
                lore = listOf("§7Kliknij, aby wrócić")
            }
        }.also {
            gui.setItem(26, it)
        }

        player.openInventory(gui)
    }

    private fun createFlagItem(material: Material, name: String, description: String, enabled: Boolean): ItemStack {
        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName(name)
                lore = listOf(
                    description,
                    "",
                    if (enabled) "§aWłączone" else "§cWyłączone",
                    "§7Kliknij, aby ${if (enabled) "wyłączyć" else "włączyć"}"
                )
            }
        }
    }

    fun handleClick(slot: Int) {
        if (slot == 26) { // Back button
            player.closeInventory()
            return
        }

        when (flagSlots[slot]) {
            "pvp" -> plot.isPvpEnabled = !plot.isPvpEnabled
            "mob_damage" -> plot.isMobDamageEnabled = !plot.isMobDamageEnabled
            "basic_interactions" -> plot.isBasicInteractionsEnabled = !plot.isBasicInteractionsEnabled
            else -> return
        }

        // Refresh GUI
        open()
    }
} 