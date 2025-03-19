package com.plotsx.commands;

import com.plotsx.PlotsX;
import com.plotsx.gui.PlotClaimGUI;
import com.plotsx.gui.PlotFlagsGUI;
import com.plotsx.models.Plot;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlotCommand implements CommandExecutor {
    private final PlotsX plugin;

    public PlotCommand(PlotsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Ta komenda może być użyta tylko przez gracza!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Default claim command
            if (plugin.getPlotManager().getPlayerPlot(player.getUniqueId()) != null) {
                player.sendMessage(ChatColor.RED + "Masz już założoną działkę!");
                return true;
            }

            Location center = player.getLocation();
            if (plugin.getPlotManager().isLocationInAnyPlot(center)) {
                player.sendMessage(ChatColor.RED + "Ta lokalizacja jest już zajęta przez inną działkę!");
                return true;
            }

            new PlotClaimGUI(plugin, player, center).open();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                handleInfo(player);
                break;
            case "delete":
                handleDelete(player);
                break;
            case "add":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Użyj: /plot add <gracz>");
                    return true;
                }
                handleAdd(player, args[1]);
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Użyj: /plot remove <gracz>");
                    return true;
                }
                handleRemove(player, args[1]);
                break;
            case "flags":
                handleFlags(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleInfo(Player player) {
        Plot plot = plugin.getPlotManager().getPlayerPlot(player.getUniqueId());
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Nie masz założonej działki!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Informacje o działce ===");
        player.sendMessage(ChatColor.YELLOW + "Właściciel: " + ChatColor.WHITE + player.getName());
        player.sendMessage(ChatColor.YELLOW + "Rozmiar: " + ChatColor.WHITE + 
            (plot.getRadius() * 2 + 1) + "x" + (plot.getRadius() * 2 + 1));
        player.sendMessage(ChatColor.YELLOW + "Promień: " + ChatColor.WHITE + plot.getRadius() + " bloków");
        player.sendMessage(ChatColor.YELLOW + "Środek: " + ChatColor.WHITE + 
            String.format("X: %.0f, Y: %.0f, Z: %.0f", 
                plot.getCenter().getX(), plot.getCenter().getY(), plot.getCenter().getZ()));
        
        player.sendMessage(ChatColor.YELLOW + "Flagi:");
        player.sendMessage(ChatColor.WHITE + "- PvP: " + (plot.isPvpEnabled() ? ChatColor.GREEN + "Włączone" : ChatColor.RED + "Wyłączone"));
        player.sendMessage(ChatColor.WHITE + "- Ochrona przed mobami: " + (plot.isMobDamageEnabled() ? ChatColor.GREEN + "Włączone" : ChatColor.RED + "Wyłączone"));
        player.sendMessage(ChatColor.WHITE + "- Podstawowe interakcje: " + (plot.isBasicInteractionsEnabled() ? ChatColor.GREEN + "Włączone" : ChatColor.RED + "Wyłączone"));
        
        if (!plot.getCoOwners().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Współwłaściciele:");
            for (UUID coOwnerId : plot.getCoOwners()) {
                String coOwnerName = plugin.getServer().getOfflinePlayer(coOwnerId).getName();
                player.sendMessage(ChatColor.WHITE + "- " + coOwnerName);
            }
        }
    }

    private void handleDelete(Player player) {
        Plot plot = plugin.getPlotManager().getPlayerPlot(player.getUniqueId());
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Nie masz założonej działki!");
            return;
        }

        plugin.getPlotManager().deletePlot(plot.getId());
        player.sendMessage(ChatColor.GREEN + "Działka została usunięta!");
    }

    private void handleAdd(Player player, String targetName) {
        Plot plot = plugin.getPlotManager().getPlayerPlot(player.getUniqueId());
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Nie masz założonej działki!");
            return;
        }

        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Nie znaleziono gracza!");
            return;
        }

        if (plot.isCoOwner(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Ten gracz jest już współwłaścicielem działki!");
            return;
        }

        plot.addCoOwner(target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Dodano " + target.getName() + " jako współwłaściciela działki!");
        target.sendMessage(ChatColor.GREEN + "Zostałeś dodany jako współwłaściciel działki gracza " + player.getName() + "!");
    }

    private void handleRemove(Player player, String targetName) {
        Plot plot = plugin.getPlotManager().getPlayerPlot(player.getUniqueId());
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Nie masz założonej działki!");
            return;
        }

        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Nie znaleziono gracza!");
            return;
        }

        if (!plot.isCoOwner(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Ten gracz nie jest współwłaścicielem działki!");
            return;
        }

        plot.removeCoOwner(target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Usunięto " + target.getName() + " ze współwłaścicieli działki!");
        target.sendMessage(ChatColor.RED + "Zostałeś usunięty ze współwłaścicieli działki gracza " + player.getName() + "!");
    }

    private void handleFlags(Player player) {
        Plot plot = plugin.getPlotManager().getPlayerPlot(player.getUniqueId());
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Nie masz założonej działki!");
            return;
        }

        new PlotFlagsGUI(plugin, player, plot).open();
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== PlotsX - Pomoc ===");
        player.sendMessage(ChatColor.YELLOW + "/plot " + ChatColor.WHITE + "- Załóż nową działkę");
        player.sendMessage(ChatColor.YELLOW + "/plot info " + ChatColor.WHITE + "- Wyświetl informacje o swojej działce");
        player.sendMessage(ChatColor.YELLOW + "/plot delete " + ChatColor.WHITE + "- Usuń swoją działkę");
        player.sendMessage(ChatColor.YELLOW + "/plot add <gracz> " + ChatColor.WHITE + "- Dodaj współwłaściciela działki");
        player.sendMessage(ChatColor.YELLOW + "/plot remove <gracz> " + ChatColor.WHITE + "- Usuń współwłaściciela działki");
        player.sendMessage(ChatColor.YELLOW + "/plot flags " + ChatColor.WHITE + "- Zarządzaj flagami działki");
    }
} 