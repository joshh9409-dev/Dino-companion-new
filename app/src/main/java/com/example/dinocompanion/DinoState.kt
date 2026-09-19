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
}
