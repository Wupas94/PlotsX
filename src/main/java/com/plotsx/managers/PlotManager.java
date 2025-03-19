package com.plotsx.managers;

import com.plotsx.PlotsX;
import com.plotsx.models.Plot;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlotManager {
    private final PlotsX plugin;
    private final Map<UUID, Plot> plots;
    private final File plotsFile;
    private final FileConfiguration plotsConfig;
    private final int defaultRadius;

    public PlotManager(PlotsX plugin) {
        this.plugin = plugin;
        this.plots = new HashMap<>();
        this.plotsFile = new File(plugin.getDataFolder(), "plots.yml");
        this.plotsConfig = YamlConfiguration.loadConfiguration(plotsFile);
        this.defaultRadius = plugin.getConfig().getInt("default-plot-radius", 16);
        
        loadPlots();
    }

    private void loadPlots() {
        if (!plotsFile.exists()) {
            plugin.saveResource("plots.yml", false);
        }

        for (String key : plotsConfig.getKeys(false)) {
            UUID plotId = UUID.fromString(key);
            UUID owner = UUID.fromString(plotsConfig.getString(key + ".owner"));
            Location center = (Location) plotsConfig.get(key + ".center");
            int radius = plotsConfig.getInt(key + ".radius");
            long creationDate = plotsConfig.getLong(key + ".creationDate");
            boolean active = plotsConfig.getBoolean(key + ".active");
            boolean pvpEnabled = plotsConfig.getBoolean(key + ".flags.pvp", false);
            boolean mobDamageEnabled = plotsConfig.getBoolean(key + ".flags.mob_damage", false);
            boolean basicInteractionsEnabled = plotsConfig.getBoolean(key + ".flags.basic_interactions", true);

            Plot plot = new Plot(owner, center, radius);
            plot.setActive(active);
            plot.setPvpEnabled(pvpEnabled);
            plot.setMobDamageEnabled(mobDamageEnabled);
            plot.setBasicInteractionsEnabled(basicInteractionsEnabled);

            // Load co-owners
            List<String> coOwnersList = plotsConfig.getStringList(key + ".coOwners");
            for (String coOwnerId : coOwnersList) {
                plot.addCoOwner(UUID.fromString(coOwnerId));
            }

            plots.put(plotId, plot);
        }
    }

    public void saveAllPlots() {
        for (Map.Entry<UUID, Plot> entry : plots.entrySet()) {
            Plot plot = entry.getValue();
            String path = entry.getKey().toString();

            plotsConfig.set(path + ".owner", plot.getOwner().toString());
            plotsConfig.set(path + ".center", plot.getCenter());
            plotsConfig.set(path + ".radius", plot.getRadius());
            plotsConfig.set(path + ".creationDate", plot.getCreationDate());
            plotsConfig.set(path + ".active", plot.isActive());
            
            // Save flags
            plotsConfig.set(path + ".flags.pvp", plot.isPvpEnabled());
            plotsConfig.set(path + ".flags.mob_damage", plot.isMobDamageEnabled());
            plotsConfig.set(path + ".flags.basic_interactions", plot.isBasicInteractionsEnabled());

            List<String> coOwnersList = new ArrayList<>();
            for (UUID coOwnerId : plot.getCoOwners()) {
                coOwnersList.add(coOwnerId.toString());
            }
            plotsConfig.set(path + ".coOwners", coOwnersList);
        }

        try {
            plotsConfig.save(plotsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save plots.yml!");
            e.printStackTrace();
        }
    }

    public Plot createPlot(Player player, Location center) {
        // Check if player already has a plot
        if (getPlayerPlot(player.getUniqueId()) != null) {
            return null;
        }

        // Check if location is already in a plot
        if (getPlotAt(center) != null) {
            return null;
        }

        Plot plot = new Plot(player.getUniqueId(), center, defaultRadius);
        plots.put(plot.getId(), plot);
        saveAllPlots();
        return plot;
    }

    public void deletePlot(UUID plotId) {
        plots.remove(plotId);
        saveAllPlots();
    }

    public Plot getPlotAt(Location location) {
        for (Plot plot : plots.values()) {
            if (plot.isInPlot(location)) {
                return plot;
            }
        }
        return null;
    }

    public Plot getPlayerPlot(UUID playerId) {
        for (Plot plot : plots.values()) {
            if (plot.isOwner(playerId)) {
                return plot;
            }
        }
        return null;
    }

    public List<Plot> getPlayerPlots(UUID playerId) {
        List<Plot> playerPlots = new ArrayList<>();
        for (Plot plot : plots.values()) {
            if (plot.hasAccess(playerId)) {
                playerPlots.add(plot);
            }
        }
        return playerPlots;
    }

    public boolean isLocationInAnyPlot(Location location) {
        return getPlotAt(location) != null;
    }

    public int getDefaultRadius() {
        return defaultRadius;
    }
} 