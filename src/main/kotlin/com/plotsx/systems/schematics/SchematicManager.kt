package com.plotsx.systems.schematics

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class SchematicManager(private val plugin: PlotsX) {
    private val schematicsFolder = File(plugin.dataFolder, "schematics")
    private val backupsFolder = File(plugin.dataFolder, "backups")
    private val presetSchematics = mutableMapOf<String, Schematic>()

    init {
        schematicsFolder.mkdirs()
        backupsFolder.mkdirs()
        loadPresetSchematics()
    }

    fun savePlotAsSchematic(plot: Plot, name: String): Boolean {
        val corners = plugin.plotManager.getPlotCorners(plot)
        if (corners == null) return false

        val schematic = Schematic(
            name,
            getBlocksInRegion(corners.first, corners.second),
            corners.second.x - corners.first.x + 1,
            corners.second.y - corners.first.y + 1,
            corners.second.z - corners.first.z + 1
        )

        return saveSchematic(schematic, File(schematicsFolder, "$name.schem"))
    }

    fun loadSchematicToPlot(plot: Plot, schematic: Schematic, offset: Location = plot.center): Boolean {
        val corners = plugin.plotManager.getPlotCorners(plot)
        if (corners == null) return false

        // Backup current plot state
        backupPlot(plot)

        // Paste schematic
        schematic.blocks.forEach { (relativePos, blockData) ->
            val targetLoc = offset.clone().add(
                relativePos.x.toDouble(),
                relativePos.y.toDouble(),
                relativePos.z.toDouble()
            )
            if (plugin.plotManager.isInPlot(targetLoc, plot)) {
                targetLoc.block.blockData = blockData
            }
        }

        return true
    }

    fun backupPlot(plot: Plot) {
        val backupName = "backup_${plot.id}_${System.currentTimeMillis()}"
        savePlotAsSchematic(plot, backupName)
    }

    fun restorePlotBackup(plot: Plot, backupName: String): Boolean {
        val backupFile = File(backupsFolder, "$backupName.schem")
        if (!backupFile.exists()) return false

        val schematic = loadSchematic(backupFile) ?: return false
        return loadSchematicToPlot(plot, schematic, plot.center)
    }

    fun copyPlot(sourcePlot: Plot, targetPlot: Plot): Boolean {
        val schematicName = "temp_copy_${sourcePlot.id}"
        if (!savePlotAsSchematic(sourcePlot, schematicName)) return false

        val schematic = loadSchematic(File(schematicsFolder, "$schematicName.schem")) ?: return false
        return loadSchematicToPlot(targetPlot, schematic, targetPlot.center)
    }

    fun getPresetSchematic(name: String): Schematic? {
        return presetSchematics[name]
    }

    private fun loadPresetSchematics() {
        val presetsFolder = File(plugin.dataFolder, "presets")
        if (!presetsFolder.exists()) {
            presetsFolder.mkdirs()
            // TODO: Copy default preset schematics from plugin resources
        }

        presetsFolder.listFiles { file -> file.extension == "schem" }?.forEach { file ->
            loadSchematic(file)?.let { schematic ->
                presetSchematics[file.nameWithoutExtension] = schematic
            }
        }
    }

    private fun getBlocksInRegion(min: Location, max: Location): Map<BlockVector, org.bukkit.block.data.BlockData> {
        val blocks = mutableMapOf<BlockVector, org.bukkit.block.data.BlockData>()
        
        for (x in min.blockX..max.blockX) {
            for (y in min.blockY..max.blockY) {
                for (z in min.blockZ..max.blockZ) {
                    val block = min.world!!.getBlockAt(x, y, z)
                    if (!block.type.isAir) {
                        blocks[BlockVector(
                            x - min.blockX,
                            y - min.blockY,
                            z - min.blockZ
                        )] = block.blockData.clone()
                    }
                }
            }
        }
        
        return blocks
    }

    private fun saveSchematic(schematic: Schematic, file: File): Boolean {
        try {
            val config = YamlConfiguration()
            config.set("name", schematic.name)
            config.set("width", schematic.width)
            config.set("height", schematic.height)
            config.set("length", schematic.length)

            // Save blocks
            val blocksSection = config.createSection("blocks")
            schematic.blocks.forEach { (pos, blockData) ->
                blocksSection.set(
                    "${pos.x},${pos.y},${pos.z}",
                    blockData.asString
                )
            }

            config.save(file)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun loadSchematic(file: File): Schematic? {
        try {
            val config = YamlConfiguration.loadConfiguration(file)
            val name = config.getString("name") ?: return null
            val width = config.getInt("width")
            val height = config.getInt("height")
            val length = config.getInt("length")

            val blocks = mutableMapOf<BlockVector, org.bukkit.block.data.BlockData>()
            val blocksSection = config.getConfigurationSection("blocks") ?: return null

            for (key in blocksSection.getKeys(false)) {
                val (x, y, z) = key.split(",").map { it.toInt() }
                val blockDataString = blocksSection.getString(key) ?: continue
                val blockData = plugin.server.createBlockData(blockDataString)
                blocks[BlockVector(x, y, z)] = blockData
            }

            return Schematic(name, blocks, width, height, length)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

data class Schematic(
    val name: String,
    val blocks: Map<BlockVector, org.bukkit.block.data.BlockData>,
    val width: Int,
    val height: Int,
    val length: Int
)

data class BlockVector(
    val x: Int,
    val y: Int,
    val z: Int
) 