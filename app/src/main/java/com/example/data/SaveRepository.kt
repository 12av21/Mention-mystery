package com.example.data

import android.content.Context
import com.example.model.GameSettings
import com.example.model.SaveData
import com.squareup.moshi.Moshi
import java.io.File
import java.io.FileOutputStream

class SaveRepository(private val filesDir: File) {

    constructor(context: Context) : this(context.filesDir)

    private val moshi: Moshi = Moshi.Builder().build()
    private val saveAdapter = moshi.adapter(SaveData::class.java)
    private val settingsAdapter = moshi.adapter(GameSettings::class.java)

    private val saveFile: File get() = File(filesDir, "mansion_save_v1.json")
    private val backupFile: File get() = File(filesDir, "mansion_save_v1.json.bak")
    private val tempFile: File get() = File(filesDir, "mansion_save_v1.json.tmp")
    private val settingsFile: File get() = File(filesDir, "mansion_settings_v1.json")

    fun hasValidSave(): Boolean = loadSave() != null

    fun saveGame(data: SaveData): Boolean {
        return runCatching {
            if (!filesDir.exists()) filesDir.mkdirs()
            val stamped = data.copy(
                version = SaveData.CURRENT_VERSION,
                updatedAtEpochMs = System.currentTimeMillis(),
            )
            val json = saveAdapter.toJson(stamped)
            FileOutputStream(tempFile).use { fos ->
                fos.write(json.toByteArray(Charsets.UTF_8))
                fos.fd.sync()
            }
            if (saveFile.exists() && isJsonValid(saveFile)) {
                saveFile.copyTo(backupFile, overwrite = true)
            }
            if (saveFile.exists()) {
                saveFile.delete()
            }
            tempFile.renameTo(saveFile)
        }.getOrDefault(false)
    }

    fun loadSave(): SaveData? {
        parseSaveFile(saveFile)?.let { return it }
        parseSaveFile(backupFile)?.let { recovered ->
            // Restore primary from valid backup
            runCatching { saveFile.writeText(saveAdapter.toJson(recovered), Charsets.UTF_8) }
            return recovered
        }
        return null
    }

    fun deleteSave() {
        runCatching {
            if (saveFile.exists()) saveFile.delete()
            if (backupFile.exists()) backupFile.delete()
            if (tempFile.exists()) tempFile.delete()
        }
    }

    fun saveSettings(settings: GameSettings) {
        runCatching {
            if (!filesDir.exists()) filesDir.mkdirs()
            settingsFile.writeText(settingsAdapter.toJson(settings), Charsets.UTF_8)
        }
    }

    fun loadSettings(): GameSettings {
        return runCatching {
            if (!settingsFile.exists()) return GameSettings()
            settingsAdapter.fromJson(settingsFile.readText(Charsets.UTF_8)) ?: GameSettings()
        }.getOrDefault(GameSettings())
    }

    private fun isJsonValid(file: File): Boolean = parseSaveFile(file) != null

    private fun parseSaveFile(file: File): SaveData? {
        if (!file.exists()) return null
        return runCatching {
            val parsed = saveAdapter.fromJson(file.readText(Charsets.UTF_8)) ?: return null
            if (parsed.version > SaveData.CURRENT_VERSION || parsed.version < 1) return null
            parsed
        }.getOrNull()
    }
}
