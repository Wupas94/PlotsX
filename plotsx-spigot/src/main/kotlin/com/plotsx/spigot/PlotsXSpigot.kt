package com.plotsx.spigot

import com.plotsx.api.PlotsXPlugin
import com.plotsx.api.manager.PlotManager
import com.plotsx.api.manager.ProtectionManager
import com.plotsx.spigot.command.PlotCommand
import com.plotsx.spigot.manager.SpigotPlotManager
import com.plotsx.spigot.manager.SpigotProtectionManager
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

class PlotsXSpigot : JavaPlugin(), PlotsXPlugin {
    override lateinit var plotManager: PlotManager
        private set
    override lateinit var protectionManager: ProtectionManager
        private set
    override lateinit var adventure: BukkitAudiences
        private set
    override lateinit var miniMessage: MiniMessage
        private set

    override fun onEnable() {
        // Inicjalizacja Adventure API
        adventure = BukkitAudiences.create(this)
        miniMessage = MiniMessage.miniMessage()
        
        // Inicjalizacja managerów
        plotManager = SpigotPlotManager(this)
        protectionManager = SpigotProtectionManager(this)
        
        // Rejestracja komend
        getCommand("plot")?.setExecutor(PlotCommand(this))
        
        // Rejestracja listenerów
        server.pluginManager.registerEvents(protectionManager as SpigotProtectionManager, this)
        
        logger.info("PlotsX Spigot has been enabled!")
    }

    override fun onDisable() {
        plotManager.saveAllPlots()
        
        if (::adventure.isInitialized) {
            adventure.close()
        }
        
        logger.info("PlotsX Spigot has been disabled!")
    }
} 