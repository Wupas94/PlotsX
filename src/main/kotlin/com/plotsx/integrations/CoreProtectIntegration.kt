package com.plotsx.integrations

import com.plotsx.PlotsX
import net.coreprotect.CoreProtect
import net.coreprotect.CoreProtectAPI
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Integracja z pluginem CoreProtect
 * Zapewnia funkcjonalność logowania i zarządzania historią zmian na działkach.
 * 
 * @property plugin Instancja głównej klasy pluginu
 * @author Wupas94
 * @since 1.0
 */
class CoreProtectIntegration(private val plugin: PlotsX) {
    private var coreProtect: CoreProtectAPI? = null

    companion object {
        // Permission messages
        private const val NO_PERMISSION_LOOKUP = "§cNie masz uprawnień do sprawdzania historii bloków!"
        private const val NO_PERMISSION_ROLLBACK = "§cNie masz uprawnień do cofania zmian!"
        private const val NO_PERMISSION_RESTORE = "§cNie masz uprawnień do przywracania zmian!"
        private const val NO_PERMISSION_INSPECT = "§cNie masz uprawnień do używania inspektora!"
        
        // Success messages
        private const val SUCCESS_ROLLBACK = "§aRozpoczęto cofanie zmian..."
        private const val SUCCESS_RESTORE = "§aRozpoczęto przywracanie zmian..."
        private const val SUCCESS_INSPECT_ENABLED = "§aWłączono tryb inspektora!"
        private const val SUCCESS_INSPECT_DISABLED = "§cWyłączono tryb inspektora!"
        
        // Error messages
        private const val ERROR_NO_COREPROTECT = "§cNie znaleziono pluginu CoreProtect! Funkcje logowania będą ograniczone."
        private const val ERROR_INVALID_VERSION = "§cNiekompatybilna wersja CoreProtect! Zaktualizuj plugin do najnowszej wersji."
        private const val ERROR_API_NOT_ENABLED = "§cAPI CoreProtect nie jest włączone!"
        
        // Success messages
        private const val SUCCESS_INIT = "§aPoprawnie połączono z CoreProtect!"
        
        // Default values
        private const val DEFAULT_LOOKUP_TIME = 86400 // 24 hours in seconds
        private const val MIN_API_VERSION = 9
    }

    /**
     * Inicjalizuje integrację z CoreProtect
     * @return true jeśli inicjalizacja się powiodła
     */
    fun initialize(): Boolean {
        val coreProtectPlugin: Plugin? = plugin.server.pluginManager.getPlugin("CoreProtect")
        
        if (coreProtectPlugin == null || coreProtectPlugin !is CoreProtect) {
            plugin.logger.warning(ERROR_NO_COREPROTECT)
            return false
        }

        val api = coreProtectPlugin.api
        if (api == null || api.APIVersion() < MIN_API_VERSION) {
            plugin.logger.warning(ERROR_INVALID_VERSION)
            return false
        }

        if (!api.isEnabled) {
            plugin.logger.warning(ERROR_API_NOT_ENABLED)
            return false
        }

        coreProtect = api
        plugin.logger.info(SUCCESS_INIT)
        return true
    }

    /**
     * Loguje postawienie bloku
     * @param player Gracz, który postawił blok
     * @param block Postawiony blok
     * @return true jeśli operacja się powiodła
     */
    fun logBlockPlace(player: Player, block: Block): Boolean =
        coreProtect?.logPlacement(player.name, block.location, block.type, block.blockData) ?: false

    /**
     * Loguje zniszczenie bloku
     * @param player Gracz, który zniszczył blok
     * @param block Zniszczony blok
     * @return true jeśli operacja się powiodła
     */
    fun logBlockBreak(player: Player, block: Block): Boolean =
        coreProtect?.logRemoval(player.name, block.location, block.type, block.blockData) ?: false

    /**
     * Loguje interakcję z kontenerem
     * @param player Gracz, który wchodzi w interakcję
     * @param block Kontener
     * @return true jeśli operacja się powiodła
     */
    fun logContainerTransaction(player: Player, block: Block): Boolean =
        coreProtect?.logContainerTransaction(player.name, block.location) ?: false

    /**
     * Loguje wiadomość na czacie
     * @param player Gracz, który wysłał wiadomość
     * @param message Treść wiadomości
     * @return true jeśli operacja się powiodła
     */
    fun logChat(player: Player, message: String): Boolean =
        coreProtect?.logChat(player.name, message) ?: false

    /**
     * Loguje użycie komendy
     * @param player Gracz, który użył komendy
     * @param command Użyta komenda
     * @return true jeśli operacja się powiodła
     */
    fun logCommand(player: Player, command: String): Boolean =
        coreProtect?.logCommand(player.name, command) ?: false

    /**
     * Pobiera historię zmian bloku
     * @param player Gracz sprawdzający historię
     * @param location Lokalizacja bloku
     * @param time Czas w sekundach (domyślnie 24h)
     * @return Lista zmian lub null jeśli brak uprawnień
     * @throws IllegalStateException gdy API nie jest dostępne
     */
    fun getBlockHistory(player: Player, location: Location, time: Int = DEFAULT_LOOKUP_TIME): List<CoreProtectAPI.ParseResult>? {
        if (!player.hasPermission("plotsx.coreprotect.lookup")) {
            player.sendMessage(NO_PERMISSION_LOOKUP)
            return null
        }

        val api = coreProtect ?: throw IllegalStateException(ERROR_API_NOT_ENABLED)
        return api.blockLookup(location.block, time)
    }

    /**
     * Cofa zmiany w określonym promieniu
     * @param player Gracz wykonujący rollback
     * @param time Czas w sekundach
     * @param radius Promień działania
     * @return true jeśli operacja się powiodła
     * @throws IllegalStateException gdy API nie jest dostępne
     */
    fun rollback(player: Player, time: Int, radius: Int): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.rollback")) {
            player.sendMessage(NO_PERMISSION_ROLLBACK)
            return false
        }

        val api = coreProtect ?: throw IllegalStateException(ERROR_API_NOT_ENABLED)
        val success = api.performRollback(
            time,
            listOf(player.name),
            null,
            null,
            null,
            null,
            radius,
            player.location
        )
        
        if (success) {
            player.sendMessage(SUCCESS_ROLLBACK)
        }
        return success
    }

    /**
     * Przywraca zmiany w określonym promieniu
     * @param player Gracz wykonujący restore
     * @param time Czas w sekundach
     * @param radius Promień działania
     * @return true jeśli operacja się powiodła
     * @throws IllegalStateException gdy API nie jest dostępne
     */
    fun restore(player: Player, time: Int, radius: Int): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.restore")) {
            player.sendMessage(NO_PERMISSION_RESTORE)
            return false
        }

        val api = coreProtect ?: throw IllegalStateException(ERROR_API_NOT_ENABLED)
        val success = api.performRestore(
            time,
            listOf(player.name),
            null,
            null,
            null,
            null,
            radius,
            player.location
        )
        
        if (success) {
            player.sendMessage(SUCCESS_RESTORE)
        }
        return success
    }

    /**
     * Przełącza tryb inspektora dla gracza
     * @param player Gracz
     * @return true jeśli operacja się powiodła
     * @throws IllegalStateException gdy API nie jest dostępne
     */
    fun toggleInspector(player: Player): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.inspect")) {
            player.sendMessage(NO_PERMISSION_INSPECT)
            return false
        }

        val api = coreProtect ?: throw IllegalStateException(ERROR_API_NOT_ENABLED)
        val result = api.inspector?.toggleInspector(player) ?: return false
        
        api.parseResult(player, result)
        player.sendMessage(if (result) SUCCESS_INSPECT_ENABLED else SUCCESS_INSPECT_DISABLED)
        return true
    }

    /**
     * Sprawdza czy integracja jest włączona
     * @return true jeśli API jest dostępne
     */
    fun isEnabled(): Boolean = coreProtect != null

    /**
     * Zwraca instancję API CoreProtect
     * @return API lub null jeśli nie jest dostępne
     */
    fun getAPI(): CoreProtectAPI? = coreProtect
} 