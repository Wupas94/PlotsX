package com.plotsx.managers

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.max

class SchematicManager(private val plugin: PlotsX) {
    private val schematicsDir = File(plugin.dataFolder, "schematics")

    init {
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs()
        }
    }

    fun savePlotSchematic(plot: Plot, name: String): Boolean {
        val file = File(schematicsDir, "$name.schem")
        if (file.exists()) {
            return false
        }

        val blocks = mutableMapOf<Location, BlockData>()
        val center = plot.center
        val world = center.world ?: return false

        for (x in plot.x1..plot.x2) {
            for (y in plot.y1..plot.y2) {
                for (z in plot.z1..plot.z2) {
                    val loc = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                    val block = loc.block
                    if (block.type != Material.AIR) {
                        blocks[loc] = block.blockData
                    }
                }
            }
        }

        return try {
            FileOutputStream(file).use { fos ->
                GZIPOutputStream(fos).use { gzos ->
                    gzos.write(blocks.size)
                    blocks.forEach { (loc, data) ->
                        gzos.write(loc.blockX - plot.x1)
                        gzos.write(loc.blockY - plot.y1)
                        gzos.write(loc.blockZ - plot.z1)
                        gzos.write(data.asString.toByteArray())
                    }
                }
            }
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save schematic: ${e.message}")
            false
        }
    }

    fun loadPlotSchematic(plot: Plot, name: String): Boolean {
        val file = File(schematicsDir, "$name.schem")
        if (!file.exists()) {
            return false
        }

        val world = plot.world
        val blocks = mutableMapOf<Location, BlockData>()

        try {
            FileInputStream(file).use { fis ->
                GZIPInputStream(fis).use { gzis ->
                    val size = gzis.read()
                    repeat(size) {
                        val x = gzis.read() + plot.x1
                        val y = gzis.read() + plot.y1
                        val z = gzis.read() + plot.z1
                        val data = world.createBlockData(gzis.readBytes().toString(Charsets.UTF_8))
                        blocks[Location(world, x.toDouble(), y.toDouble(), z.toDouble())] = data
                    }
                }
            }

            blocks.forEach { (loc, data) ->
                if (loc.x >= plot.x1 && loc.x <= plot.x2 &&
                    loc.y >= plot.y1 && loc.y <= plot.y2 &&
                    loc.z >= plot.z1 && loc.z <= plot.z2) {
                    loc.block.blockData = data
                }
            }
            return true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load schematic: ${e.message}")
            return false
        }
    }

    fun clearPlot(plot: Plot) {
        val world = plot.world

        for (x in plot.x1..plot.x2) {
            for (y in plot.y1..plot.y2) {
                for (z in plot.z1..plot.z2) {
                    val loc = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                    loc.block.type = Material.AIR
                }
            }
        }
    }

    fun getSchematicsList(): List<String> {
        return schematicsDir.listFiles()
            ?.filter { it.extension == "schem" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }

    fun deleteSchematic(name: String): Boolean {
        val file = File(schematicsDir, "$name.schem")
        return file.exists() && file.delete()
    }
} 