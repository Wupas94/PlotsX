package com.plotsx.api.manager

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

interface ProtectionManager {
    fun canPlayerAccessPlot(player: Player, location: Location): Boolean
    fun canPlayerBuild(player: Player, location: Location): Boolean
    fun canPlayerInteract(player: Player, location: Location): Boolean
    fun canPlayerAttack(player: Player, location: Location): Boolean
    fun canMobsAttack(location: Location): Boolean
} 