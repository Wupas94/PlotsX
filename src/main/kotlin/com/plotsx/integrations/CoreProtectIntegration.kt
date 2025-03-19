package com.plotsx.integrations

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import net.coreprotect.CoreProtect
import net.coreprotect.CoreProtectAPI
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.math.max

/**
 * Integracja z pluginem CoreProtect
 * Zapewnia funkcjonalność logowania i zarządzania historią zmian na działkach.
 * 
 * @property plugin Instancja głównej klasy pluginu
 * @author Wupas94
 * @since 1.0
 */
class CoreProtectIntegration(private val plugin: PlotsX) {
    private var api: CoreProtectAPI? = null

    init {
        setupCoreProtect()
    }

    private fun setupCoreProtect() {
        val coreProtect = plugin.server.pluginManager.getPlugin("CoreProtect") as? CoreProtect
        if (coreProtect != null) {
            api = coreProtect.api
            if (api?.isEnabled == true && api?.apiVersion() != null) {
                plugin.logger.info("Successfully hooked into CoreProtect ${api?.apiVersion()}")
            } else {
                plugin.logger.warning("Failed to hook into CoreProtect")
                api = null
            }
        } else {
            plugin.logger.warning("CoreProtect not found")
        }
    }

    /**
     * Loguje postawienie bloku
     * @param player Gracz, który postawił blok
     * @param block Postawiony blok
     * @return true jeśli operacja się powiodła
     */
    fun logBlockPlace(player: Player, block: Block): Boolean =
        api?.logPlacement(player.name, block.location, block.type, block.blockData) ?: false

    /**
     * Loguje zniszczenie bloku
     * @param player Gracz, który zniszczył blok
     * @param block Zniszczony blok
     * @return true jeśli operacja się powiodła
     */
    fun logBlockBreak(player: Player, block: Block): Boolean =
        api?.logRemoval(player.name, block.location, block.type, block.blockData) ?: false

    /**
     * Loguje interakcję z kontenerem
     * @param player Gracz, który wchodzi w interakcję
     * @param block Kontener
     * @return true jeśli operacja się powiodła
     */
    fun logContainerTransaction(player: Player, block: Block): Boolean =
        api?.logContainerTransaction(player.name, block.location) ?: false

    /**
     * Loguje wiadomość na czacie
     * @param player Gracz, który wysłał wiadomość
     * @param message Treść wiadomości
     * @return true jeśli operacja się powiodła
     */
    fun logChat(player: Player, message: String): Boolean =
        api?.logChat(player.name, message) ?: false

    /**
     * Loguje użycie komendy
     * @param player Gracz, który użył komendy
     * @param command Użyta komenda
     * @return true jeśli operacja się powiodła
     */
    fun logCommand(player: Player, command: String): Boolean =
        api?.logCommand(player.name, command) ?: false

    /**
     * Pobiera historię zmian bloku
     * @param player Gracz sprawdzający historię
     * @param location Lokalizacja bloku
     * @param time Czas w sekundach (domyślnie 24h)
     * @return Lista zmian lub null jeśli brak uprawnień
     * @throws IllegalStateException gdy API nie jest dostępne
     */
    fun getBlockHistory(player: Player, location: Location, time: Int = 86400): List<CoreProtectAPI.ParseResult>? {
        if (!player.hasPermission("plotsx.coreprotect.lookup")) {
            player.sendMessage("§cNie masz uprawnień do sprawdzania historii bloków!")
            return null
        }

        val api = this.api ?: throw IllegalStateException("§cAPI CoreProtect nie jest włączone!")
        return api.blockLookup(location.block, time)
    }

    /**
     * Cofa zmiany w określonym promieniu
     * @param player Gracz wykonujący rollback
     * @param time Czas w sekundach
     * @param distance Promień działania
     * @return true jeśli operacja się powiodła
     * @throws IllegalStateException gdy API nie jest dostępne
     */
    fun rollback(player: Player, time: Int, distance: Int): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.rollback")) {
            player.sendMessage("§cNie masz uprawnień do cofania zmian!")
            return false
        }

        val api = this.api ?: throw IllegalStateException("§cAPI CoreProtect nie jest włączone!")
        val success = api.performRollback(
            time,
            player.name,
            null,
            null,
            null,
            null,
            distance,
            player.location
        )
        
        if (success) {
            player.sendMessage("§aRozpoczęto cofanie zmian...")
        }
        return success
    }

    /**
     * Przywraca zmiany w określonym promieniu
     * @param player Gracz wykonujący restore
     * @param time Czas w sekundach
     * @param distance Promień działania
     * @return true jeśli operacja się powiodła
     * @throws IllegalStateException gdy API nie jest dostępne
     */
    fun restore(player: Player, time: Int, distance: Int): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.restore")) {
            player.sendMessage("§cNie masz uprawnień do przywracania zmian!")
            return false
        }

        val api = this.api ?: throw IllegalStateException("§cAPI CoreProtect nie jest włączone!")
        val success = api.performRestore(
            time,
            player.name,
            null,
            null,
            null,
            null,
            distance,
            player.location
        )
        
        if (success) {
            player.sendMessage("§aRozpoczęto przywracanie zmian...")
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
            player.sendMessage("§cNie masz uprawnień do używania inspektora!")
            return false
        }

        val api = this.api ?: throw IllegalStateException("§cAPI CoreProtect nie jest włączone!")
        val result = api.inspector?.toggleInspector(player) ?: return false
        
        api.parseResult(player, result)
        player.sendMessage(if (result) "§aWłączono tryb inspektora!" else "§cWyłączono tryb inspektora!")
        return true
    }

    /**
     * Sprawdza czy integracja jest włączona
     * @return true jeśli API jest dostępne
     */
    fun isEnabled(): Boolean = api?.isEnabled == true

    /**
     * Zwraca instancję API CoreProtect
     * @return API lub null jeśli nie jest dostępne
     */
    fun getAPI(): CoreProtectAPI? = api

    fun rollbackPlot(plot: Plot, time: Int, player: Player? = null): Boolean {
        val api = this.api ?: return false
        val width = plot.x2 - plot.x1
        val height = plot.z2 - plot.z1
        val maxDimension = max(width, height)

        return api.performRollback(
            time,
            player?.name,
            null,
            null,
            null,
            null,
            maxDimension,
            plot.center
        )
    }

    fun restorePlot(plot: Plot, time: Int, player: Player? = null): Boolean {
        val api = this.api ?: return false
        val width = plot.x2 - plot.x1
        val height = plot.z2 - plot.z1
        val maxDimension = max(width, height)

        return api.performRestore(
            time,
            player?.name,
            null,
            null,
            null,
            null,
            maxDimension,
            plot.center
        )
    }

    fun lookupBlock(block: Block, time: Int): List<Array<Any>>? {
        val api = this.api ?: return null
        return api.blockLookup(block, time)
    }

    fun lookupPlayer(player: String, time: Int, location: Location? = null): List<Array<Any>>? {
        val api = this.api ?: return null
        return api.performLookup(
            time,
            listOf(player),
            null,
            null,
            null,
            null,
            if (location != null) 0 else -1,
            location
        )
    }
} 