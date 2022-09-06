package com.example.todoperfect.ui.broadcast

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.todoperfect.MainActivity
import com.example.todoperfect.R
import com.example.todoperfect.StartTaskActivity
import com.example.todoperfect.TodoPerfectApplication

class TimeUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val subject = intent?.getStringExtra("current_task")
        val pm = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "todoperfect:time_up")
        wl.acquire()
        val notificationIntent = Intent(context, StartTaskActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0 , notificationIntent, 0)
        val notification = NotificationCompat.Builder(context, "task_alarm").apply {
            setContentTitle(subject)
            setContentText("Time's Up!")
            setVibrate(longArrayOf(100, 400))
            setSmallIcon(R.drawable.small_icon)
            setLargeIcon(
                BitmapFactory.decodeResource(
                TodoPerfectApplication.context.resources, R.drawable.large_icon))
            setContentIntent(pi)
            setAutoCancel(true)
        }.build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, notification)
        wl.release()
    }

}