package com.pixeldarken.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.pixeldarken.app.MainActivity
import com.pixeldarken.app.R
import com.pixeldarken.app.SpotStore

class PixelDarkenService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: DarkSpotOverlayView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        running = true
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        attachOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_REFRESH -> refreshSpots()
            else -> refreshSpots()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        detachOverlay()
        running = false
        super.onDestroy()
    }

    private fun attachOverlay() {
        if (overlayView != null) return

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val view = DarkSpotOverlayView(this)
        overlayView = view

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        wm.addView(view, params)
        refreshSpots()
    }

    private fun detachOverlay() {
        val view = overlayView ?: return
        try {
            windowManager?.removeView(view)
        } catch (_: Exception) {
            // already removed
        }
        overlayView = null
        windowManager = null
    }

    private fun refreshSpots() {
        overlayView?.spots = SpotStore.load(this)
    }

    private fun createChannel() {
        val mgr = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        mgr.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this,
            1,
            Intent(this, PixelDarkenService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(open)
            .addAction(0, getString(R.string.stop_overlay), stop)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "pixel_darken_overlay"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.pixeldarken.app.STOP"
        const val ACTION_REFRESH = "com.pixeldarken.app.REFRESH"

        fun start(context: Context) {
            val intent = Intent(context, PixelDarkenService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, PixelDarkenService::class.java).setAction(ACTION_STOP)
            )
        }

        fun refresh(context: Context) {
            context.startService(
                Intent(context, PixelDarkenService::class.java).setAction(ACTION_REFRESH)
            )
        }

        @Volatile
        private var running: Boolean = false

        fun isRunning(@Suppress("UNUSED_PARAMETER") context: Context): Boolean = running
    }
}
