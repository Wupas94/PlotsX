package com.plotsx.systems.biome

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import java.util.*

class BiomeManager(private val plugin: PlotsX) {
    private val plotBiomes = mutableMapOf<UUID, Biome>()
    private val plotGrowthRates = mutableMapOf<UUID, Double>()
    private val plotMobSpawnRules = mutableMapOf<UUID, MutableMap<EntityType, Boolean>>()
    private val plotSpreadableBlocks = mutableMapOf<UUID, MutableSet<Material>>()

    fun setBiome(plot: Plot, biome: Biome) {
        val corners = plugin.plotManager.getPlotCorners(plot) ?: return
        val world = corners.first.world ?: return

        // Change biome for all blocks in plot
        for (x in corners.first.blockX..corners.second.blockX) {
            for (z in corners.first.blockZ..corners.second.blockZ) {
                world.setBiome(x, z, biome)
            }
        }

        plotBiomes[plot.id] = biome
    }

    fun getPlotBiome(plot: Plot): Biome? {
        return plotBiomes[plot.id]
    }

    fun setGrowthRate(plot: Plot, rate: Double) {
        if (rate <= 0) {
            plotGrowthRates.remove(plot.id)
            return
        }
        plotGrowthRates[plot.id] = rate.coerceIn(0.0, 5.0) // Limit between 0x and 5x
    }

    fun getGrowthRate(plot: Plot): Double {
        return plotGrowthRates[plot.id] ?: 1.0
    }

    fun setMobSpawnRule(plot: Plot, entityType: EntityType, allowed: Boolean) {
        plotMobSpawnRules.getOrPut(plot.id) { mutableMapOf() }[entityType] = allowed
    }

    fun canMobSpawn(plot: Plot, entityType: EntityType): Boolean {
        return plotMobSpawnRules[plot.id]?.get(entityType) ?: true
    }

    fun setBlockSpreadable(plot: Plot, material: Material, spreadable: Boolean) {
        if (spreadable) {
            plotSpreadableBlocks.getOrPut(plot.id) { mutableSetOf() }.add(material)
        } else {
            plotSpreadableBlocks[plot.id]?.remove(material)
        }
    }

    fun canBlockSpread(plot: Plot, material: Material): Boolean {
        return plotSpreadableBlocks[plot.id]?.contains(material) ?: true
    }

    fun handleBlockGrowth(block: Block, plot: Plot) {
        val growthRate = getGrowthRate(plot)
        if (growthRate <= 0) return

        when (block.type) {
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.MELON_STEM,
            Material.PUMPKIN_STEM -> {
                val data = block.blockData as org.bukkit.block.data.Ageable
                if (data.age < data.maximumAge && Math.random() < (0.1 * growthRate)) {
                    data.age++
                    block.blockData = data
                }
            }
            else -> {}
        }
    }

    fun handleBlockSpread(block: Block, plot: Plot) {
        if (!canBlockSpread(plot, block.type)) {
            return
        }

        // Handle specific spread mechanics based on block type
        when (block.type) {
            Material.GRASS_BLOCK -> {
                // Spread grass to dirt
                block.location.add(0.0, 1.0, 0.0).block.let { above ->
                    if (above.type == Material.AIR && Math.random() < 0.1) {
                        block.location.subtract(0.0, 1.0, 0.0).block.let { below ->
                            if (below.type == Material.DIRT) {
                                below.type = Material.GRASS_BLOCK
                            }
                        }
                    }
                }
            }
            Material.VINE -> {
                // Spread vines downward
                block.location.subtract(0.0, 1.0, 0.0).block.let { below ->
                    if (below.type == Material.AIR && Math.random() < 0.1) {
                        below.type = Material.VINE
                        below.blockData = block.blockData.clone()
                    }
                }
            }
            else -> {}
        }
    }

    // Save/load methods for persistence
    fun saveData() {
        // TODO: Implement saving biome data to config
    }

    fun loadData() {
        // TODO: Implement loading biome data from config
    }
}

data class BiomeSettings(
    val biome: Biome,
    val growthRate: Double = 1.0,
    val mobSpawnRules: Map<EntityType, Boolean> = emptyMap(),
    val spreadableBlocks: Set<Material> = emptySet()
) 