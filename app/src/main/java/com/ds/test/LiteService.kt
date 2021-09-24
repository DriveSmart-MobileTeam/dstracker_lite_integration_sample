package com.ds.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.drivesmartsdk.singleton.DSManager
import com.drivesmartsdk.singleton.DSManager.Companion.getInstance


class LiteService : Service() {
    private val CHANNEL_ID = "DSTracker channel"

    private lateinit var manager: DSManager

    companion object {
        fun startService(context: Context, message: String, pmd: String) {
            val startIntent = Intent(context, LiteService::class.java)
            startIntent.putExtra("inputExtra", message)
            startIntent.putExtra("PMD", pmd)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context, appicationContext: Context) {
            val stopIntent = Intent(context, LiteService::class.java)
            appicationContext.stopService(stopIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        manager = getInstance(applicationContext)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val input = intent.getStringExtra("inputExtra")
        val pmd = intent.getStringExtra("PMD")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivityKotlin::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DSTracker Service")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_stat_routetracking)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        manager.startDecoupledService(pmd)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        startForceUpload()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "DSTracker",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    private fun startForceUpload() {
        manager.startManualUpload(this)
    }
}
