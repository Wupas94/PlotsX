package com.plotsx.systems.tasks

import com.plotsx.PlotsX
import com.plotsx.models.Plot
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

class TaskManager(private val plugin: PlotsX) {
    private val tasks = mutableMapOf<String, Task>()
    private val playerProgress = mutableMapOf<UUID, MutableMap<String, TaskProgress>>()
    private val plotAchievements = mutableMapOf<UUID, MutableSet<Achievement>>()
    private val plotPoints = mutableMapOf<UUID, Int>()

    init {
        registerDefaultTasks()
    }

    private fun registerDefaultTasks() {
        registerTask(Task(
            "builder_novice",
            "Budowniczy Początkujący",
            "Zbuduj 100 bloków na swojej działce",
            100,
            { player, plot -> giveReward(player, 100.0) }
        ))

        registerTask(Task(
            "builder_expert",
            "Budowniczy Expert",
            "Zbuduj 1000 bloków na swojej działce",
            1000,
            { player, plot -> 
                giveReward(player, 1000.0)
                awardAchievement(plot, Achievement.MASTER_BUILDER)
            }
        ))

        registerTask(Task(
            "gardener",
            "Ogrodnik",
            "Posadź 50 kwiatów na swojej działce",
            50,
            { player, plot -> 
                giveReward(player, 500.0)
                awardAchievement(plot, Achievement.GREEN_THUMB)
            }
        ))

        // Add more default tasks here
    }

    fun registerTask(task: Task) {
        tasks[task.id] = task
    }

    fun progressTask(player: Player, plot: Plot, taskId: String, amount: Int = 1) {
        val task = tasks[taskId] ?: return
        val progress = getTaskProgress(player.uniqueId, taskId)
        
        progress.currentAmount += amount
        if (progress.currentAmount >= task.requiredAmount && !progress.completed) {
            progress.completed = true
            task.reward.invoke(player, plot)
            player.sendMessage("§aUkończyłeś zadanie: ${task.name}!")
            addPlotPoints(plot, 100)
        }
        
        saveProgress(player.uniqueId, taskId, progress)
    }

    fun getTaskProgress(playerId: UUID, taskId: String): TaskProgress {
        return playerProgress
            .getOrPut(playerId) { mutableMapOf() }
            .getOrPut(taskId) { TaskProgress() }
    }

    fun awardAchievement(plot: Plot, achievement: Achievement) {
        plotAchievements.getOrPut(plot.id) { mutableSetOf() }.add(achievement)
        
        // Notify online plot owner
        plugin.server.getPlayer(plot.owner)?.let { player ->
            player.sendMessage("§6Twoja działka zdobyła osiągnięcie: ${achievement.displayName}")
        }

        addPlotPoints(plot, achievement.points)
    }

    fun hasAchievement(plot: Plot, achievement: Achievement): Boolean {
        return plotAchievements[plot.id]?.contains(achievement) ?: false
    }

    fun getPlotAchievements(plot: Plot): Set<Achievement> {
        return plotAchievements[plot.id] ?: emptySet()
    }

    fun addPlotPoints(plot: Plot, points: Int) {
        plotPoints[plot.id] = (plotPoints[plot.id] ?: 0) + points
    }

    fun getPlotPoints(plot: Plot): Int {
        return plotPoints[plot.id] ?: 0
    }

    private fun giveReward(player: Player, money: Double) {
        plugin.economy?.depositPlayer(player, money)
        player.sendMessage("§aOtrzymałeś nagrodę: $money monet!")
    }

    private fun saveProgress(playerId: UUID, taskId: String, progress: TaskProgress) {
        playerProgress.getOrPut(playerId) { mutableMapOf() }[taskId] = progress
    }

    // Save/load methods for persistence
    fun saveData() {
        // TODO: Implement saving tasks data to config
    }

    fun loadData() {
        // TODO: Implement loading tasks data from config
    }
}

data class Task(
    val id: String,
    val name: String,
    val description: String,
    val requiredAmount: Int,
    val reward: (Player, Plot) -> Unit
)

data class TaskProgress(
    var currentAmount: Int = 0,
    var completed: Boolean = false
)

enum class Achievement(val displayName: String, val description: String, val points: Int) {
    MASTER_BUILDER("Mistrz Budownictwa", "Wybuduj imponującą konstrukcję", 500),
    GREEN_THUMB("Zielony Kciuk", "Stwórz piękny ogród", 300),
    REDSTONE_GENIUS("Geniusz Czerwonego Kamienia", "Stwórz skomplikowany mechanizm", 400),
    DECORATOR("Dekorator", "Udekoruj działkę w wyjątkowy sposób", 250),
    COMMUNITY_PILLAR("Filar Społeczności", "Otrzymaj 10 pozytywnych ocen", 600),
    ARCHITECT("Architekt", "Stwórz unikalny budynek", 450),
    INNOVATOR("Innowator", "Wykorzystaj zaawansowane mechaniki", 350),
    PERFECTIONIST("Perfekcjonista", "Osiągnij wszystkie poprzednie osiągnięcia", 1000)
} 