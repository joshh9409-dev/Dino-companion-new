package com.example.dinocompanion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dinocompanion.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var state: DinoState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        state = DinoState(this)
        render()

        binding.btnFeed.setOnClickListener {
            state.hunger = state.hunger + 12f
            state.happiness = state.happiness + 3f
            state.xp += 25
            render()
        }
        binding.btnPlay.setOnClickListener {
            state.happiness = state.happiness + 12f
            state.hunger = state.hunger - 3f
            state.xp += 30
            render()
        }
        binding.btnClean.setOnClickListener {
            state.cleanliness = state.cleanliness + 15f
            state.xp += 20
            render()
        }
        binding.btnOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ))
                Toast.makeText(this, "Allow Dino Companion to appear over other apps.", Toast.LENGTH_LONG).show()
            } else {
                startService(Intent(this, DinoOverlayService::class.java))
                Toast.makeText(this, "Dino overlay started.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized && ::state.isInitialized) render()
    }

    private fun render() {
        binding.txtDinoName.text = state.dinoName
        binding.txtDinoType.text = state.dinoType
        binding.txtStage.text = "Stage " + state.stage
        binding.txtXp.text = "XP " + state.xp
        binding.progressHunger.progress = state.hunger.toInt()
        binding.progressHappiness.progress = state.happiness.toInt()
        binding.progressClean.progress = state.cleanliness.toInt()
        binding.txtHunger.text = state.hunger.toInt().toString() + "%"
        binding.txtHappiness.text = state.happiness.toInt().toString() + "%"
        binding.txtClean.text = state.cleanliness.toInt().toString() + "%"
    }
}
