package com.example.dinocompanion

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ObjectAnimator
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dinocompanion.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var state: DinoState
    private var dinoAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        state = DinoState(this)
        state.simulateTimePassage()
        wireNavigation()
        wireHome()
        wireCare()
        wireDino()
        wireShop()
        wireSettings()
        render()
        showPage(binding.pageHome)
        startDinoAnimation()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized && ::state.isInitialized) {
            render()
            startDinoAnimation()
        }
    }

    override fun onPause() {
        dinoAnimator?.cancel()
        super.onPause()
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
        binding.imgHomeDino.setOnClickListener { dinoReaction("Your Dino is happy to see you!") }
        binding.imgDinoPage.setOnClickListener { dinoReaction("Raaawr! That tickles.") }
    }

    private fun wireCare() {
        binding.careFeed.setOnClickListener {
            state.hunger += 12f
            state.happiness += 3f
            gainXp(25)
            dinoReaction("Yum! ${state.dinoName} loved the food.")
        }
        binding.carePlay.setOnClickListener {
            state.happiness += 12f
            state.hunger -= 3f
            state.energy -= 5f
            gainXp(30)
            dinoReaction("${state.dinoName} had a great time!")
        }
        binding.careClean.setOnClickListener {
            state.cleanliness += 15f
            gainXp(20)
            dinoReaction("${state.dinoName} is sparkling clean.")
        }
        binding.careSleep.setOnClickListener {
            state.energy += 20f
            state.happiness += 3f
            gainXp(15)
            dinoReaction("${state.dinoName} had a nice nap.")
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
            dinoReaction("+500 coins added to your Dino wallet.")
        }
    }

    private fun wireSettings() {
        binding.settingOverlay.setOnClickListener { toggleOverlay() }
        binding.settingNotifications.setOnClickListener {
            state.notificationsEnabled = !state.notificationsEnabled
            dinoReaction("Notifications " + if (state.notificationsEnabled) "enabled." else "disabled.")
        }
        binding.settingBattery.setOnClickListener {
            state.batteryReactions = !state.batteryReactions
            dinoReaction("Battery reactions " + if (state.batteryReactions) "enabled." else "disabled.")
        }
        binding.settingKeyboard.setOnClickListener {
            state.keyboardCompanion = !state.keyboardCompanion
            dinoReaction("Keyboard companion " + if (state.keyboardCompanion) "enabled." else "disabled.")
        }
        binding.settingSound.setOnClickListener {
            state.soundEnabled = !state.soundEnabled
            dinoReaction("Sound & voice " + if (state.soundEnabled) "enabled." else "disabled.")
        }
    }

    private fun selectDino(type: String, cost: Int) {
        if (state.dinoType == type) {
            dinoReaction("${state.dinoName} is already your $type.")
            return
        }
        if (cost > 0 && state.coins < cost) {
            dinoReaction("You need $cost coins to unlock $type.")
            showPage(binding.pageShop)
            return
        }
        if (cost > 0) state.coins -= cost
        state.dinoType = type
        state.stage = 1
        state.xp = 0
        dinoReaction("$type selected! Welcome to your new companion.")
        render()
        showPage(binding.pageDino)
    }

    private fun buyItem(name: String, cost: Int) {
        if (state.coins < cost) {
            dinoReaction("Not enough coins for $name.")
            return
        }
        state.coins -= cost
        gainXp(10)
        dinoReaction("$name purchased.")
    }

    private fun gainXp(amount: Int) {
        val oldStage = state.stage
        state.xp += amount
        state.stage = (state.xp / 1000 + 1).coerceAtMost(4)
        render()

        if (state.stage > oldStage) {
            binding.imgHomeDino.animate()
                .scaleX(1.18f).scaleY(1.18f).setDuration(260)
                .withEndAction {
                    binding.imgHomeDino.animate().scaleX(1f).scaleY(1f).setDuration(260).start()
                }.start()
            AlertDialog.Builder(this)
                .setTitle("Dino evolved!")
                .setMessage("${state.dinoName} reached Stage ${state.stage}. Your companion has grown stronger.")
                .setPositiveButton("Awesome!", null)
                .show()
        }
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
                    dinoReaction("Your Dino is now $name.")
                }
            }
            .show()
    }

    private fun toggleOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            dinoReaction("Allow Dino Companion to appear over other apps, then return here.")
            return
        }
        if (state.overlayEnabled) {
            stopService(Intent(this, DinoOverlayService::class.java))
            state.overlayEnabled = false
            dinoReaction("Floating Dino hidden.")
        } else {
            if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 42)
            ContextCompat.startForegroundService(this, Intent(this, DinoOverlayService::class.java))
            state.overlayEnabled = true
            dinoReaction("Floating Dino is now on.")
        }
        render()
    }

    private fun showPage(page: View) {
        binding.pageHome.visibility = View.GONE
        binding.pageDino.visibility = View.GONE
        binding.pageCare.visibility = View.GONE
        binding.pageShop.visibility = View.GONE
        binding.pageSettings.visibility = View.GONE
        page.visibility = View.VISIBLE
        binding.scroll.scrollTo(0, 0)
        val active = when (page) {
            binding.pageDino -> binding.navDino
            binding.pageCare -> binding.navCare
            binding.pageShop -> binding.navShop
            else -> binding.navHome
        }
        listOf(binding.navHome, binding.navDino, binding.navCare, binding.navShop)
            .forEach { it.alpha = if (it == active) 1f else 0.55f }
    }

    private fun render() {
        val progress = (state.xp % 1000) / 10
        val stageName = when (state.stage) {
            1 -> "Baby"
            2 -> "Young"
            3 -> "Adult"
            else -> "Legendary"
        }
        setDinoArt(binding.imgHomeDino)
        setDinoArt(binding.imgDinoPage)
        binding.txtDinoName.text = state.dinoName
        binding.txtDinoType.text = "${state.dinoType} • $stageName"
        binding.txtStage.text = if (state.stage >= 4) "Stage 4 • Legendary companion" else "Stage ${state.stage} • $progress% to next evolution"
        binding.txtXp.text = "${state.xp} XP"
        binding.txtCoins.text = "● ${state.coins}"
        binding.txtCoinsShop.text = "● ${state.coins}"
        binding.progressEvolution.progress = progress
        binding.progressHunger.progress = state.hunger.toInt()
        binding.progressHappiness.progress = state.happiness.toInt()
        binding.progressClean.progress = state.cleanliness.toInt()
        binding.txtHunger.text = "Hunger     ${state.hunger.toInt()}%"
        binding.txtHappiness.text = "Happiness  ${state.happiness.toInt()}%"
        binding.txtClean.text = "Cleanliness ${state.cleanliness.toInt()}%"
        binding.txtDinoPageName.text = state.dinoName
        binding.txtDinoPageType.text = "${state.dinoType} • Stage ${state.stage}"
        binding.txtCareGreeting.text = "${state.dinoName} is waiting for you!"
        binding.settingOverlay.text = "Floating Dino     ${if (state.overlayEnabled) "ON" else "OFF"}"
        binding.settingNotifications.text = "Notifications     " + if (state.notificationsEnabled) "ON" else "OFF"
        binding.settingBattery.text = "Battery reactions     " + if (state.batteryReactions) "ON" else "OFF"
        binding.settingKeyboard.text = "Keyboard companion     " + if (state.keyboardCompanion) "ON" else "OFF"
        binding.settingSound.text = "Sound & voice     " + if (state.soundEnabled) "ON" else "OFF"
        val type = state.dinoType
        binding.selectTRex.text = "🦖\nT-Rex\n${if (type == "T-Rex") "Selected" else "300 coins"}"
        binding.selectTriceratops.text = "🦕\nTriceratops\n${if (type == "Triceratops") "Selected" else "300 coins"}"
        binding.selectPtero.text = "🪽\nPterodactyl\n${if (type == "Pterodactyl") "Selected" else "500 coins"}"
        binding.selectStego.text = "🦕\nStegosaurus\n${if (type == "Stegosaurus") "Selected" else "700 coins"}"
    }

    private fun setDinoArt(view: android.widget.ImageView) {
        view.setImageResource(
            when (state.dinoType) {
                "Triceratops" -> R.drawable.dino_triceratops
                "Pterodactyl" -> R.drawable.dino_pterodactyl
                "Stegosaurus" -> R.drawable.dino_stegosaurus
                else -> R.drawable.dino_trex
            }
        )
        val scale = when (state.stage) {
            1 -> 0.88f
            2 -> 0.96f
            3 -> 1.04f
            else -> 1.12f
        }
        view.scaleX = scale
        view.scaleY = scale
    }

    private fun startDinoAnimation() {
        dinoAnimator?.cancel()
        dinoAnimator = ObjectAnimator.ofFloat(binding.imgHomeDino, View.TRANSLATION_Y, 0f, -8f, 0f).apply {
            duration = 2400
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun dinoReaction(message: String) {
        binding.imgHomeDino.animate().rotationBy(7f).setDuration(100).withEndAction {
            binding.imgHomeDino.animate().rotationBy(-14f).setDuration(180).withEndAction {
                binding.imgHomeDino.animate().rotation(0f).setDuration(100).start()
            }.start()
        }.start()
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        render()
    }
}
