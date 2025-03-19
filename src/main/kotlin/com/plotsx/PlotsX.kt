package com.plotsx

import com.plotsx.commands.PlotCommand
import com.plotsx.managers.PlotManager
import com.plotsx.managers.ProtectionManager
import com.plotsx.systems.biome.BiomeManager
import com.plotsx.systems.economy.EconomyManager
import com.plotsx.systems.flags.FlagManager
import com.plotsx.systems.levels.PlotLevelManager
import com.plotsx.systems.schematics.SchematicManager
import com.plotsx.systems.tasks.TaskManager
import com.plotsx.systems.trust.TrustManager
import com.plotsx.systems.visit.VisitManager
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin

class PlotsX : JavaPlugin() {
    lateinit var plotManager: PlotManager
        private set
    lateinit var protectionManager: ProtectionManager
        private set
    lateinit var levelManager: PlotLevelManager
        private set
    lateinit var visitManager: VisitManager
        private set
    lateinit var flagManager: FlagManager
        private set
    lateinit var schematicManager: SchematicManager
        private set
    lateinit var trustManager: TrustManager
        private set
    lateinit var taskManager: TaskManager
        private set
    lateinit var economyManager: EconomyManager
        private set
    lateinit var biomeManager: BiomeManager
        private set
    
    var economy: Economy? = null
        private set

    override fun onEnable() {
        // Create data folder
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        // Setup Vault economy
        setupEconomy()

        // Initialize managers
        plotManager = PlotManager(this)
        protectionManager = ProtectionManager(this)
        levelManager = PlotLevelManager(this)
        visitManager = VisitManager(this)
        flagManager = FlagManager(this)
        schematicManager = SchematicManager(this)
        trustManager = TrustManager(this)
        taskManager = TaskManager(this)
        economyManager = EconomyManager(this)
        biomeManager = BiomeManager(this)

        // Register commands
        getCommand("plot")?.setExecutor(PlotCommand(this))

        // Load data
        loadData()

        // Start cleanup tasks
        startCleanupTasks()

        logger.info("PlotsX has been enabled!")
    }

    override fun onDisable() {
        // Save all data
        saveData()
        
        logger.info("PlotsX has been disabled!")
    }

    private fun setupEconomy() {
        if (server.pluginManager.getPlugin("Vault") == null) {
            logger.warning("Vault not found! Economy features will be disabled.")
            return
        }

        val rsp = server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            logger.warning("No economy plugin found! Economy features will be disabled.")
            return
        }

        economy = rsp.provider
        logger.info("Successfully hooked into Vault economy!")
    }

    private fun loadData() {
        plotManager.loadData()
        levelManager.loadData()
        visitManager.loadData()
        flagManager.loadData()
        schematicManager.loadData()
        trustManager.loadData()
        taskManager.loadData()
        economyManager.loadData()
        biomeManager.loadData()
    }

    private fun saveData() {
        plotManager.saveData()
        levelManager.saveData()
        visitManager.saveData()
        flagManager.saveData()
        schematicManager.saveData()
        trustManager.saveData()
        taskManager.saveData()
        economyManager.saveData()
        biomeManager.saveData()
    }

    private fun startCleanupTasks() {
        // Cleanup expired rentals and auctions
        server.scheduler.runTaskTimer(this, {
            economyManager.cleanupExpiredRentals()
            economyManager.cleanupExpiredAuctions()
        }, 20L * 60L, 20L * 60L) // Run every minute

        // Cleanup expired trust
        server.scheduler.runTaskTimer(this, {
            trustManager.cleanupExpiredTrust()
        }, 20L * 60L, 20L * 60L) // Run every minute
    }

    companion object {
        const val PLOT_SIZE = 50 // Domyślny rozmiar działki
        const val PLOT_HEIGHT = 256 // Maksymalna wysokość działki
        
        // Stałe dla wiadomości
        const val PREFIX = "<gradient:#00b4d8:#0077b6>[PlotsX]</gradient> "
        const val SUCCESS_COLOR = "<#2ecc71>"
        const val ERROR_COLOR = "<#e74c3c>"
        const val INFO_COLOR = "<#3498db>"
    }
} 