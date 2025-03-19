package com.plotsx;

import com.plotsx.managers.PlotManager;
import com.plotsx.managers.ProtectionManager;
import com.plotsx.commands.PlotCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.coreprotect.CoreProtect;
import net.luckperms.api.LuckPerms;

public class PlotsX extends JavaPlugin {
    private static PlotsX instance;
    private PlotManager plotManager;
    private ProtectionManager protectionManager;
    private CoreProtect coreProtect;
    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        this.plotManager = new PlotManager(this);
        this.protectionManager = new ProtectionManager(this);
        
        // Setup CoreProtect
        setupCoreProtect();
        
        // Setup LuckPerms
        setupLuckPerms();
        
        // Register commands
        getCommand("plot").setExecutor(new PlotCommand(this));
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(protectionManager, this);
        
        getLogger().info("PlotsX has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all plots
        if (plotManager != null) {
            plotManager.saveAllPlots();
        }
        
        getLogger().info("PlotsX has been disabled!");
    }

    private void setupCoreProtect() {
        RegisteredServiceProvider<CoreProtect> provider = getServer().getServicesManager().getRegistration(CoreProtect.class);
        if (provider != null) {
            this.coreProtect = provider.getProvider();
            getLogger().info("CoreProtect integration enabled!");
        } else {
            getLogger().warning("CoreProtect not found! Some features may be limited.");
        }
    }

    private void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            getLogger().info("LuckPerms integration enabled!");
        } else {
            getLogger().info("LuckPerms not found. Running without permissions integration.");
        }
    }

    public static PlotsX getInstance() {
        return instance;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public CoreProtect getCoreProtect() {
        return coreProtect;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
} 