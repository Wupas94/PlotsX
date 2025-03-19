package com.plotsx

import com.plotsx.commands.PlotCommand
import com.plotsx.integrations.CoreProtectIntegration
import com.plotsx.managers.*
import com.plotsx.models.Plot
import com.plotsx.utils.*
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

/**
 * Główna klasa pluginu PlotsX
 * 
 * @author Wupas94
 * @since 1.0
 */
class PlotsX : JavaPlugin() {
    // Instancje menedżerów
    lateinit var plotManager: PlotManager
        private set
    lateinit var protectionManager: ProtectionManager
        private set
    lateinit var schematicManager: SchematicManager
        private set
    lateinit var taskManager: TaskManager
        private set
    lateinit var trustManager: TrustManager
        private set
    lateinit var visitManager: VisitManager
        private set
    lateinit var weatherManager: WeatherManager
        private set
    lateinit var worldManager: WorldManager
        private set
    lateinit var coreProtectIntegration: CoreProtectIntegration
        private set
    lateinit var cacheManager: CacheManager
        private set
    lateinit var asyncDataManager: AsyncDataManager
        private set
    lateinit var metricsManager: MetricsManager
        private set
    lateinit var backupManager: BackupManager
        private set
    lateinit var notificationManager: NotificationManager
        private set

    // Instancje narzędzi
    lateinit var database: Database
        private set
    lateinit var messages: Messages
        private set
    lateinit var permissions: Permissions
        private set
    lateinit var settings: Settings
        private set

    override fun onEnable() {
        // Inicjalizacja narzędzi
        settings = Settings(this)
        messages = Messages(this)
        permissions = Permissions(this)
        database = Database(this)

        // Inicjalizacja menedżerów
        plotManager = PlotManager(this)
        protectionManager = ProtectionManager(this)
        schematicManager = SchematicManager(this)
        taskManager = TaskManager(this)
        trustManager = TrustManager(this)
        visitManager = VisitManager(this)
        weatherManager = WeatherManager(this)
        worldManager = WorldManager(this)
        coreProtectIntegration = CoreProtectIntegration(this)
        cacheManager = CacheManager()
        asyncDataManager = AsyncDataManager(this)
        metricsManager = MetricsManager(this)
        backupManager = BackupManager(this)
        notificationManager = NotificationManager(this)

        // Rejestracja komend
        getCommand("plot")?.setExecutor(PlotCommand(this))

        // Inicjalizacja integracji
        if (!coreProtectIntegration.initialize()) {
            notificationManager.addNotification(
                "integration",
                "Nie udało się zainicjalizować integracji z CoreProtect!",
                NotificationManager.NotificationPriority.HIGH
            )
        }

        // Załadowanie danych
        loadData()

        // Uruchomienie zadań
        startTasks()

        logger.info("PlotsX został pomyślnie włączony!")
    }

    override fun onDisable() {
        // Zatrzymanie zadań
        stopTasks()

        // Zapisanie danych
        saveData()

        // Zatrzymanie menedżerów
        asyncDataManager.shutdown()
        metricsManager.clearMetrics()
        notificationManager.clearNotifications()

        logger.info("PlotsX został pomyślnie wyłączony!")
    }

    /**
     * Ładuje dane pluginu
     */
    private fun loadData() {
        val startTime = System.currentTimeMillis()
        
        try {
            // Załadowanie działek
            val plots = database.loadAllPlots()
            plots.forEach { plot ->
                cacheManager.updatePlot(plot)
                plotManager.registerPlot(plot)
            }

            // Załadowanie schematów
            schematicManager.loadSchematics()

            // Załadowanie zadań
            taskManager.loadTasks()

            // Załadowanie ufanych graczy
            trustManager.loadTrustedPlayers()

            // Załadowanie wizyt
            visitManager.loadVisits()

            val loadTime = System.currentTimeMillis() - startTime
            metricsManager.recordTiming("data_load", loadTime)
            logger.info("Dane zostały załadowane w ${loadTime}ms")
        } catch (e: Exception) {
            metricsManager.recordError("data_load_error")
            notificationManager.addNotification(
                "error",
                "Błąd podczas ładowania danych: ${e.message}",
                NotificationManager.NotificationPriority.HIGH
            )
            logger.severe("Błąd podczas ładowania danych: ${e.message}")
        }
    }

    /**
     * Zapisuje dane pluginu
     */
    private fun saveData() {
        val startTime = System.currentTimeMillis()
        
        try {
            // Zapisanie działek
            plotManager.getAllPlots().forEach { plot ->
                asyncDataManager.queueSave(plot)
            }

            // Zapisanie schematów
            schematicManager.saveSchematics()

            // Zapisanie zadań
            taskManager.saveTasks()

            // Zapisanie ufanych graczy
            trustManager.saveTrustedPlayers()

            // Zapisanie wizyt
            visitManager.saveVisits()

            val saveTime = System.currentTimeMillis() - startTime
            metricsManager.recordTiming("data_save", saveTime)
            logger.info("Dane zostały zapisane w ${saveTime}ms")
        } catch (e: Exception) {
            metricsManager.recordError("data_save_error")
            notificationManager.addNotification(
                "error",
                "Błąd podczas zapisywania danych: ${e.message}",
                NotificationManager.NotificationPriority.HIGH
            )
            logger.severe("Błąd podczas zapisywania danych: ${e.message}")
        }
    }

    /**
     * Uruchamia zadania pluginu
     */
    private fun startTasks() {
        // Zadanie aktualizacji pogody
        object : org.bukkit.scheduler.BukkitRunnable() {
            override fun run() {
                weatherManager.updateWeather()
            }
        }.runTaskTimer(this, 20L, 20L)

        // Zadanie aktualizacji zadań
        object : org.bukkit.scheduler.BukkitRunnable() {
            override fun run() {
                taskManager.updateTasks()
            }
        }.runTaskTimer(this, 20L, 20L)

        // Zadanie aktualizacji wizyt
        object : org.bukkit.scheduler.BukkitRunnable() {
            override fun run() {
                visitManager.updateVisits()
            }
        }.runTaskTimer(this, 20L, 20L)

        // Zadanie aktualizacji metryk
        object : org.bukkit.scheduler.BukkitRunnable() {
            override fun run() {
                metricsManager.updateMetrics()
            }
        }.runTaskTimerAsynchronously(this, 20L, 20L)

        // Zadanie sprawdzania stanu pluginu
        object : org.bukkit.scheduler.BukkitRunnable() {
            override fun run() {
                if (!metricsManager.isHealthy()) {
                    notificationManager.addNotification(
                        "health",
                        "Plugin wykazuje problemy z wydajnością!",
                        NotificationManager.NotificationPriority.HIGH
                    )
                }
            }
        }.runTaskTimerAsynchronously(this, TimeUnit.MINUTES.toMillis(5) / 50, TimeUnit.MINUTES.toMillis(5) / 50)
    }

    /**
     * Zatrzymuje zadania pluginu
     */
    private fun stopTasks() {
        // Zatrzymanie wszystkich zadań
        server.scheduler.cancelTasks(this)
    }

    /**
     * Pobiera raport o stanie pluginu
     * @return Tekst raportu
     */
    fun getStatusReport(): String = metricsManager.generateReport()
} 