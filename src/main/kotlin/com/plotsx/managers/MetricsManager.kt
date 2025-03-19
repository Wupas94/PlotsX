package com.plotsx.managers

import com.plotsx.PlotsX
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Menedżer metryk i monitorowania pluginu
 * Zapewnia zbieranie i analizę statystyk działania pluginu
 * 
 * @property plugin Instancja głównej klasy pluginu
 * @author Wupas94
 * @since 1.0
 */
class MetricsManager(private val plugin: PlotsX) {
    private val metrics = ConcurrentHashMap<String, Long>()
    private val timings = ConcurrentHashMap<String, MutableList<Long>>()
    private val errors = ConcurrentHashMap<String, Int>()
    private val startTime = System.currentTimeMillis()
    private val updateInterval = TimeUnit.MINUTES.toMillis(1)

    init {
        startMetricsTask()
    }

    /**
     * Uruchamia zadanie okresowego aktualizowania metryk
     */
    private fun startMetricsTask() {
        object : BukkitRunnable() {
            override fun run() {
                updateMetrics()
            }
        }.runTaskTimerAsynchronously(plugin, updateInterval / 50, updateInterval / 50)
    }

    /**
     * Aktualizuje metryki pluginu
     */
    private fun updateMetrics() {
        val uptime = System.currentTimeMillis() - startTime
        metrics["uptime"] = uptime
        metrics["plots"] = plugin.plotManager.getPlotCount().toLong()
        metrics["players"] = plugin.server.onlinePlayers.size.toLong()
        metrics["cache_size"] = plugin.cacheManager.getCacheSize().toLong()
        metrics["memory_usage"] = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }

    /**
     * Zapisuje czas wykonania operacji
     * @param operation Nazwa operacji
     * @param time Czas wykonania w milisekundach
     */
    fun recordTiming(operation: String, time: Long) {
        timings.computeIfAbsent(operation) { mutableListOf() }.add(time)
    }

    /**
     * Zapisuje błąd
     * @param type Typ błędu
     */
    fun recordError(type: String) {
        errors.compute(type) { _, count -> (count ?: 0) + 1 }
    }

    /**
     * Zwiększa licznik operacji
     * @param operation Nazwa operacji
     */
    fun incrementMetric(operation: String) {
        metrics.compute(operation) { _, count -> (count ?: 0) + 1 }
    }

    /**
     * Pobiera statystyki pluginu
     * @return Mapa ze statystykami
     */
    fun getStats(): Map<String, Any> {
        val stats = mutableMapOf<String, Any>()
        
        // Podstawowe metryki
        stats.putAll(metrics)
        
        // Średnie czasy operacji
        timings.forEach { (operation, times) ->
            if (times.isNotEmpty()) {
                stats["${operation}_avg_time"] = times.average()
                stats["${operation}_max_time"] = times.maxOrNull() ?: 0
                stats["${operation}_min_time"] = times.minOrNull() ?: 0
            }
        }
        
        // Liczba błędów
        stats.putAll(errors)
        
        // Wydajność
        stats["memory_usage_mb"] = metrics["memory_usage"]?.div(1024 * 1024) ?: 0
        stats["uptime_hours"] = metrics["uptime"]?.div(TimeUnit.HOURS.toMillis(1)) ?: 0
        
        return stats
    }

    /**
     * Czyści wszystkie metryki
     */
    fun clearMetrics() {
        metrics.clear()
        timings.clear()
        errors.clear()
    }

    /**
     * Sprawdza czy plugin działa poprawnie
     * @return true jeśli wszystko jest w porządku
     */
    fun isHealthy(): Boolean {
        val errorCount = errors.values.sum()
        val memoryUsage = metrics["memory_usage"] ?: 0
        val maxMemory = Runtime.getRuntime().maxMemory()
        
        return errorCount < 100 && memoryUsage < maxMemory * 0.9
    }

    /**
     * Generuje raport o stanie pluginu
     * @return Tekst raportu
     */
    fun generateReport(): String {
        val stats = getStats()
        return buildString {
            appendLine("=== Raport stanu PlotsX ===")
            appendLine("Czas działania: ${stats["uptime_hours"]}h")
            appendLine("Liczba działek: ${stats["plots"]}")
            appendLine("Liczba graczy: ${stats["players"]}")
            appendLine("Użycie pamięci: ${stats["memory_usage_mb"]}MB")
            appendLine("Rozmiar cache: ${stats["cache_size"]}")
            appendLine("Liczba błędów: ${errors.values.sum()}")
            appendLine("Status: ${if (isHealthy()) "OK" else "UWAGA"}")
        }
    }
} 