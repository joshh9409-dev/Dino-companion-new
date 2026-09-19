package com.example.dinocompanion

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

class DinoOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        showDino()
    }

    private fun showDino() {
        if (overlayView != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            setBackgroundResource(R.drawable.bg_bubble_circle)
        }

        val dino = ImageView(this).apply {
            setImageResource(R.drawable.dino_placeholder)
            contentDescription = "Dino Companion"
            setPadding(8, 8, 8, 8)
        }

        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            visibility = View.GONE
        }

        val feed = action("Feed")
        val play = action("Play")
        val clean = action("Clean")
        val hide = action("Hide")
        menu.addView(feed)
        menu.addView(play)
        menu.addView(clean)
        menu.addView(hide)
        root.addView(dino, LinearLayout.LayoutParams(72, 72))
        root.addView(menu)

        dino.setOnClickListener {
            menu.visibility = if (menu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        feed.setOnClickListener {
            DinoState(this).apply { hunger += 12f; happiness += 3f; xp += 25 }
            menu.visibility = View.GONE
        }
        play.setOnClickListener {
            DinoState(this).apply { happiness += 12f; hunger -= 3f; xp += 30 }
            menu.visibility = View.GONE
        }
        clean.setOnClickListener {
            DinoState(this).apply { cleanliness += 15f; xp += 20 }
            menu.visibility = View.GONE
        }
        hide.setOnClickListener { stopSelf() }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 180
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
            textSize = 10f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            setBackgroundResource(R.drawable.bg_card_rounded)
        }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dino Companion")
            .setContentText("Your Dino is hanging out.")
            .setSmallIcon(R.drawable.ic_dino)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Dino Companion", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        overlayView?.let {
            if (::windowManager.isInitialized) windowManager.removeView(it)
        }
        overlayView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "dino_companion"
        private const val NOTIFICATION_ID = 1001
    }
}
