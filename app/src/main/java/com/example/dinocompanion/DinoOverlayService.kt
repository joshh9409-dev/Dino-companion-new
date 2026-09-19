package com.example.dinocompanion

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class DinoOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var dinoView: ImageView? = null
    private var idleRunning = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_LOW) {
                dinoView?.animate()?.scaleX(0.86f)?.scaleY(0.86f)?.setDuration(250)?.withEndAction {
                    dinoView?.animate()?.scaleX(1.18f)?.scaleY(1.18f)?.setDuration(350)?.withEndAction {
                        dinoView?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(350)?.start()
                    }?.start()
                }?.start()
                getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, buildNotification("Battery is low. Your Dino is getting sleepy."))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildNotification("Your Dino is hanging out with you.")
        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_LOW), Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_LOW))
        }
        showDino()
    }

    private fun showDino() {
        if (overlayView != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val root = FrameLayout(this).apply { setPadding(4, 4, 4, 4) }
        val dino = ImageView(this).apply {
            setImageResource(
                when (DinoState(this@DinoOverlayService).dinoType) {
                    "Triceratops" -> R.drawable.dino_triceratops
                    "Pterodactyl" -> R.drawable.dino_pterodactyl
                    "Stegosaurus" -> R.drawable.dino_stegosaurus
                    else -> R.drawable.dino_trex
                }
            )
            contentDescription = "Dino Companion"
            setPadding(7, 7, 7, 7)
            setBackgroundResource(R.drawable.bg_bubble_circle)
        }
        dinoView = dino

        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(7, 7, 7, 7)
            setBackgroundResource(R.drawable.bg_card_rounded)
            visibility = View.GONE
            elevation = 12f
        }
        val title = TextView(this).apply {
            text = "DINO"
            textSize = 11f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(10, 5, 10, 5)
        }
        val feed = action("🍖  Feed")
        val play = action("●  Play")
        val clean = action("✚  Clean")
        val info = action("ⓘ  Info")
        val hide = action("×  Hide")
        menu.addView(title); menu.addView(feed); menu.addView(play); menu.addView(clean); menu.addView(info); menu.addView(hide)

        root.addView(dino, FrameLayout.LayoutParams(76, 76).apply { gravity = Gravity.CENTER_VERTICAL or Gravity.START })
        root.addView(menu, FrameLayout.LayoutParams(150, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            leftMargin = 82
        })

        dino.setOnClickListener {
            menu.visibility = if (menu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            if (menu.visibility == View.VISIBLE) {
                dino.animate().rotationBy(8f).setDuration(120).withEndAction {
                    dino.animate().rotation(0f).setDuration(120).start()
                }.start()
            }
        }
        feed.setOnClickListener {
            DinoState(this).apply { hunger += 12f; happiness += 3f; xp += 25 }
            menu.visibility = View.GONE
            react()
        }
        play.setOnClickListener {
            DinoState(this).apply { happiness += 12f; hunger -= 3f; energy -= 5f; xp += 30 }
            menu.visibility = View.GONE
            react()
        }
        clean.setOnClickListener {
            DinoState(this).apply { cleanliness += 15f; xp += 20 }
            menu.visibility = View.GONE
            react()
        }
        info.setOnClickListener {
            val s = DinoState(this)
            toast("${s.dinoName} • ${s.dinoType} • Stage ${s.stage}")
            menu.visibility = View.GONE
        }
        hide.setOnClickListener { stopSelf() }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 220
        }

        dino.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f
            var downY = 0f
            var startX = 0
            var startY = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX; downY = event.rawY; startX = params.x; startY = params.y; return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + (event.rawX - downX).toInt()
                        params.y = startY + (event.rawY - downY).toInt()
                        windowManager.updateViewLayout(root, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (kotlin.math.abs(event.rawX - downX) < 12 && kotlin.math.abs(event.rawY - downY) < 12) v.performClick()
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(root, params)
        overlayView = root
        startIdleAnimation()
    }

    private fun startIdleAnimation() {
        if (idleRunning) return
        idleRunning = true
        fun loop() {
            val view = dinoView ?: return
            view.animate().translationY(-5f).setDuration(1100).withEndAction {
                view.animate().translationY(0f).setDuration(1100).withEndAction { loop() }.start()
            }.start()
        }
        loop()
    }

    private fun react() {
        dinoView?.animate()?.scaleX(1.12f)?.scaleY(1.12f)?.setDuration(160)?.withEndAction {
            dinoView?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(180)?.start()
        }?.start()
    }

    private fun action(label: String): TextView = TextView(this).apply {
        text = label
        textSize = 12f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(10, 8, 10, 8)
        isClickable = true
    }

    private fun buildNotification(message: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dino Companion")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_dino)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Dino Companion", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun toast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(batteryReceiver) }
        overlayView?.let { if (::windowManager.isInitialized) runCatching { windowManager.removeView(it) } }
        DinoState(this).overlayEnabled = false
        dinoView = null
        overlayView = null
        idleRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "dino_companion"
        private const val NOTIFICATION_ID = 1001
    }
}
