package com.plotsx.managers;

import com.plotsx.PlotsX;
import com.plotsx.models.Plot;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;

public class ProtectionManager implements Listener {
    private final PlotsX plugin;

    public ProtectionManager(PlotsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Plot plot = plugin.getPlotManager().getPlotAt(event.getBlock().getLocation());
        
        if (plot != null && !plot.hasAccess(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cNie możesz niszczyć bloków na tej działce!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Plot plot = plugin.getPlotManager().getPlotAt(event.getBlock().getLocation());
        
        if (plot != null && !plot.hasAccess(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cNie możesz stawiać bloków na tej działce!");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Plot plot = plugin.getPlotManager().getPlotAt(event.getBlock().getLocation());
        
        if (plot != null && !plot.hasAccess(player.getUniqueId())) {
            // Check if basic interactions are enabled
            if (plot.isBasicInteractionsEnabled() && 
                event.getAction().name().contains("RIGHT_CLICK") && 
                (event.getBlock().getType().name().contains("DOOR") || 
                 event.getBlock().getType().name().contains("GATE") ||
                 event.getBlock().getType().name().contains("BUTTON") ||
                 event.getBlock().getType().name().contains("LEVER"))) {
                return;
            }
            
            event.setCancelled(true);
            player.sendMessage("§cNie możesz używać przedmiotów na tej działce!");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        Plot plot = plugin.getPlotManager().getPlotAt(event.getEntity().getLocation());
        
        if (plot != null) {
            // Check PvP flag
            if (!plot.isPvpEnabled() && event.getEntity() instanceof Player) {
                event.setCancelled(true);
                player.sendMessage("§cPvP jest wyłączone na tej działce!");
                return;
            }
            
            // Check mob damage flag
            if (!plot.isMobDamageEnabled() && !(event.getEntity() instanceof Player)) {
                event.setCancelled(true);
                player.sendMessage("§cAtaki na moby są wyłączone na tej działce!");
                return;
            }
        }
    }

    public boolean canPlayerAccessPlot(Player player, Plot plot) {
        if (plot == null) {
            return true;
        }
        return plot.hasAccess(player.getUniqueId());
    }
} 