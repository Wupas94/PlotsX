package com.plotsx.managers

import com.plotsx.models.Plot
import org.bukkit.Location
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Menedżer cache'owania danych pluginu
 * Zapewnia wydajne przechowywanie i odświeżanie często używanych danych
 * 
 * @author Wupas94
 * @since 1.0
 */
class CacheManager {
    private val plotCache = ConcurrentHashMap<String, Plot>()
    private val locationCache = ConcurrentHashMap<Location, String>()
    private val lastUpdate = ConcurrentHashMap<String, Long>()
    private val cacheTimeout = TimeUnit.MINUTES.toMillis(5)

    /**
     * Pobiera działkę z cache'u
     * @param id Identyfikator działki
     * @return Działka lub null jeśli nie jest w cache'u lub jest przestarzała
     */
    fun getPlot(id: String): Plot? {
        val lastUpdateTime = lastUpdate[id] ?: return null
        if (System.currentTimeMillis() - lastUpdateTime > cacheTimeout) {
            invalidatePlot(id)
            return null
        }
        return plotCache[id]
    }

    /**
     * Dodaje lub aktualizuje działkę w cache'u
     * @param plot Działka do zapisania
     */
    fun updatePlot(plot: Plot) {
        plotCache[plot.id] = plot
        lastUpdate[plot.id] = System.currentTimeMillis()
        plot.center?.let { locationCache[it] = plot.id }
    }

    /**
     * Usuwa działkę z cache'u
     * @param id Identyfikator działki
     */
    fun invalidatePlot(id: String) {
        plotCache.remove(id)
        lastUpdate.remove(id)
        locationCache.entries.removeIf { it.value == id }
    }

    /**
     * Pobiera ID działki na podstawie lokalizacji
     * @param location Lokalizacja do sprawdzenia
     * @return ID działki lub null jeśli nie znaleziono
     */
    fun getPlotIdAt(location: Location): String? {
        return locationCache[location]
    }

    /**
     * Czyści cały cache
     */
    fun clearCache() {
        plotCache.clear()
        locationCache.clear()
        lastUpdate.clear()
    }

    /**
     * Sprawdza czy cache jest aktualny
     * @param id Identyfikator działki
     * @return true jeśli cache jest aktualny
     */
    fun isCacheValid(id: String): Boolean {
        val lastUpdateTime = lastUpdate[id] ?: return false
        return System.currentTimeMillis() - lastUpdateTime <= cacheTimeout
    }

    /**
     * Pobiera liczbę działek w cache'u
     * @return Liczba działek
     */
    fun getCacheSize(): Int = plotCache.size
} 