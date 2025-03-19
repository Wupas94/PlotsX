fun getPlotByLocation(location: Location): Plot? {
    return plots.find { it.isInPlot(location) }
}

fun getPlotByPlayer(player: Player): Plot? {
    return plots.find { it.owner == player.uniqueId }
}

fun getPlotByUUID(uuid: UUID): Plot? {
    return plots.find { it.owner == uuid }
}

fun getPlotByID(id: String): Plot? {
    return plots.find { it.id == id }
}

fun getPlotByID(id: Int): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Long): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Double): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Float): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Boolean): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Char): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Byte): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Short): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: IntArray): Plot? {
    return plots.find { it.id == id.contentToString() }
}

fun getPlotByID(id: LongArray): Plot? {
    return plots.find { it.id == id.contentToString() }
}

fun getPlotByID(id: DoubleArray): Plot? {
    return plots.find { it.id == id.contentToString() }
}

fun getPlotByID(id: FloatArray): Plot? {
    return plots.find { it.id == id.contentToString() }
}

fun getPlotByID(id: BooleanArray): Plot? {
    return plots.find { it.id == id.contentToString() }
}

fun getPlotByID(id: CharArray): Plot? {
    return plots.find { it.id == id.contentToString() }
}

fun getPlotByID(id: ByteArray): Plot? {
    return plots.find { it.id == id.contentToString() }
}

fun getPlotByID(id: ShortArray): Plot? {
    return plots.find { it.id == id.contentToString() }
}

fun getPlotByID(id: Array<*>): Plot? {
    return plots.find { it.id == id.contentToString() }
}

fun getPlotByID(id: List<*>): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Set<*>): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Map<*, *>): Plot? {
    return plots.find { it.id == id.toString() }
}

fun getPlotByID(id: Any): Plot? {
    return plots.find { it.id == id.toString() }
} 