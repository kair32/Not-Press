package com.aks.notpress.service.notification

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.aks.notpress.R
import com.aks.notpress.service.service.ServiceOverlay

const val ID_NOTIFICATION = 102
class Notification {
    private val tag = "Notification"

    fun init(activity: Activity?) {
        if (activity == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel(activity)
    }

    fun stopNotification(context: Context?) =
        (context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)
            ?.cancel(ID_NOTIFICATION)

    fun statNotification(context: Context?){
        if (context == null) return

        val resultIntent = Intent(context, ServiceOverlay::class.java)
        val resultPendingIntent: PendingIntent = PendingIntent.getService(context, 101, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId= context.getString(R.string.notification_channel_id)
        val mNotificationManager: NotificationManager? = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val mBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(BitmapFactory.decodeResource(context.applicationContext.resources, R.mipmap.ic_launcher_round))
            .setTicker(context.getText(R.string.app_name))
            .setContentTitle(context.getText(R.string.app_name))
            .setContentText(context.getText(R.string.touch))
            .setSound(null)
            .setContentIntent(resultPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)

        mNotificationManager?.notify(ID_NOTIFICATION, mBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(activity: Activity){
        val channelId= activity.getString(R.string.notification_channel_id)
        val name = activity.getString(R.string.notification_channel_name)

            (activity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)?.let { manager ->
                if (manager.getNotificationChannel(channelId) != null) return@let
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
                    .apply {
                        enableLights(false)
                        this.description = activity.getString(R.string.notification_channel_description)
                        setSound(null, null)
                        manager.createNotificationChannel(this)
                    }
            }
    }
}