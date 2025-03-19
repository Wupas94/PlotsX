package com.plotsx.utils

import com.plotsx.PlotsX
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Duration

object Messages {
    private lateinit var plugin: PlotsX
    private lateinit var miniMessage: MiniMessage

    // Predefiniowane wiadomości
    const val PLOT_CREATED = "${PlotsX.SUCCESS_COLOR}Pomyślnie utworzono działkę!"
    const val PLOT_DELETED = "${PlotsX.SUCCESS_COLOR}Pomyślnie usunięto działkę!"
    const val PLOT_EXISTS = "${PlotsX.ERROR_COLOR}Już posiadasz działkę!"
    const val NO_PERMISSION = "${PlotsX.ERROR_COLOR}Nie masz uprawnień do tej komendy!"
    const val INVALID_LOCATION = "${PlotsX.ERROR_COLOR}Nie możesz tutaj założyć działki!"
    const val NOT_YOUR_PLOT = "${PlotsX.ERROR_COLOR}Ta działka nie należy do Ciebie!"
    const val PLAYER_NOT_FOUND = "${PlotsX.ERROR_COLOR}Nie znaleziono gracza!"
    const val COOWNER_ADDED = "${PlotsX.SUCCESS_COLOR}Pomyślnie dodano współwłaściciela!"
    const val COOWNER_REMOVED = "${PlotsX.SUCCESS_COLOR}Pomyślnie usunięto współwłaściciela!"
    const val FLAG_UPDATED = "${PlotsX.SUCCESS_COLOR}Pomyślnie zaktualizowano flagę!"

    fun init(plugin: PlotsX) {
        this.plugin = plugin
        this.miniMessage = plugin.miniMessage
    }

    fun CommandSender.sendMessage(message: String) {
        if (this is Player) {
            plugin.adventure.player(this).sendMessage(miniMessage.deserialize(PlotsX.PREFIX + message))
        } else {
            plugin.adventure.console().sendMessage(miniMessage.deserialize(PlotsX.PREFIX + message))
        }
    }

    fun Player.sendTitle(title: String, subtitle: String, fadeIn: Duration = Duration.ofSeconds(1), stay: Duration = Duration.ofSeconds(3), fadeOut: Duration = Duration.ofSeconds(1)) {
        val titleComponent = miniMessage.deserialize(title)
        val subtitleComponent = miniMessage.deserialize(subtitle)
        
        plugin.adventure.player(this).showTitle(
            Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(fadeIn, stay, fadeOut)
            )
        )
    }

    fun Player.sendActionBar(message: String) {
        plugin.adventure.player(this).sendActionBar(miniMessage.deserialize(message))
    }

    fun broadcast(message: String) {
        plugin.adventure.all().sendMessage(miniMessage.deserialize(PlotsX.PREFIX + message))
    }

    fun Audience.sendFormattedMessage(message: String) {
        this.sendMessage(miniMessage.deserialize(PlotsX.PREFIX + message))
    }
} 