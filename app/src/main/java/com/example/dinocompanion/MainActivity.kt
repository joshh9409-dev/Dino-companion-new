package com.example.dinocompanion

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dinocompanion.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var state: DinoState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        state = DinoState(this)
        wireNavigation()
        wireHome()
        wireCare()
        wireDino()
        wireShop()
        wireSettings()
        render()
        showPage(binding.pageHome)
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized && ::state.isInitialized) render()
    }

    private fun wireNavigation() {
        binding.navHome.setOnClickListener { showPage(binding.pageHome) }
        binding.navDino.setOnClickListener { showPage(binding.pageDino) }
        binding.navCare.setOnClickListener { showPage(binding.pageCare) }
        binding.navShop.setOnClickListener { showPage(binding.pageShop) }
        binding.btnSettingsTop.setOnClickListener { showPage(binding.pageSettings) }
        binding.btnSettingsHome.setOnClickListener { showPage(binding.pageSettings) }
        binding.btnBackDino.setOnClickListener { showPage(binding.pageHome) }
        binding.btnBackCare.setOnClickListener { showPage(binding.pageHome) }
        binding.btnBackShop.setOnClickListener { showPage(binding.pageHome) }
        binding.btnBackSettings.setOnClickListener { showPage(binding.pageHome) }
    }

    private fun wireHome() {
        binding.btnCareHome.setOnClickListener { showPage(binding.pageCare) }
        binding.btnShopHome.setOnClickListener { showPage(binding.pageShop) }
        binding.btnDinoHome.setOnClickListener { showPage(binding.pageDino) }
        binding.btnChoose.setOnClickListener { showPage(binding.pageDino) }
        binding.btnRename.setOnClickListener { renameDino() }
        binding.btnRenameDino.setOnClickListener { renameDino() }
        binding.btnOverlay.setOnClickListener { toggleOverlay() }
    }

    private fun wireCare() {
        binding.careFeed.setOnClickListener {
            state.hunger += 12f
            state.happiness += 3f
            state.xp += 25
            toast("Yum! ${state.dinoName} loved the food.")
            render()
        }
        binding.carePlay.setOnClickListener {
            state.happiness += 12f
            state.hunger -= 3f
            state.energy -= 5f
            state.xp += 30
            toast("${state.dinoName} had a great time!")
            render()
        }
        binding.careClean.setOnClickListener {
            state.cleanliness += 15f
            state.xp += 20
            toast("${state.dinoName} is sparkling clean.")
            render()
        }
        binding.careSleep.setOnClickListener {
            state.energy += 20f
            state.happiness += 3f
            state.xp += 15
            toast("${state.dinoName} had a nice nap.")
            render()
        }
    }

    private fun wireDino() {
        binding.selectTRex.setOnClickListener { selectDino("T-Rex", 0) }
        binding.selectTriceratops.setOnClickListener { selectDino("Triceratops", 300) }
        binding.selectPtero.setOnClickListener { selectDino("Pterodactyl", 500) }
        binding.selectStego.setOnClickListener { selectDino("Stegosaurus", 700) }
    }

    private fun wireShop() {
        binding.shopFood.setOnClickListener { buyItem("Food Pack", 100) }
        binding.shopToy.setOnClickListener { buyItem("Toy Pack", 100) }
        binding.shopCare.setOnClickListener { buyItem("Care Pack", 100) }
        binding.shopRareEgg.setOnClickListener { buyItem("Rare Egg", 300) }
        binding.shopEpicEgg.setOnClickListener { buyItem("Epic Egg", 500) }
        binding.shopCoins.setOnClickListener {
            state.coins += 500
            toast("+500 coins added to your Dino wallet.")
            render()
        }
    }

    private fun wireSettings() {
        binding.settingOverlay.setOnClickListener { toggleOverlay() }
        binding.settingNotifications.setOnClickListener {
            toast("Notifications are enabled for Dino reactions.")
        }
        binding.settingBattery.setOnClickListener {
            toggleText(binding.settingBattery, "Battery reactions")
        }
        binding.settingKeyboard.setOnClickListener {
            toggleText(binding.settingKeyboard, "Keyboard companion")
        }
        binding.settingSound.setOnClickListener {
            toggleText(binding.settingSound, "Sound & voice")
        }
    }

    private fun toggleText(view: android.widget.TextView, label: String) {
        val isOn = view.text.toString().endsWith("ON")
        view.text = "$label     ${if (isOn) "OFF" else "ON"}"
    }

    private fun selectDino(type: String, cost: Int) {
        if (state.dinoType == type) {
            toast("${state.dinoName} is already your $type.")
            return
        }
        if (cost > 0 && state.coins < cost) {
            toast("You need $cost coins to unlock $type.")
            showPage(binding.pageShop)
            return
        }
        if (cost > 0) state.coins -= cost
        state.dinoType = type
        state.stage = 1
        state.xp = 0
        toast("$type selected! Welcome to your new companion.")
        render()
        showPage(binding.pageDino)
    }

    private fun buyItem(name: String, cost: Int) {
        if (state.coins < cost) {
            toast("Not enough coins for $name.")
            return
        }
        state.coins -= cost
        state.xp += 10
        toast("$name purchased.")
        render()
    }

    private fun renameDino() {
        val input = EditText(this).apply {
            setSingleLine(true)
            hint = "Dino name"
            setText(state.dinoName)
            selectAll()
            setPadding(28, 12, 28, 12)
        }

        AlertDialog.Builder(this)
            .setTitle("Rename your Dino")
            .setMessage("Give your companion a name.")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    state.dinoName = name
                    render()
                    toast("Your Dino is now $name.")
                }
            }
            .show()
    }

    private fun toggleOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
            toast("Allow Dino Companion to appear over other apps, then return here.")
            return
        }

        if (state.overlayEnabled) {
            stopService(Intent(this, DinoOverlayService::class.java))
            state.overlayEnabled = false
            toast("Floating Dino hidden.")
        } else {
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 42)
            }
            ContextCompat.startForegroundService(
                this,
                Intent(this, DinoOverlayService::class.java)
            )
            state.overlayEnabled = true
            toast("Floating Dino is now on.")
        }
        render()
    }

    private fun showPage(page: android.view.View) {
        binding.pageHome.visibility = android.view.View.GONE
        binding.pageDino.visibility = android.view.View.GONE
        binding.pageCare.visibility = android.view.View.GONE
        binding.pageShop.visibility = android.view.View.GONE
        binding.pageSettings.visibility = android.view.View.GONE
        page.visibility = android.view.View.VISIBLE
        binding.scroll.scrollTo(0, 0)

        val active = when (page) {
            binding.pageDino -> binding.navDino
            binding.pageCare -> binding.navCare
            binding.pageShop -> binding.navShop
            else -> binding.navHome
        }
        val navs = listOf(binding.navHome, binding.navDino, binding.navCare, binding.navShop)
        navs.forEach { it.alpha = if (it == active) 1f else 0.55f }
    }

    private fun render() {
        val evolution = (state.xp % 1000) / 10
        val stageName = when (state.stage) {
            1 -> "Baby"
            2 -> "Young"
            3 -> "Adult"
            else -> "Legendary"
        }

        binding.txtDinoName.text = state.dinoName
        binding.txtDinoType.text = "${state.dinoType} • $stageName"
        binding.txtStage.text = "Stage ${state.stage} • $evolution% to next evolution"
        binding.txtXp.text = "${state.xp} XP"
        binding.txtCoins.text = "● ${state.coins}"
        binding.txtCoinsShop.text = "● ${state.coins}"

        binding.progressEvolution.progress = evolution
        binding.progressHunger.progress = state.hunger.toInt()
        binding.progressHappiness.progress = state.happiness.toInt()
        binding.progressClean.progress = state.cleanliness.toInt()

        binding.txtHunger.text = "Hunger     ${state.hunger.toInt()}%"
        binding.txtHappiness.text = "Happiness  ${state.happiness.toInt()}%"
        binding.txtClean.text = "Cleanliness ${state.cleanliness.toInt()}%"

        binding.txtDinoPageName.text = state.dinoName
        binding.txtDinoPageType.text = "${state.dinoType} • Stage ${state.stage}"
        binding.txtCareGreeting.text = "${state.dinoName} is waiting for you!"
        binding.settingOverlay.text =
            "Floating Dino     ${if (state.overlayEnabled) "ON" else "OFF"}"

        val type = state.dinoType
        binding.selectTRex.text = "🦖\nT-Rex\n${if (type == "T-Rex") "Selected" else "300 coins"}"
        binding.selectTriceratops.text = "🦕\nTriceratops\n${if (type == "Triceratops") "Selected" else "300 coins"}"
        binding.selectPtero.text = "🪽\nPterodactyl\n${if (type == "Pterodactyl") "Selected" else "500 coins"}"
        binding.selectStego.text = "🦕\nStegosaurus\n${if (type == "Stegosaurus") "Selected" else "700 coins"}"
    }

    private fun toast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
