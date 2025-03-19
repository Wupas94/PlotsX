package com.plotsx.gui;

import com.plotsx.PlotsX;
import com.plotsx.models.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class PlotClaimGUI {
    private final PlotsX plugin;
    private final Player player;
    private final Location plotCenter;

    public PlotClaimGUI(PlotsX plugin, Player player, Location plotCenter) {
        this.plugin = plugin;
        this.player = player;
        this.plotCenter = plotCenter;
    }

    public void open() {
        Inventory gui = Bukkit.createInventory(null, 27, "§6Potwierdź założenie działki");

        // Confirm button
        ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName("§aPotwierdź");
        confirmMeta.setLore(Arrays.asList(
            "§7Kliknij, aby potwierdzić",
            "§7założenie działki",
            "",
            "§7Środek działki:",
            String.format("§7X: %.0f, Y: %.0f, Z: %.0f", 
                plotCenter.getX(), plotCenter.getY(), plotCenter.getZ()),
            "§7Promień: " + plugin.getPlotManager().getDefaultRadius() + " bloków"
        ));
        confirmItem.setItemMeta(confirmMeta);
        gui.setItem(11, confirmItem);

        // Cancel button
        ItemStack cancelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName("§cAnuluj");
        cancelMeta.setLore(Arrays.asList(
            "§7Kliknij, aby anulować",
            "§7założenie działki"
        ));
        cancelItem.setItemMeta(cancelMeta);
        gui.setItem(15, cancelItem);

        // Plot info
        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta infoMeta = (SkullMeta) infoItem.getItemMeta();
        infoMeta.setDisplayName("§eInformacje o działce");
        infoMeta.setLore(Arrays.asList(
            "§7Właściciel: §f" + player.getName(),
            "§7Rozmiar: §f" + (plugin.getPlotManager().getDefaultRadius() * 2 + 1) + "x" + 
                (plugin.getPlotManager().getDefaultRadius() * 2 + 1),
            "§7Promień: §f" + plugin.getPlotManager().getDefaultRadius() + " bloków",
            "",
            "§7Środek działki:",
            String.format("§7X: %.0f, Y: %.0f, Z: %.0f", 
                plotCenter.getX(), plotCenter.getY(), plotCenter.getZ())
        ));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(13, infoItem);

        player.openInventory(gui);
    }

    public void handleClick(int slot) {
        if (slot == 11) { // Confirm
            Plot plot = plugin.getPlotManager().createPlot(player, plotCenter);
            if (plot != null) {
                player.sendMessage("§aDziałka została założona pomyślnie!");
                player.closeInventory();
            } else {
                player.sendMessage("§cNie udało się założyć działki! Możliwe, że masz już działkę lub ta lokalizacja jest zajęta.");
            }
        } else if (slot == 15) { // Cancel
            player.sendMessage("§cZakładanie działki zostało anulowane.");
            player.closeInventory();
        }
    }
} 