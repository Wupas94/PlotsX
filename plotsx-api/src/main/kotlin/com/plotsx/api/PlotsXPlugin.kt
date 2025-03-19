package com.plotsx.api

import com.plotsx.api.manager.PlotManager
import com.plotsx.api.manager.ProtectionManager
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.Plugin

interface PlotsXPlugin : Plugin {
    val plotManager: PlotManager
    val protectionManager: ProtectionManager
    val adventure: BukkitAudiences
    val miniMessage: MiniMessage
    
    companion object {
        const val PLOT_SIZE = 50
        const val PLOT_HEIGHT = 256
        
        const val PREFIX = "<gradient:#00b4d8:#0077b6>[PlotsX]</gradient> "
        const val SUCCESS_COLOR = "<#2ecc71>"
        const val ERROR_COLOR = "<#e74c3c>"
        const val INFO_COLOR = "<#3498db>"
    }
} 