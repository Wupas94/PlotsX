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
 * @author Wupas94
 */
class CoreProtectIntegration(private val plugin: PlotsX) {
    private var coreProtect: CoreProtectAPI? = null

    companion object {
        private const val NO_PERMISSION_LOOKUP = "§cNie masz uprawnień do sprawdzania historii bloków!"
        private const val NO_PERMISSION_ROLLBACK = "§cNie masz uprawnień do cofania zmian!"
        private const val NO_PERMISSION_RESTORE = "§cNie masz uprawnień do przywracania zmian!"
        private const val NO_PERMISSION_INSPECT = "§cNie masz uprawnień do używania inspektora!"
        
        private const val SUCCESS_ROLLBACK = "§aRozpoczęto cofanie zmian..."
        private const val SUCCESS_RESTORE = "§aRozpoczęto przywracanie zmian..."
        
        private const val ERROR_NO_COREPROTECT = "CoreProtect not found! Logging features will be limited."
        private const val ERROR_INVALID_VERSION = "Unsupported version of CoreProtect! Please update to the latest version."
        private const val SUCCESS_INIT = "Successfully hooked into CoreProtect!"
        
        private const val DEFAULT_LOOKUP_TIME = 86400 // 24 hours in seconds
    }

    fun initialize() {
        val coreProtectPlugin: Plugin? = plugin.server.pluginManager.getPlugin("CoreProtect")
        
        if (coreProtectPlugin == null || coreProtectPlugin !is CoreProtect) {
            plugin.logger.warning(ERROR_NO_COREPROTECT)
            return
        }

        val api = coreProtectPlugin.api
        if (api == null || api.APIVersion() < 9) {
            plugin.logger.warning(ERROR_INVALID_VERSION)
            return
        }

        coreProtect = api
        plugin.logger.info(SUCCESS_INIT)
    }

    /**
     * Loguje postawienie bloku
     * @param player Gracz, który postawił blok
     * @param block Postawiony blok
     */
    fun logBlockPlace(player: Player, block: Block) {
        coreProtect?.logPlacement(player.name, block.location, block.type, block.blockData)
    }

    /**
     * Loguje zniszczenie bloku
     * @param player Gracz, który zniszczył blok
     * @param block Zniszczony blok
     */
    fun logBlockBreak(player: Player, block: Block) {
        coreProtect?.logRemoval(player.name, block.location, block.type, block.blockData)
    }

    /**
     * Loguje interakcję z kontenerem
     * @param player Gracz, który wchodzi w interakcję
     * @param block Kontener
     */
    fun logContainerTransaction(player: Player, block: Block) {
        coreProtect?.logContainerTransaction(player.name, block.location)
    }

    /**
     * Loguje wiadomość na czacie
     * @param player Gracz, który wysłał wiadomość
     * @param message Treść wiadomości
     */
    fun logChat(player: Player, message: String) {
        coreProtect?.logChat(player.name, message)
    }

    /**
     * Loguje użycie komendy
     * @param player Gracz, który użył komendy
     * @param command Użyta komenda
     */
    fun logCommand(player: Player, command: String) {
        coreProtect?.logCommand(player.name, command)
    }

    /**
     * Pobiera historię zmian bloku
     * @param player Gracz sprawdzający historię
     * @param location Lokalizacja bloku
     * @param time Czas w sekundach (domyślnie 24h)
     * @return Lista zmian lub null jeśli brak uprawnień
     */
    fun getBlockHistory(player: Player, location: Location, time: Int = DEFAULT_LOOKUP_TIME): List<CoreProtectAPI.ParseResult>? {
        if (!player.hasPermission("plotsx.coreprotect.lookup")) {
            player.sendMessage(NO_PERMISSION_LOOKUP)
            return null
        }
        return coreProtect?.blockLookup(location.block, time)
    }

    /**
     * Cofa zmiany w określonym promieniu
     * @param player Gracz wykonujący rollback
     * @param time Czas w sekundach
     * @param radius Promień działania
     * @return true jeśli operacja się powiodła
     */
    fun rollback(player: Player, time: Int, radius: Int): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.rollback")) {
            player.sendMessage(NO_PERMISSION_ROLLBACK)
            return false
        }

        coreProtect?.performRollback(
            time,
            listOf(player.name),
            null,
            null,
            null,
            null,
            radius,
            player.location
        )
        
        player.sendMessage(SUCCESS_ROLLBACK)
        return true
    }

    /**
     * Przywraca zmiany w określonym promieniu
     * @param player Gracz wykonujący restore
     * @param time Czas w sekundach
     * @param radius Promień działania
     * @return true jeśli operacja się powiodła
     */
    fun restore(player: Player, time: Int, radius: Int): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.restore")) {
            player.sendMessage(NO_PERMISSION_RESTORE)
            return false
        }

        coreProtect?.performRestore(
            time,
            listOf(player.name),
            null,
            null,
            null,
            null,
            radius,
            player.location
        )
        
        player.sendMessage(SUCCESS_RESTORE)
        return true
    }

    /**
     * Przełącza tryb inspektora dla gracza
     * @param player Gracz
     * @return true jeśli operacja się powiodła
     */
    fun toggleInspector(player: Player): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.inspect")) {
            player.sendMessage(NO_PERMISSION_INSPECT)
            return false
        }

        coreProtect?.parseResult(player, coreProtect?.inspector?.toggleInspector(player) ?: return false)
        return true
    }

    fun isEnabled(): Boolean = coreProtect != null

    fun getAPI(): CoreProtectAPI? = coreProtect
} 