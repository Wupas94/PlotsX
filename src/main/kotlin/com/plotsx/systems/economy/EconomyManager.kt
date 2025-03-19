package com.plotsx.systems.economy

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.entity.Player
import java.util.*

class EconomyManager(private val plugin: PlotsX) {
    private val plotPrices = mutableMapOf<UUID, Double>()
    private val plotRentals = mutableMapOf<UUID, RentalInfo>()
    private val plotAuctions = mutableMapOf<UUID, AuctionInfo>()
    private val plotShops = mutableMapOf<UUID, MutableList<ShopItem>>()

    fun setPlotPrice(plot: Plot, price: Double) {
        if (price <= 0) {
            plotPrices.remove(plot.id)
            return
        }
        plotPrices[plot.id] = price
    }

    fun getPlotPrice(plot: Plot): Double? {
        return plotPrices[plot.id]
    }

    fun buyPlot(player: Player, plot: Plot): Boolean {
        val price = plotPrices[plot.id] ?: return false
        val economy = plugin.economy ?: return false

        if (economy.getBalance(player) < price) {
            player.sendMessage("§cNie masz wystarczająco pieniędzy! (Potrzeba: $price)")
            return false
        }

        // Transfer money
        economy.withdrawPlayer(player, price)
        plugin.server.getPlayer(plot.owner)?.let { owner ->
            economy.depositPlayer(owner, price)
            owner.sendMessage("§aGracz ${player.name} kupił twoją działkę za $price!")
        }

        // Transfer ownership
        plot.owner = player.uniqueId
        plugin.plotManager.savePlot(plot)
        plotPrices.remove(plot.id)

        player.sendMessage("§aKupiłeś działkę za $price!")
        return true
    }

    fun setRental(plot: Plot, price: Double, duration: Long) {
        if (price <= 0) {
            plotRentals.remove(plot.id)
            return
        }
        plotRentals[plot.id] = RentalInfo(price, duration)
    }

    fun getRentalInfo(plot: Plot): RentalInfo? {
        return plotRentals[plot.id]
    }

    fun rentPlot(player: Player, plot: Plot): Boolean {
        val rentalInfo = plotRentals[plot.id] ?: return false
        val economy = plugin.economy ?: return false

        if (economy.getBalance(player) < rentalInfo.price) {
            player.sendMessage("§cNie masz wystarczająco pieniędzy! (Potrzeba: ${rentalInfo.price})")
            return false
        }

        // Transfer money
        economy.withdrawPlayer(player, rentalInfo.price)
        plugin.server.getPlayer(plot.owner)?.let { owner ->
            economy.depositPlayer(owner, rentalInfo.price)
            owner.sendMessage("§aGracz ${player.name} wynajął twoją działkę za ${rentalInfo.price}!")
        }

        // Set rental
        plot.rentedTo = player.uniqueId
        plot.rentalExpiry = System.currentTimeMillis() + rentalInfo.duration
        plugin.plotManager.savePlot(plot)

        player.sendMessage("§aWynająłeś działkę za ${rentalInfo.price}!")
        return true
    }

    fun setAuction(plot: Plot, startingPrice: Double, duration: Long) {
        if (startingPrice <= 0) {
            plotAuctions.remove(plot.id)
            return
        }
        plotAuctions[plot.id] = AuctionInfo(startingPrice, duration, System.currentTimeMillis())
    }

    fun getAuctionInfo(plot: Plot): AuctionInfo? {
        return plotAuctions[plot.id]
    }

    fun bidOnPlot(player: Player, plot: Plot, amount: Double): Boolean {
        val auctionInfo = plotAuctions[plot.id] ?: return false
        val economy = plugin.economy ?: return false

        if (amount <= auctionInfo.currentPrice) {
            player.sendMessage("§cOferta musi być wyższa niż obecna cena! (Obecna: ${auctionInfo.currentPrice})")
            return false
        }

        if (economy.getBalance(player) < amount) {
            player.sendMessage("§cNie masz wystarczająco pieniędzy! (Potrzeba: $amount)")
            return false
        }

        // Transfer money
        economy.withdrawPlayer(player, amount)
        auctionInfo.currentBidder?.let { oldBidder ->
            plugin.server.getPlayer(oldBidder)?.let { oldPlayer ->
                economy.depositPlayer(oldPlayer, auctionInfo.currentPrice)
                oldPlayer.sendMessage("§cTwoja oferta na działkę została przebita!")
            }
        }

        // Update auction
        auctionInfo.currentPrice = amount
        auctionInfo.currentBidder = player.uniqueId
        auctionInfo.lastBidTime = System.currentTimeMillis()

        player.sendMessage("§aZłożyłeś ofertę $amount na działkę!")
        return true
    }

    fun addShopItem(plot: Plot, item: ShopItem) {
        plotShops.getOrPut(plot.id) { mutableListOf() }.add(item)
    }

    fun removeShopItem(plot: Plot, item: ShopItem) {
        plotShops[plot.id]?.remove(item)
    }

    fun getShopItems(plot: Plot): List<ShopItem> {
        return plotShops[plot.id] ?: emptyList()
    }

    fun buyShopItem(player: Player, plot: Plot, item: ShopItem): Boolean {
        val economy = plugin.economy ?: return false

        if (economy.getBalance(player) < item.price) {
            player.sendMessage("§cNie masz wystarczająco pieniędzy! (Potrzeba: ${item.price})")
            return false
        }

        // Transfer money
        economy.withdrawPlayer(player, item.price)
        plugin.server.getPlayer(plot.owner)?.let { owner ->
            economy.depositPlayer(owner, item.price)
            owner.sendMessage("§aGracz ${player.name} kupił ${item.name} za ${item.price}!")
        }

        // Give item
        player.inventory.addItem(item.itemStack)

        player.sendMessage("§aKupiłeś ${item.name} za ${item.price}!")
        return true
    }

    fun cleanupExpiredRentals() {
        val currentTime = System.currentTimeMillis()
        plotRentals.entries.removeIf { (plotId, rental) ->
            if (rental.expirationTime != null && currentTime > rental.expirationTime!!) {
                plugin.plotManager.getPlot(plotId)?.let { plot ->
                    plugin.server.getPlayer(rental.currentTenant!!)?.let { tenant ->
                        tenant.sendMessage("§cTwój wynajem działki wygasł!")
                    }
                    plugin.server.getPlayer(plot.owner)?.let { owner ->
                        owner.sendMessage("§aWynajem twojej działki wygasł!")
                    }
                }
                true
            } else false
        }
    }

    fun cleanupExpiredAuctions() {
        val currentTime = System.currentTimeMillis()
        plotAuctions.entries.removeIf { (plotId, auction) ->
            if (currentTime > auction.endTime) {
                plugin.plotManager.getPlot(plotId)?.let { plot ->
                    auction.currentBidder?.let { winner ->
                        plugin.server.getPlayer(winner)?.let { player ->
                            player.sendMessage("§aWygrałeś aukcję działki za ${auction.currentBid}!")
                        }
                        plugin.server.getPlayer(plot.owner)?.let { owner ->
                            owner.sendMessage("§aTwoja działka została sprzedana na aukcji za ${auction.currentBid}!")
                            plugin.economy?.depositPlayer(owner, auction.currentBid)
                        }
                        plot.owner = winner
                        plugin.plotManager.savePlot(plot)
                    }
                }
                true
            } else false
        }
    }

    // Save/load methods for persistence
    fun saveData() {
        // TODO: Implement saving economy data to config
    }

    fun loadData() {
        // TODO: Implement loading economy data from config
    }
}

data class RentalInfo(
    val price: Double,
    val duration: Long
)

data class AuctionInfo(
    var currentPrice: Double,
    val duration: Long,
    val startTime: Long,
    var currentBidder: UUID? = null,
    var lastBidTime: Long = startTime
)

data class ShopItem(
    val name: String,
    val price: Double,
    val itemStack: org.bukkit.inventory.ItemStack
) 