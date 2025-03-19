package com.plotsx.systems.flags

enum class PlotFlag(
    val displayName: String,
    val description: String,
    val defaultValue: Boolean = false
) {
    PVP("PvP", "Pozwala na walkę PvP na działce"),
    MOB_DAMAGE("Obrażenia mobów", "Pozwala mobom zadawać obrażenia na działce"),
    WEATHER("Pogoda", "Kontroluje pogodę na działce", false),
    TIME("Czas", "Kontroluje porę dnia na działce", false),
    MOB_SPAWNING("Spawn mobów", "Kontroluje spawnienie się mobów", true),
    PARTICLE_EFFECTS("Efekty cząsteczkowe", "Pokazuje efekty cząsteczkowe na granicy działki", false),
    AUTO_FEED("Auto-karmienie", "Automatycznie karmi graczy na działce", false),
    WELCOME_MESSAGE("Wiadomość powitalna", "Pokazuje wiadomość powitalną", true),
    FAREWELL_MESSAGE("Wiadomość pożegnalna", "Pokazuje wiadomość pożegnalną", true);

    companion object {
        fun fromString(name: String): PlotFlag? {
            return values().find { it.name.equals(name, ignoreCase = true) }
        }
    }
} 