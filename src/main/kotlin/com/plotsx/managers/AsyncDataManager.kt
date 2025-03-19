package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * Menedżer asynchronicznych operacji na danych
 * Zapewnia wydajne zapisywanie i odczytywanie danych bez blokowania głównego wątku
 * 
 * @property plugin Instancja głównej klasy pluginu
 * @author Wupas94
 * @since 1.0
 */
class AsyncDataManager(private val plugin: PlotsX) {
    private val saveQueue = ConcurrentLinkedQueue<Plot>()
    private val loadQueue = ConcurrentLinkedQueue<String>()
    private var isSaving = false
    private var isLoading = false
    private val saveInterval = TimeUnit.MINUTES.toMillis(5)

    init {
        startSaveTask()
    }

    /**
     * Uruchamia zadanie okresowego zapisywania danych
     */
    private fun startSaveTask() {
        object : BukkitRunnable() {
            override fun run() {
                processSaveQueue()
            }
        }.runTaskTimerAsynchronously(plugin, saveInterval / 50, saveInterval / 50)
    }

    /**
     * Dodaje działkę do kolejki zapisywania
     * @param plot Działka do zapisania
     */
    fun queueSave(plot: Plot) {
        saveQueue.offer(plot)
        if (!isSaving) {
            processSaveQueue()
        }
    }

    /**
     * Dodaje ID działki do kolejki ładowania
     * @param id Identyfikator działki
     */
    fun queueLoad(id: String) {
        loadQueue.offer(id)
        if (!isLoading) {
            processLoadQueue()
        }
    }

    /**
     * Przetwarza kolejkę zapisywania
     */
    private fun processSaveQueue() {
        if (saveQueue.isEmpty()) {
            isSaving = false
            return
        }

        isSaving = true
        CompletableFuture.runAsync {
            val plot = saveQueue.poll()
            if (plot != null) {
                plugin.database.savePlot(plot)
            }
            processSaveQueue()
        }
    }

    /**
     * Przetwarza kolejkę ładowania
     */
    private fun processLoadQueue() {
        if (loadQueue.isEmpty()) {
            isLoading = false
            return
        }

        isLoading = true
        CompletableFuture.runAsync {
            val id = loadQueue.poll()
            if (id != null) {
                val plot = plugin.database.loadPlot(id)
                if (plot != null) {
                    plugin.cacheManager.updatePlot(plot)
                }
            }
            processLoadQueue()
        }
    }

    /**
     * Zapisuje wszystkie oczekujące dane
     * @return CompletableFuture z informacją o zakończeniu
     */
    fun saveAll(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            while (saveQueue.isNotEmpty()) {
                processSaveQueue()
                Thread.sleep(100)
            }
        }
    }

    /**
     * Ładuje wszystkie oczekujące dane
     * @return CompletableFuture z informacją o zakończeniu
     */
    fun loadAll(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            while (loadQueue.isNotEmpty()) {
                processLoadQueue()
                Thread.sleep(100)
            }
        }
    }

    /**
     * Zatrzymuje wszystkie zadania asynchroniczne
     */
    fun shutdown() {
        saveAll().join()
        loadAll().join()
    }
} 