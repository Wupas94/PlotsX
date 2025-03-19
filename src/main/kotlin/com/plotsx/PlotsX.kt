package com.plotsx

import com.plotsx.managers.PlotManager
import com.plotsx.managers.ProtectionManager
import com.plotsx.commands.PlotCommand
import net.coreprotect.CoreProtect
import net.luckperms.api.LuckPerms
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

class PlotsX : JavaPlugin() {
    lateinit var plotManager: PlotManager
        private set
    lateinit var protectionManager: ProtectionManager
        private set
    var coreProtect: CoreProtect? = null
        private set
    var luckPerms: LuckPerms? = null
        private set

    override fun onEnable() {
        saveDefaultConfig()
        
        initializeManagers()
        setupIntegrations()
        registerCommands()
        registerEventListeners()
        
        logger.info("PlotsX has been enabled!")
    }

    override fun onDisable() {
        plotManager.saveAllPlots()
        logger.info("PlotsX has been disabled!")
    }

    private fun initializeManagers() {
        plotManager = PlotManager(this)
        protectionManager = ProtectionManager(this)
    }

    private fun setupIntegrations() {
        setupCoreProtect()
        setupLuckPerms()
    }

    private fun registerCommands() {
        getCommand("plot")?.setExecutor(PlotCommand(this))
    }

    private fun registerEventListeners() {
        server.pluginManager.registerEvents(protectionManager, this)
    }

    private fun setupCoreProtect() {
        coreProtect = server.servicesManager
            .getRegistration(CoreProtect::class.java)
            ?.provider
            .also { provider ->
                if (provider != null) {
                    logger.info("CoreProtect integration enabled!")
                } else {
                    logger.warning("CoreProtect not found! Some features may be limited.")
                }
            }
    }

    private fun setupLuckPerms() {
        luckPerms = server.servicesManager
            .getRegistration(LuckPerms::class.java)
            ?.provider
            .also { provider ->
                if (provider != null) {
                    logger.info("LuckPerms integration enabled!")
                } else {
                    logger.info("LuckPerms not found. Running without permissions integration.")
                }
            }
    }

    companion object {
        const val PLOT_SIZE = 50 // Domyślny rozmiar działki
        const val PLOT_HEIGHT = 256 // Maksymalna wysokość działki
    }
} 