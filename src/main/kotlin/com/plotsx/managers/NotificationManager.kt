package com.plotsx.managers

import com.plotsx.PlotsX
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Menedżer powiadomień dla administratorów
 * Zapewnia system powiadomień o ważnych zdarzeniach w pluginie
 * 
 * @property plugin Instancja głównej klasy pluginu
 * @author Wupas94
 * @since 1.0
 */
class NotificationManager(private val plugin: PlotsX) {
    private val notifications = ConcurrentHashMap<String, MutableList<Notification>>()
    private val notificationTimeout = TimeUnit.MINUTES.toMillis(30)
    private val maxNotifications = 100

    init {
        startCleanupTask()
    }

    /**
     * Uruchamia zadanie czyszczenia starych powiadomień
     */
    private fun startCleanupTask() {
        object : BukkitRunnable() {
            override fun run() {
                cleanupOldNotifications()
            }
        }.runTaskTimerAsynchronously(plugin, TimeUnit.MINUTES.toMillis(5) / 50, TimeUnit.MINUTES.toMillis(5) / 50)
    }

    /**
     * Dodaje nowe powiadomienie
     * @param type Typ powiadomienia
     * @param message Treść powiadomienia
     * @param priority Priorytet powiadomienia
     */
    fun addNotification(type: String, message: String, priority: NotificationPriority = NotificationPriority.NORMAL) {
        val notification = Notification(message, System.currentTimeMillis(), priority)
        notifications.computeIfAbsent(type) { mutableListOf() }.add(notification)
        
        // Wysyłanie powiadomienia do online administratorów
        Bukkit.getOnlinePlayers().forEach { player ->
            if (player.hasPermission("plotsx.admin.notifications")) {
                sendNotification(player, notification)
            }
        }

        // Ograniczenie liczby powiadomień
        if (notifications[type]?.size ?: 0 > maxNotifications) {
            notifications[type]?.removeAt(0)
        }
    }

    /**
     * Wysyła powiadomienie do gracza
     * @param player Gracz
     * @param notification Powiadomienie
     */
    private fun sendNotification(player: Player, notification: Notification) {
        val color = when (notification.priority) {
            NotificationPriority.HIGH -> ChatColor.RED
            NotificationPriority.NORMAL -> ChatColor.YELLOW
            NotificationPriority.LOW -> ChatColor.GREEN
        }

        player.sendMessage("${ChatColor.GOLD}[PlotsX] ${color}${notification.message}")
    }

    /**
     * Pobiera powiadomienia danego typu
     * @param type Typ powiadomienia
     * @return Lista powiadomień
     */
    fun getNotifications(type: String): List<Notification> {
        return notifications[type]?.filter { isNotificationValid(it) } ?: emptyList()
    }

    /**
     * Pobiera wszystkie powiadomienia
     * @return Mapa powiadomień
     */
    fun getAllNotifications(): Map<String, List<Notification>> {
        return notifications.mapValues { (_, list) -> list.filter { isNotificationValid(it) } }
    }

    /**
     * Sprawdza czy powiadomienie jest aktualne
     * @param notification Powiadomienie
     * @return true jeśli powiadomienie jest aktualne
     */
    private fun isNotificationValid(notification: Notification): Boolean {
        return System.currentTimeMillis() - notification.timestamp <= notificationTimeout
    }

    /**
     * Czyści stare powiadomienia
     */
    private fun cleanupOldNotifications() {
        notifications.forEach { (type, list) ->
            list.removeAll { !isNotificationValid(it) }
            if (list.isEmpty()) {
                notifications.remove(type)
            }
        }
    }

    /**
     * Czyści wszystkie powiadomienia
     */
    fun clearNotifications() {
        notifications.clear()
    }

    /**
     * Klasa reprezentująca pojedyncze powiadomienie
     */
    data class Notification(
        val message: String,
        val timestamp: Long,
        val priority: NotificationPriority
    )

    /**
     * Enum reprezentujący priorytet powiadomienia
     */
    enum class NotificationPriority {
        HIGH, NORMAL, LOW
    }
} 