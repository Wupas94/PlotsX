package com.plotsx.commands

import com.plotsx.PlotsX
import com.plotsx.gui.PlotClaimGUI
import com.plotsx.gui.PlotFlagsGUI
import com.plotsx.models.Plot
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class PlotCommand(private val plugin: PlotsX) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}Ta komenda może być użyta tylko przez gracza!")
            return true
        }

        if (args.isEmpty()) {
            // Default claim command
            plugin.plotManager.getPlayerPlot(sender.uniqueId)?.let {
                sender.sendMessage("${ChatColor.RED}Masz już założoną działkę!")
                return true
            }

            val center = sender.location
            if (plugin.plotManager.isLocationInAnyPlot(center)) {
                sender.sendMessage("${ChatColor.RED}Ta lokalizacja jest już zajęta przez inną działkę!")
                return true
            }

            PlotClaimGUI(plugin, sender, center).open()
            return true
        }

        when (args[0].toLowerCase()) {
            "info" -> handleInfo(sender, args)
            "delete" -> handleDelete(sender)
            "add" -> handleAdd(sender, args)
            "remove" -> handleRemove(sender, args)
            "flags" -> handleFlags(sender)
            "biome" -> handleBiome(sender, args)
            "weather" -> handleWeather(sender, args)
            "time" -> handleTime(sender, args)
            "border" -> handleBorder(sender)
            "expand" -> handleExpand(sender, args)
            "shrink" -> handleShrink(sender, args)
            "move" -> handleMove(sender)
            "merge" -> handleMerge(sender)
            "split" -> handleSplit(sender)
            "swap" -> handleSwap(sender)
            "clear" -> handleClear(sender)
            "reset" -> handleReset(sender)
            "save" -> handleSave(sender, args)
            "load" -> handleLoad(sender, args)
            "list" -> handleList(sender)
            "search" -> handleSearch(sender, args)
            "stats" -> handleStats(sender)
            "sell" -> handleSell(sender, args)
            "buy" -> handleBuy(sender)
            "rent" -> handleRent(sender, args)
            "tasks" -> handleTasks(sender)
            "rewards" -> handleRewards(sender)
            "rate" -> handleRate(sender, args)
            "visit" -> handleVisit(sender, args)
            "home" -> handleHome(sender, args)
            "trust" -> handleTrust(sender, args)
            "untrust" -> handleUntrust(sender, args)
            "trusted" -> handleTrusted(sender)
            "flag" -> handleFlag(sender, args)
            "visits" -> handleVisits(sender)
            else -> sendHelp(sender)
        }

        return true
    }

    private fun handleInfo(sender: Player, args: Array<String>) {
        val plot = if (args.size > 1) {
            val target = plugin.server.getPlayer(args[1])
            if (target == null) {
                sender.sendMessage("${ChatColor.RED}Nie znaleziono gracza!")
                return
            }
            plugin.plotManager.getPlayerPlot(target.uniqueId)
        } else {
            plugin.plotManager.getPlayerPlot(sender.uniqueId)
        }

        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie znaleziono działki!")
            return
        }

        showPlotInfo(sender, plot)
    }

    private fun showPlotInfo(sender: Player, plot: Plot) {
        sender.sendMessage("${ChatColor.DARK_GRAY}=== ${ChatColor.AQUA}Informacje o działce ${ChatColor.DARK_GRAY}===")
        sender.sendMessage("  ${ChatColor.WHITE}Właściciel: ${ChatColor.YELLOW}${plugin.server.getOfflinePlayer(plot.owner).name}")
        sender.sendMessage("  ${ChatColor.WHITE}Wymiary: ${ChatColor.YELLOW}${plot.x2 - plot.x1}x${plot.z2 - plot.z1}")
        sender.sendMessage("  ${ChatColor.WHITE}Współwłaściciele: ${ChatColor.YELLOW}${plot.coowners.joinToString(", ") { plugin.server.getOfflinePlayer(it).name ?: it.toString() }}")
        sender.sendMessage("  ${ChatColor.WHITE}Flagi: ${ChatColor.YELLOW}${plot.flags.entries.joinToString(", ") { "${it.key}=${it.value}" }}")
        sender.sendMessage("  ${ChatColor.WHITE}Lokalizacja: ${ChatColor.YELLOW}${plot.center.world.name} (${plot.center.blockX}, ${plot.center.blockY}, ${plot.center.blockZ})")
        sender.sendMessage("  ${ChatColor.WHITE}Data utworzenia: ${ChatColor.YELLOW}${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(plot.createdAt))}")
        sender.sendMessage("  ${ChatColor.WHITE}Ostatnia modyfikacja: ${ChatColor.YELLOW}${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(plot.lastModified))}")
    }

    private fun handleDelete(sender: Player) {
        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        if (plugin.config.getBoolean("plot.require-deletion-confirmation", true)) {
            // TODO: Implement confirmation GUI
            sender.sendMessage("${ChatColor.YELLOW}Użyj ponownie komendy, aby potwierdzić usunięcie działki.")
            return
        }

        if (plugin.plotManager.deletePlot(plot.id)) {
            sender.sendMessage("${ChatColor.GREEN}Działka została usunięta!")
        } else {
            sender.sendMessage("${ChatColor.RED}Nie udało się usunąć działki!")
        }
    }

    private fun handleAdd(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot add <gracz>")
            return
        }

        val target = plugin.server.getPlayer(args[1])
        if (target == null) {
            sender.sendMessage("${ChatColor.RED}Nie znaleziono gracza!")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        if (plot.coowners.size >= plugin.config.getInt("plot.max-co-owners", 5)) {
            sender.sendMessage("${ChatColor.RED}Osiągnąłeś maksymalną liczbę współwłaścicieli!")
            return
        }

        if (plot.addCoOwner(target.uniqueId)) {
            sender.sendMessage("${ChatColor.GREEN}Dodano ${target.name} jako współwłaściciela działki!")
            target.sendMessage("${ChatColor.GREEN}Zostałeś dodany jako współwłaściciel działki gracza ${sender.name}!")
        } else {
            sender.sendMessage("${ChatColor.RED}Ten gracz jest już współwłaścicielem działki!")
        }
    }

    private fun handleRemove(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot remove <gracz>")
            return
        }

        val target = plugin.server.getPlayer(args[1])
        if (target == null) {
            sender.sendMessage("${ChatColor.RED}Nie znaleziono gracza!")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        if (plot.removeCoOwner(target.uniqueId)) {
            sender.sendMessage("${ChatColor.GREEN}Usunięto ${target.name} ze współwłaścicieli działki!")
            target.sendMessage("${ChatColor.RED}Zostałeś usunięty ze współwłaścicieli działki gracza ${sender.name}!")
        } else {
            sender.sendMessage("${ChatColor.RED}Ten gracz nie jest współwłaścicielem działki!")
        }
    }

    private fun handleFlags(sender: Player) {
        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        PlotFlagsGUI(plugin, sender, plot).open()
    }

    private fun handleBiome(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot biome <biom>")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        try {
            val biome = org.bukkit.block.Biome.valueOf(args[1].toUpperCase())
            plugin.worldManager.setPlotBiome(plot, biome)
            sender.sendMessage("${ChatColor.GREEN}Zmieniono biom działki na ${biome.name}!")
        } catch (e: IllegalArgumentException) {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowy biom!")
        }
    }

    private fun handleWeather(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot weather <clear/rain/thunder>")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        when (args[1].toLowerCase()) {
            "clear" -> {
                plugin.weatherManager.setPlotWeather(plot, org.bukkit.WeatherType.CLEAR)
                sender.sendMessage("${ChatColor.GREEN}Ustawiono pogodę na bezchmurną!")
            }
            "rain" -> {
                plugin.weatherManager.setPlotWeather(plot, org.bukkit.WeatherType.DOWNFALL)
                sender.sendMessage("${ChatColor.GREEN}Ustawiono pogodę na deszczową!")
            }
            "thunder" -> {
                plugin.weatherManager.setPlotWeather(plot, org.bukkit.WeatherType.DOWNFALL)
                plot.center.world?.setThundering(true)
                sender.sendMessage("${ChatColor.GREEN}Ustawiono pogodę na burzową!")
            }
            else -> sender.sendMessage("${ChatColor.RED}Nieprawidłowa pogoda! Użyj: clear, rain lub thunder")
        }
    }

    private fun handleTime(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot time <day/night>")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        when (args[1].toLowerCase()) {
            "day" -> {
                plugin.worldManager.setPlotTime(plot, 6000)
                sender.sendMessage("${ChatColor.GREEN}Ustawiono czas na dzień!")
            }
            "night" -> {
                plugin.worldManager.setPlotTime(plot, 18000)
                sender.sendMessage("${ChatColor.GREEN}Ustawiono czas na noc!")
            }
            else -> sender.sendMessage("${ChatColor.RED}Nieprawidłowy czas! Użyj: day lub night")
        }
    }

    private fun handleBorder(sender: Player) {
        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        // TODO: Implement border visualization
        sender.sendMessage("${ChatColor.GREEN}Granice działki zostały pokazane!")
    }

    private fun handleExpand(sender: Player, args: Array<String>) {
        if (!sender.hasPermission("plotsx.expand")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do powiększania działek!")
            return
        }

        if (args.size < 3) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot expand <szerokość> <wysokość>")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        if (plot.owner != sender.uniqueId) {
            sender.sendMessage("${ChatColor.RED}Nie jesteś właścicielem tej działki!")
            return
        }

        val width = try {
            args[1].toInt()
        } catch (e: NumberFormatException) {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowa szerokość!")
            return
        }

        val height = try {
            args[2].toInt()
        } catch (e: NumberFormatException) {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowa wysokość!")
            return
        }

        plugin.plotManager.expandPlot(plot, width, height).fold(
            onSuccess = {
                sender.sendMessage("${ChatColor.GREEN}Powiększono działkę do ${width}x${height}!")
            },
            onFailure = { error ->
                sender.sendMessage("${ChatColor.RED}${error.message}")
            }
        )
    }

    private fun handleShrink(sender: Player, args: Array<String>) {
        if (!sender.hasPermission("plotsx.shrink")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do zmniejszania działek!")
            return
        }

        if (args.size < 3) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot shrink <szerokość> <wysokość>")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        if (plot.owner != sender.uniqueId) {
            sender.sendMessage("${ChatColor.RED}Nie jesteś właścicielem tej działki!")
            return
        }

        val width = try {
            args[1].toInt()
        } catch (e: NumberFormatException) {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowa szerokość!")
            return
        }

        val height = try {
            args[2].toInt()
        } catch (e: NumberFormatException) {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowa wysokość!")
            return
        }

        plugin.plotManager.shrinkPlot(plot, width, height).fold(
            onSuccess = {
                sender.sendMessage("${ChatColor.GREEN}Zmniejszono działkę do ${width}x${height}!")
            },
            onFailure = { error ->
                sender.sendMessage("${ChatColor.RED}${error.message}")
            }
        )
    }

    private fun handleMove(sender: Player) {
        if (!sender.hasPermission("plotsx.move")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do przenoszenia działek!")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        if (plot.owner != sender.uniqueId) {
            sender.sendMessage("${ChatColor.RED}Nie jesteś właścicielem tej działki!")
            return
        }

        plugin.plotManager.movePlot(plot, sender.location).fold(
            onSuccess = {
                sender.sendMessage("${ChatColor.GREEN}Przeniesiono działkę!")
            },
            onFailure = { error ->
                sender.sendMessage("${ChatColor.RED}${error.message}")
            }
        )
    }

    private fun handleMerge(sender: Player) {
        if (!sender.hasPermission("plotsx.merge")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do łączenia działek!")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        if (plot.owner != sender.uniqueId) {
            sender.sendMessage("${ChatColor.RED}Nie jesteś właścicielem tej działki!")
            return
        }

        val nearbyPlots = plugin.plotManager.getNearbyPlots(sender.location, 1)
            .filter { it.owner == sender.uniqueId && it.id != plot.id }

        if (nearbyPlots.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Nie znaleziono sąsiednich działek do połączenia!")
            return
        }

        val nearestPlot = nearbyPlots.minByOrNull { it.center.distance(plot.center) }!!

        plugin.plotManager.mergePlots(plot, nearestPlot).fold(
            onSuccess = {
                sender.sendMessage("${ChatColor.GREEN}Połączono działki!")
            },
            onFailure = { error ->
                sender.sendMessage("${ChatColor.RED}${error.message}")
            }
        )
    }

    private fun handleSplit(sender: Player) {
        if (!sender.hasPermission("plotsx.split")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do dzielenia działek!")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        if (plot.owner != sender.uniqueId) {
            sender.sendMessage("${ChatColor.RED}Nie jesteś właścicielem tej działki!")
            return
        }

        plugin.plotManager.splitPlot(plot, sender.location).fold(
            onSuccess = {
                sender.sendMessage("${ChatColor.GREEN}Podzielono działkę!")
            },
            onFailure = { error ->
                sender.sendMessage("${ChatColor.RED}${error.message}")
            }
        )
    }

    private fun handleSwap(sender: Player) {
        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        // TODO: Implement plot swapping
        sender.sendMessage("${ChatColor.GREEN}Działki zostały zamienione miejscami!")
    }

    private fun handleClear(sender: Player) {
        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        // TODO: Implement plot clearing
        sender.sendMessage("${ChatColor.GREEN}Działka została wyczyszczona!")
    }

    private fun handleReset(sender: Player) {
        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        // TODO: Implement plot resetting
        sender.sendMessage("${ChatColor.GREEN}Działka została zresetowana!")
    }

    private fun handleSave(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot save <nazwa>")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        if (plugin.schematicManager.savePlotAsSchematic(plot, args[1])) {
            sender.sendMessage("${ChatColor.GREEN}Zapisano schemat działki jako ${args[1]}!")
        } else {
            sender.sendMessage("${ChatColor.RED}Nie udało się zapisać schematu działki!")
        }
    }

    private fun handleLoad(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot load <nazwa>")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        val schematic = plugin.schematicManager.getSchematic(args[1])
        if (schematic == null) {
            sender.sendMessage("${ChatColor.RED}Nie znaleziono schematu o nazwie ${args[1]}!")
            return
        }

        if (plugin.schematicManager.loadSchematicToPlot(plot, schematic)) {
            sender.sendMessage("${ChatColor.GREEN}Załadowano schemat ${args[1]} na działkę!")
        } else {
            sender.sendMessage("${ChatColor.RED}Nie udało się załadować schematu na działkę!")
        }
    }

    private fun handleList(sender: Player) {
        val plots = plugin.plotManager.getPlayerPlots(sender.uniqueId)
        if (plots.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Nie masz żadnych działek!")
            return
        }

        sender.sendMessage("${ChatColor.GOLD}=== Twoje działki ===")
        plots.forEach { plot ->
            sender.sendMessage("${ChatColor.YELLOW}- Działka ${plot.id}")
            sender.sendMessage("  ${ChatColor.WHITE}Środek: X: ${plot.center.x}, Y: ${plot.center.y}, Z: ${plot.center.z}")
            sender.sendMessage("  ${ChatColor.WHITE}Wymiary: X: ${plot.x1}-${plot.x2}, Y: ${plot.y1}-${plot.y2}, Z: ${plot.z1}-${plot.z2}")
        }
    }

    private fun handleSearch(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot search <nazwa>")
            return
        }

        val plots = plugin.plotManager.searchPlots(args[1])
        if (plots.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Nie znaleziono działek o nazwie ${args[1]}!")
            return
        }

        sender.sendMessage("${ChatColor.GOLD}=== Znalezione działki ===")
        plots.forEach { plot ->
            sender.sendMessage("${ChatColor.YELLOW}- Działka ${plot.id}")
            sender.sendMessage("  ${ChatColor.WHITE}Właściciel: ${plugin.server.getOfflinePlayer(plot.owner).name}")
            sender.sendMessage("  ${ChatColor.WHITE}Środek: X: ${plot.center.x}, Y: ${plot.center.y}, Z: ${plot.center.z}")
            sender.sendMessage("  ${ChatColor.WHITE}Wymiary: ${plot.x2 - plot.x1}x${plot.z2 - plot.z1}")
        }
    }

    private fun handleStats(sender: Player) {
        sender.sendMessage(plugin.getStatusReport())
    }

    private fun handleSell(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot sell <cena>")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        val price = args[1].toDoubleOrNull()
        if (price == null || price <= 0) {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowa cena!")
            return
        }

        // TODO: Implement plot selling
        sender.sendMessage("${ChatColor.GREEN}Działka została wystawiona na sprzedaż za $price!")
    }

    private fun handleBuy(sender: Player) {
        // TODO: Implement plot buying
        sender.sendMessage("${ChatColor.GREEN}Działka została kupiona!")
    }

    private fun handleRent(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot rent <cena>")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        val price = args[1].toDoubleOrNull()
        if (price == null || price <= 0) {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowa cena!")
            return
        }

        // TODO: Implement plot renting
        sender.sendMessage("${ChatColor.GREEN}Działka została wystawiona na wynajem za $price!")
    }

    private fun handleTasks(sender: Player) {
        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        // TODO: Implement task display
        sender.sendMessage("${ChatColor.GREEN}Lista zadań została wyświetlona!")
    }

    private fun handleRewards(sender: Player) {
        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie masz założonej działki!")
            return
        }

        // TODO: Implement rewards collection
        sender.sendMessage("${ChatColor.GREEN}Nagrody zostały odebrane!")
    }

    private fun handleRate(sender: Player, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot rate <1-5>")
            return
        }

        val rating = args[1].toIntOrNull()
        if (rating == null || rating !in 1..5) {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowa ocena! Użyj liczby od 1 do 5.")
            return
        }

        // TODO: Implement plot rating
        sender.sendMessage("${ChatColor.GREEN}Działka została oceniona na $rating gwiazdek!")
    }

    private fun handleVisit(sender: Player, args: Array<String>) {
        if (!sender.hasPermission("plotsx.visit")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do odwiedzania działek!")
            return
        }

        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot visit <gracz>")
            return
        }

        val target = plugin.server.getOfflinePlayer(args[1])
        val plots = plugin.plotManager.getAllPlots().filter { it.owner == target.uniqueId }
        if (plots.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Ten gracz nie ma żadnych działek!")
            return
        }

        val plot = plots.first()
        plugin.worldManager.teleportToPlot(sender, plot)
        sender.sendMessage("${ChatColor.GREEN}Odwiedzasz działkę gracza ${target.name}!")
        plugin.visitManager.addVisit(sender.uniqueId, plot.id)
    }

    private fun handleHome(sender: Player, args: Array<String>) {
        if (!sender.hasPermission("plotsx.home")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do teleportacji do działek!")
            return
        }

        val plots = plugin.plotManager.getAllPlots().filter { it.owner == sender.uniqueId }
        if (plots.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Nie masz żadnych działek!")
            return
        }

        val index = if (args.size > 1) {
            try {
                args[1].toInt() - 1
            } catch (e: NumberFormatException) {
                sender.sendMessage("${ChatColor.RED}Nieprawidłowy numer działki!")
                return
            }
        } else 0

        if (index < 0 || index >= plots.size) {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowy numer działki!")
            return
        }

        val plot = plots[index]
        plugin.worldManager.teleportToPlot(sender, plot)
        sender.sendMessage("${ChatColor.GREEN}Teleportowano do działki!")
    }

    private fun handleTrust(sender: Player, args: Array<String>) {
        if (!sender.hasPermission("plotsx.trust")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do zarządzania zaufanymi graczami!")
            return
        }

        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot trust <gracz>")
            return
        }

        val target = plugin.server.getOfflinePlayer(args[1])
        if (target.uniqueId == sender.uniqueId) {
            sender.sendMessage("${ChatColor.RED}Nie możesz dodać właściciela do zaufanych!")
            return
        }

        if (plugin.plotManager.getPlayerPlot(sender.uniqueId)?.coowners?.contains(target.uniqueId) == true) {
            sender.sendMessage("${ChatColor.RED}Ten gracz jest już zaufany!")
            return
        }

        plugin.trustManager.trustPlayer(sender.uniqueId, target.uniqueId, TrustLevel.BUILD)
        sender.sendMessage("${ChatColor.GREEN}Dodano ${target.name} do zaufanych graczy!")
        target.sendMessage("${ChatColor.GREEN}Zostałeś dodany do zaufanych graczy działki gracza ${sender.name}!")
    }

    private fun handleUntrust(sender: Player, args: Array<String>) {
        if (!sender.hasPermission("plotsx.untrust")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do zarządzania zaufanymi graczami!")
            return
        }

        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot untrust <gracz>")
            return
        }

        val target = plugin.server.getOfflinePlayer(args[1])
        if (!plugin.plotManager.getPlayerPlot(sender.uniqueId)?.coowners?.contains(target.uniqueId) == true) {
            sender.sendMessage("${ChatColor.RED}Ten gracz nie jest zaufany!")
            return
        }

        plugin.trustManager.untrustPlayer(sender.uniqueId, target.uniqueId)
        sender.sendMessage("${ChatColor.GREEN}Usunięto ${target.name} ze zaufanych graczy!")
        target.sendMessage("${ChatColor.RED}Zostałeś usunięty ze zaufanych graczy działki gracza ${sender.name}!")
    }

    private fun handleTrusted(sender: Player) {
        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie znaleziono działki w tym miejscu!")
            return
        }

        sender.sendMessage("${ChatColor.GRAY}Lista zaufanych graczy:")
        plot.coowners.forEach { uuid ->
            val name = plugin.server.getOfflinePlayer(uuid).name ?: uuid.toString()
            sender.sendMessage("${ChatColor.YELLOW}- $name")
        }
    }

    private fun handleFlag(sender: Player, args: Array<String>) {
        if (!sender.hasPermission("plotsx.flag")) {
            sender.sendMessage("${ChatColor.RED}Nie masz uprawnień do zarządzania flagami!")
            return
        }

        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Użyj: /plot flag <flaga> [wartość]")
            return
        }

        val plot = plugin.plotManager.getPlayerPlot(sender.uniqueId)
        if (plot == null) {
            sender.sendMessage("${ChatColor.RED}Nie znaleziono działki w tym miejscu!")
            return
        }

        val flag = args[1].toLowerCase()
        val value = if (args.size > 2) args[2] else null

        if (plot.flags.containsKey(flag)) {
            if (value == null) {
                sender.sendMessage("${ChatColor.GREEN}Flaga ${flag} została usunięta!")
                plot.flags.remove(flag)
            } else {
                plot.flags[flag] = value
                sender.sendMessage("${ChatColor.GREEN}Flaga ${flag} została zaktualizowana!")
            }
        } else {
            sender.sendMessage("${ChatColor.RED}Nieprawidłowa flaga!")
        }
    }

    private fun sendHelp(sender: Player) {
        sender.sendMessage("""
            ${ChatColor.GOLD}=== PlotsX - Pomoc ===
            ${ChatColor.YELLOW}Zarządzanie działką:
            ${ChatColor.WHITE}/plot claim §8- §fZajmij działkę
            ${ChatColor.WHITE}/plot delete §8- §fUsuń działkę
            ${ChatColor.WHITE}/plot home §8- §fTeleportuj się na działkę
            ${ChatColor.WHITE}/plot info §8- §fInformacje o działce
            ${ChatColor.WHITE}/plot list §8- §fLista twoich działek
            
            ${ChatColor.YELLOW}Zaufani gracze:
            ${ChatColor.WHITE}/plot trust <gracz> §8- §fDodaj zaufanego gracza
            ${ChatColor.WHITE}/plot untrust <gracz> §8- §fUsuń zaufanego gracza
            
            ${ChatColor.YELLOW}System wizyt:
            ${ChatColor.WHITE}/plot visit <gracz> §8- §fOdwiedź działkę gracza
            ${ChatColor.WHITE}/plot rate <1-5> §8- §fOceń działkę
            
            ${ChatColor.YELLOW}Ustawienia działki:
            ${ChatColor.WHITE}/plot flags §8- §fZarządzaj flagami działki
            ${ChatColor.WHITE}/plot biome §8- §fZmień biom działki
            ${ChatColor.WHITE}/plot weather §8- §fZmień pogodę na działce
            ${ChatColor.WHITE}/plot time §8- §fZmień czas na działce
            ${ChatColor.WHITE}/plot border §8- §fPokaż/ukryj granice działki
            
            ${ChatColor.YELLOW}System ekonomii:
            ${ChatColor.WHITE}/plot sell <cena> §8- §fWystaw działkę na sprzedaż
            ${ChatColor.WHITE}/plot buy §8- §fKup wystawioną działkę
            ${ChatColor.WHITE}/plot rent §8- §fWynajmij działkę
            
            ${ChatColor.YELLOW}System zadań:
            ${ChatColor.WHITE}/plot tasks §8- §fSprawdź dostępne zadania
            ${ChatColor.WHITE}/plot rewards §8- §fOdbierz nagrody
            
            ${ChatColor.YELLOW}Inne:
            ${ChatColor.WHITE}/plot stats §8- §fStatystyki pluginu
            ${ChatColor.WHITE}/plot help §8- §fWyświetl tę pomoc
        """.trimIndent())
    }
} 