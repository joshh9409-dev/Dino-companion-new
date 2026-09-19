package com.example.dinocompanion

import android.content.Context
import android.content.SharedPreferences

class DinoState(context: Context) {
    private val prefs = context.getSharedPreferences("DinoPrefs", Context.MODE_PRIVATE)

    var dinoName: String
        get() = prefs.getString("dino_name", "Rex") ?: "Rex"
        set(value) = prefs.edit().putString("dino_name", value).apply()

    var dinoType: String
        get() = prefs.getString("dino_type", "T-Rex") ?: "T-Rex"
        set(value) = prefs.edit().putString("dino_type", value).apply()

    var stage: Int
        get() = prefs.getInt("dino_stage", 3)
        set(value) = prefs.edit().putInt("dino_stage", value).apply()

    var hunger: Float
        get() = prefs.getFloat("dino_hunger", 85f)
        set(value) = prefs.edit().putFloat("dino_hunger", value.coerceIn(0f, 100f)).apply()

    var happiness: Float
        get() = prefs.getFloat("dino_happiness", 90f)
        set(value) = prefs.edit().putFloat("dino_happiness", value.coerceIn(0f, 100f)).apply()

    var cleanliness: Float
        get() = prefs.getFloat("dino_clean", 80f)
        set(value) = prefs.edit().putFloat("dino_clean", value.coerceIn(0f, 100f)).apply()

    var xp: Int
        get() = prefs.getInt("dino_xp", 1450)
        set(value) = prefs.edit().putInt("dino_xp", value).apply()
}
