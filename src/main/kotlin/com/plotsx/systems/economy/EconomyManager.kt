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

        // Set rental period
        rentalInfo.currentTenant = player.uniqueId
        rentalInfo.expirationTime = System.currentTimeMillis() + (rentalInfo.duration * 60 * 1000)

        player.sendMessage("§aWynająłeś działkę za ${rentalInfo.price}!")
        return true
    }

    fun startAuction(plot: Plot, startingBid: Double, duration: Long) {
        plotAuctions[plot.id] = AuctionInfo(
            startingBid,
            System.currentTimeMillis() + (duration * 60 * 1000)
        )
    }

    fun placeBid(player: Player, plot: Plot, amount: Double): Boolean {
        val auction = plotAuctions[plot.id] ?: return false
        val economy = plugin.economy ?: return false

        if (amount <= auction.currentBid) {
            player.sendMessage("§cOferta musi być wyższa niż obecna! (Minimum: ${auction.currentBid + 1})")
            return false
        }

        if (economy.getBalance(player) < amount) {
            player.sendMessage("§cNie masz wystarczająco pieniędzy!")
            return false
        }

        // Refund previous bidder
        auction.currentBidder?.let { previousBidder ->
            plugin.server.getPlayer(previousBidder)?.let { player ->
                economy.depositPlayer(player, auction.currentBid)
                player.sendMessage("§aTwoja oferta została przebita! Zwrócono ${auction.currentBid}")
            }
        }

        // Place new bid
        economy.withdrawPlayer(player, amount)
        auction.currentBid = amount
        auction.currentBidder = player.uniqueId

        player.sendMessage("§aZłożyłeś ofertę: $amount!")
        return true
    }

    fun addShopItem(plot: Plot, item: ShopItem) {
        plotShops.getOrPut(plot.id) { mutableListOf() }.add(item)
    }

    fun removeShopItem(plot: Plot, index: Int) {
        plotShops[plot.id]?.removeAt(index)
    }

    fun getShopItems(plot: Plot): List<ShopItem> {
        return plotShops[plot.id] ?: emptyList()
    }

    fun buyShopItem(player: Player, plot: Plot, index: Int): Boolean {
        val items = plotShops[plot.id] ?: return false
        val item = items.getOrNull(index) ?: return false
        val economy = plugin.economy ?: return false

        if (economy.getBalance(player) < item.price) {
            player.sendMessage("§cNie masz wystarczająco pieniędzy! (Potrzeba: ${item.price})")
            return false
        }

        // Transfer money
        economy.withdrawPlayer(player, item.price)
        plugin.server.getPlayer(plot.owner)?.let { owner ->
            economy.depositPlayer(owner, item.price)
            owner.sendMessage("§aGracz ${player.name} kupił ${item.itemStack.type} za ${item.price}!")
        }

        // Give item
        player.inventory.addItem(item.itemStack)
        player.sendMessage("§aKupiłeś ${item.itemStack.type} za ${item.price}!")
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
    val duration: Long, // in minutes
    var currentTenant: UUID? = null,
    var expirationTime: Long? = null
)

data class AuctionInfo(
    var currentBid: Double,
    val endTime: Long,
    var currentBidder: UUID? = null
)

data class ShopItem(
    val itemStack: org.bukkit.inventory.ItemStack,
    val price: Double
) 