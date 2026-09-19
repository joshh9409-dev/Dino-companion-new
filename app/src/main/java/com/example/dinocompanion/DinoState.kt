package com.example.dinocompanion

import android.content.Context
import android.content.SharedPreferences

class DinoState(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("DinoPrefs", Context.MODE_PRIVATE)

    var dinoName: String
        get() = prefs.getString("dino_name", "Rex") ?: "Rex"
        set(value) = prefs.edit().putString("dino_name", value.trim().ifBlank { "Rex" }).apply()

    var dinoType: String
        get() = prefs.getString("dino_type", "T-Rex") ?: "T-Rex"
        set(value) = prefs.edit().putString("dino_type", value).apply()

    var stage: Int
        get() = prefs.getInt("dino_stage", 1)
        set(value) = prefs.edit().putInt("dino_stage", value.coerceIn(1, 4)).apply()

    var hunger: Float
        get() = prefs.getFloat("dino_hunger", 85f)
        set(value) = prefs.edit().putFloat("dino_hunger", value.coerceIn(0f, 100f)).apply()

    var happiness: Float
        get() = prefs.getFloat("dino_happiness", 90f)
        set(value) = prefs.edit().putFloat("dino_happiness", value.coerceIn(0f, 100f)).apply()

    var cleanliness: Float
        get() = prefs.getFloat("dino_clean", 80f)
        set(value) = prefs.edit().putFloat("dino_clean", value.coerceIn(0f, 100f)).apply()

    var energy: Float
        get() = prefs.getFloat("dino_energy", 75f)
        set(value) = prefs.edit().putFloat("dino_energy", value.coerceIn(0f, 100f)).apply()

    var xp: Int
        get() = prefs.getInt("dino_xp", 1450)
        set(value) = prefs.edit().putInt("dino_xp", value.coerceAtLeast(0)).apply()

    var coins: Int
        get() = prefs.getInt("coins", 250)
        set(value) = prefs.edit().putInt("coins", value.coerceAtLeast(0)).apply()

    var overlayEnabled: Boolean
        get() = prefs.getBoolean("overlay_enabled", false)
        set(value) = prefs.edit().putBoolean("overlay_enabled", value).apply()
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    var batteryReactions: Boolean
        get() = prefs.getBoolean("battery_reactions", true)
        set(value) = prefs.edit().putBoolean("battery_reactions", value).apply()

    var keyboardCompanion: Boolean
        get() = prefs.getBoolean("keyboard_companion", true)
        set(value) = prefs.edit().putBoolean("keyboard_companion", value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var lastSimulationTime: Long
        get() = prefs.getLong("last_simulation_time", System.currentTimeMillis())
        set(value) = prefs.edit().putLong("last_simulation_time", value).apply()

    fun simulateTimePassage(now: Long = System.currentTimeMillis()) {
        val last = lastSimulationTime
        if (last <= 0L) {
            lastSimulationTime = now
            return
        }
        val hours = ((now - last).coerceAtLeast(0L) / 3_600_000L).coerceAtMost(72L)
        if (hours > 0L) {
            hunger -= hours * 1.2f
            happiness -= hours * 0.8f
            cleanliness -= hours * 0.6f
            energy += hours * 0.35f
            lastSimulationTime = now
        }
    }
}
