package com.plotsx.gui;

import com.plotsx.PlotsX;
import com.plotsx.models.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlotFlagsGUI {
    private final PlotsX plugin;
    private final Player player;
    private final Plot plot;
    private final Map<Integer, String> flagSlots;

    public PlotFlagsGUI(PlotsX plugin, Player player, Plot plot) {
        this.plugin = plugin;
        this.player = player;
        this.plot = plot;
        this.flagSlots = new HashMap<>();
    }

    public void open() {
        Inventory gui = Bukkit.createInventory(null, 27, "§6Zarządzanie flagami działki");

        // PvP flag
        ItemStack pvpItem = createFlagItem(
            Material.DIAMOND_SWORD,
            "§ePvP",
            "§7Zezwól na walkę PvP na działce",
            plot.isPvpEnabled()
        );
        gui.setItem(10, pvpItem);
        flagSlots.put(10, "pvp");

        // Mob damage flag
        ItemStack mobDamageItem = createFlagItem(
            Material.ZOMBIE_HEAD,
            "§eOchrona przed mobami",
            "§7Zezwól na ataki mobów na działce",
            plot.isMobDamageEnabled()
        );
        gui.setItem(11, mobDamageItem);
        flagSlots.put(11, "mob_damage");

        // Basic interactions flag
        ItemStack interactionsItem = createFlagItem(
            Material.LEVER,
            "§ePodstawowe interakcje",
            "§7Zezwól na używanie drzwi, przycisków itp.",
            plot.isBasicInteractionsEnabled()
        );
        gui.setItem(12, interactionsItem);
        flagSlots.put(12, "basic_interactions");

        // Back button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cPowrót");
        backMeta.setLore(Arrays.asList("§7Kliknij, aby wrócić"));
        backItem.setItemMeta(backMeta);
        gui.setItem(26, backItem);

        player.openInventory(gui);
    }

    private ItemStack createFlagItem(Material material, String name, String description, boolean enabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(
            description,
            "",
            enabled ? "§aWłączone" : "§cWyłączone",
            "§7Kliknij, aby " + (enabled ? "wyłączyć" : "włączyć")
        ));
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(int slot) {
        if (slot == 26) { // Back button
            player.closeInventory();
            return;
        }

        String flag = flagSlots.get(slot);
        if (flag == null) {
            return;
        }

        switch (flag) {
            case "pvp":
                plot.setPvpEnabled(!plot.isPvpEnabled());
                break;
            case "mob_damage":
                plot.setMobDamageEnabled(!plot.isMobDamageEnabled());
                break;
            case "basic_interactions":
                plot.setBasicInteractionsEnabled(!plot.isBasicInteractionsEnabled());
                break;
        }

        // Refresh GUI
        open();
    }
} 