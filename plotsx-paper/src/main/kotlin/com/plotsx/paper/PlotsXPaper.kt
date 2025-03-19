package com.plotsx.paper

import com.plotsx.api.PlotsXPlugin
import com.plotsx.api.manager.PlotManager
import com.plotsx.api.manager.ProtectionManager
import com.plotsx.paper.command.PlotCommand
import com.plotsx.paper.manager.PaperPlotManager
import com.plotsx.paper.manager.PaperProtectionManager
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

class PlotsXPaper : JavaPlugin(), PlotsXPlugin {
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
        plotManager = PaperPlotManager(this)
        protectionManager = PaperProtectionManager(this)
        
        // Rejestracja komend
        getCommand("plot")?.setExecutor(PlotCommand(this))
        
        // Rejestracja listenerów
        server.pluginManager.registerEvents(protectionManager as PaperProtectionManager, this)
        
        logger.info("PlotsX Paper has been enabled!")
    }

    override fun onDisable() {
        plotManager.saveAllPlots()
        
        if (::adventure.isInitialized) {
            adventure.close()
        }
        
        logger.info("PlotsX Paper has been disabled!")
    }
} 