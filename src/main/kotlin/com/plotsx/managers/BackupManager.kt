package com.plotsx.managers

import com.plotsx.PlotsX
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.BufferedInputStream

/**
 * Menedżer backupów danych pluginu
 * Zapewnia automatyczne tworzenie i zarządzanie kopiami zapasowymi
 * 
 * @property plugin Instancja głównej klasy pluginu
 * @author Wupas94
 * @since 1.0
 */
class BackupManager(private val plugin: PlotsX) {
    private val backupFolder = File(plugin.dataFolder, "backups")
    private val maxBackups = 5
    private val backupInterval = TimeUnit.HOURS.toMillis(24)
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")

    init {
        if (!backupFolder.exists()) {
            backupFolder.mkdirs()
        }
        startBackupTask()
    }

    /**
     * Uruchamia zadanie okresowego tworzenia backupów
     */
    private fun startBackupTask() {
        object : BukkitRunnable() {
            override fun run() {
                createBackup()
            }
        }.runTaskTimerAsynchronously(plugin, backupInterval / 50, backupInterval / 50)
    }

    /**
     * Tworzy nowy backup danych
     */
    fun createBackup() {
        val startTime = System.currentTimeMillis()
        val timestamp = LocalDateTime.now().format(dateFormat)
        val backupFile = File(backupFolder, "backup_$timestamp.zip")

        try {
            ZipOutputStream(FileOutputStream(backupFile)).use { zos ->
                // Backup działek
                val plotsFile = File(plugin.dataFolder, "plots.json")
                if (plotsFile.exists()) {
                    addToZip(zos, plotsFile, "plots.json")
                }

                // Backup schematów
                val schematicsFolder = File(plugin.dataFolder, "schematics")
                if (schematicsFolder.exists()) {
                    schematicsFolder.listFiles()?.forEach { file ->
                        addToZip(zos, file, "schematics/${file.name}")
                    }
                }

                // Backup ustawień
                val configFile = File(plugin.dataFolder, "config.yml")
                if (configFile.exists()) {
                    addToZip(zos, configFile, "config.yml")
                }
            }

            // Usuwanie starych backupów
            cleanupOldBackups()

            val duration = System.currentTimeMillis() - startTime
            plugin.metricsManager.recordTiming("backup_creation", duration)
            plugin.logger.info("Backup został utworzony w ${duration}ms")
        } catch (e: Exception) {
            plugin.metricsManager.recordError("backup_error")
            plugin.logger.severe("Błąd podczas tworzenia backupu: ${e.message}")
        }
    }

    /**
     * Dodaje plik do archiwum ZIP
     * @param zos Strumień ZIP
     * @param file Plik do dodania
     * @param entryName Nazwa wpisu w archiwum
     */
    private fun addToZip(zos: ZipOutputStream, file: File, entryName: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        BufferedInputStream(FileInputStream(file)).use { bis ->
            bis.copyTo(zos)
        }
        zos.closeEntry()
    }

    /**
     * Usuwa stare backupi
     */
    private fun cleanupOldBackups() {
        val backups = backupFolder.listFiles()?.filter { it.name.endsWith(".zip") }
            ?.sortedByDescending { it.lastModified() } ?: return

        if (backups.size > maxBackups) {
            backups.drop(maxBackups).forEach { it.delete() }
        }
    }

    /**
     * Przywraca backup z pliku
     * @param backupFile Plik backupu
     * @return true jeśli przywracanie się powiodło
     */
    fun restoreBackup(backupFile: File): Boolean {
        val startTime = System.currentTimeMillis()
        
        try {
            // Tymczasowy folder na rozpakowane pliki
            val tempFolder = File(plugin.dataFolder, "temp_restore")
            if (tempFolder.exists()) {
                tempFolder.deleteRecursively()
            }
            tempFolder.mkdirs()

            // Rozpakowanie backupu
            java.util.zip.ZipFile(backupFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val file = File(tempFolder, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }

            // Przywracanie plików
            val plotsFile = File(tempFolder, "plots.json")
            if (plotsFile.exists()) {
                plotsFile.copyTo(File(plugin.dataFolder, "plots.json"), overwrite = true)
            }

            val schematicsFolder = File(tempFolder, "schematics")
            if (schematicsFolder.exists()) {
                schematicsFolder.copyRecursively(File(plugin.dataFolder, "schematics"), overwrite = true)
            }

            val configFile = File(tempFolder, "config.yml")
            if (configFile.exists()) {
                configFile.copyTo(File(plugin.dataFolder, "config.yml"), overwrite = true)
            }

            // Czyszczenie
            tempFolder.deleteRecursively()

            val duration = System.currentTimeMillis() - startTime
            plugin.metricsManager.recordTiming("backup_restore", duration)
            plugin.logger.info("Backup został przywrócony w ${duration}ms")
            return true
        } catch (e: Exception) {
            plugin.metricsManager.recordError("backup_restore_error")
            plugin.logger.severe("Błąd podczas przywracania backupu: ${e.message}")
            return false
        }
    }

    /**
     * Pobiera listę dostępnych backupów
     * @return Lista plików backupów
     */
    fun getBackups(): List<File> {
        return backupFolder.listFiles()?.filter { it.name.endsWith(".zip") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
} 