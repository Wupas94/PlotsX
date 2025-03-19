package com.plotsx.systems.levels

data class PlotLevel(
    val level: Int,
    val name: String,
    val size: Int,
    val maxMembers: Int,
    val cost: Double,
    val requiredItems: Map<String, Int>,
    val availableFlags: List<String>
) 