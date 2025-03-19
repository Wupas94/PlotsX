package com.plotsx.integrations

import com.plotsx.PlotsX
import net.coreprotect.CoreProtect
import net.coreprotect.CoreProtectAPI
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class CoreProtectIntegration(private val plugin: PlotsX) {
    private var coreProtect: CoreProtectAPI? = null

    fun initialize() {
        val coreProtectPlugin: Plugin? = plugin.server.pluginManager.getPlugin("CoreProtect")
        
        if (coreProtectPlugin == null || coreProtectPlugin !is CoreProtect) {
            plugin.logger.warning("CoreProtect not found! Logging features will be limited.")
            return
        }

        val api = coreProtectPlugin.api
        if (api == null || api.APIVersion() < 9) {
            plugin.logger.warning("Unsupported version of CoreProtect! Please update to the latest version.")
            return
        }

        coreProtect = api
        plugin.logger.info("Successfully hooked into CoreProtect!")
    }

    fun logBlockPlace(player: Player, block: Block) {
        coreProtect?.logPlacement(player.name, block.location, block.type, block.blockData)
    }

    fun logBlockBreak(player: Player, block: Block) {
        coreProtect?.logRemoval(player.name, block.location, block.type, block.blockData)
    }

    fun logContainerTransaction(player: Player, block: Block) {
        coreProtect?.logContainerTransaction(player.name, block.location)
    }

    fun logChat(player: Player, message: String) {
        coreProtect?.logChat(player.name, message)
    }

    fun logCommand(player: Player, command: String) {
        coreProtect?.logCommand(player.name, command)
    }

    fun getBlockHistory(player: Player, location: Location, time: Int = 86400): List<CoreProtectAPI.ParseResult>? {
        if (!player.hasPermission("plotsx.coreprotect.lookup")) {
            player.sendMessage("${PlotsX.PREFIX}${PlotsX.ERROR_COLOR}Nie masz uprawnień do sprawdzania historii bloków!")
            return null
        }
        return coreProtect?.blockLookup(location.block, time)
    }

    fun rollback(player: Player, time: Int, radius: Int): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.rollback")) {
            player.sendMessage("${PlotsX.PREFIX}${PlotsX.ERROR_COLOR}Nie masz uprawnień do cofania zmian!")
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
        
        player.sendMessage("${PlotsX.PREFIX}${PlotsX.SUCCESS_COLOR}Rozpoczęto cofanie zmian...")
        return true
    }

    fun restore(player: Player, time: Int, radius: Int): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.restore")) {
            player.sendMessage("${PlotsX.PREFIX}${PlotsX.ERROR_COLOR}Nie masz uprawnień do przywracania zmian!")
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
        
        player.sendMessage("${PlotsX.PREFIX}${PlotsX.SUCCESS_COLOR}Rozpoczęto przywracanie zmian...")
        return true
    }

    fun toggleInspector(player: Player): Boolean {
        if (!player.hasPermission("plotsx.coreprotect.inspect")) {
            player.sendMessage("${PlotsX.PREFIX}${PlotsX.ERROR_COLOR}Nie masz uprawnień do używania inspektora!")
            return false
        }

        coreProtect?.parseResult(player, coreProtect?.inspector?.toggleInspector(player) ?: return false)
        return true
    }

    fun isEnabled(): Boolean {
        return coreProtect != null
    }

    fun getAPI(): CoreProtectAPI? {
        return coreProtect
    }
} 