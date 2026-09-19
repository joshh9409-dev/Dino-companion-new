package com.example.dinocompanion

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
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

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        showDino()
    }

    private fun showDino() {
        if (overlayView != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val root = FrameLayout(this).apply {
            setPadding(4, 4, 4, 4)
        }

        val dino = ImageView(this).apply {
            setImageResource(R.drawable.dino_placeholder)
            contentDescription = "Dino Companion"
            setPadding(7, 7, 7, 7)
            setBackgroundResource(R.drawable.bg_bubble_circle)
        }

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

        menu.addView(title)
        menu.addView(feed)
        menu.addView(play)
        menu.addView(clean)
        menu.addView(info)
        menu.addView(hide)

        root.addView(
            dino,
            FrameLayout.LayoutParams(76, 76).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
            }
        )
        root.addView(
            menu,
            FrameLayout.LayoutParams(150, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                leftMargin = 82
            }
        )

        dino.setOnClickListener {
            menu.visibility = if (menu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        feed.setOnClickListener {
            DinoState(this).apply {
                hunger += 12f
                happiness += 3f
                xp += 25
            }
            menu.visibility = View.GONE
        }

        play.setOnClickListener {
            DinoState(this).apply {
                happiness += 12f
                hunger -= 3f
                energy -= 5f
                xp += 30
            }
            menu.visibility = View.GONE
        }

        clean.setOnClickListener {
            DinoState(this).apply {
                cleanliness += 15f
                xp += 20
            }
            menu.visibility = View.GONE
        }

        info.setOnClickListener {
            toast("${DinoState(this).dinoName} • ${DinoState(this).dinoType}")
            menu.visibility = View.GONE
        }

        hide.setOnClickListener { stopSelf() }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
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
                        downX = event.rawX
                        downY = event.rawY
                        startX = params.x
                        startY = params.y
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + (event.rawX - downX).toInt()
                        params.y = startY + (event.rawY - downY).toInt()
                        windowManager.updateViewLayout(root, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (kotlin.math.abs(event.rawX - downX) < 12 &&
                            kotlin.math.abs(event.rawY - downY) < 12) {
                            v.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(root, params)
        overlayView = root
    }

    private fun action(label: String): TextView =
        TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10, 8, 10, 8)
        }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dino Companion")
            .setContentText("Your Dino is hanging out with you.")
            .setSmallIcon(R.drawable.ic_dino)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Dino Companion",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun toast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        overlayView?.let {
            if (::windowManager.isInitialized) {
                runCatching { windowManager.removeView(it) }
            }
        }
        DinoState(this).overlayEnabled = false
        overlayView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "dino_companion"
        private const val NOTIFICATION_ID = 1001
    }
}
