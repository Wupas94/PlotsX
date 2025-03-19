package com.plotsx.models

import org.bukkit.block.data.BlockData
import java.io.Serializable

data class Schematic(
    val blocks: Map<Position, BlockData>
) : Serializable

data class Position(
    val x: Int,
    val y: Int,
    val z: Int
) : Serializable 