package com.example.todoperfect.ui.broadcast

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.todoperfect.LogUtil
import com.example.todoperfect.MainActivity
import com.example.todoperfect.R
import com.example.todoperfect.TodoPerfectApplication

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val pm = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "todoperfect:task_alarm")
        wl.acquire()
        val subject = intent?.getStringExtra("current_task_subject")!!
        val taskId = intent.getStringExtra("current_task_id")!!.toLong()
        val requestCode = intent.getStringExtra("time_of_alarm")!!.toInt()
        buildNotification(subject, taskId, requestCode, context)
        wl.release()
    }

    private fun buildNotification(subject: String,id: Long, requestCode: Int, context: Context?) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0 , notificationIntent, 0)
        val notification = NotificationCompat.Builder(context!!, "task_alarm").apply {
            setContentTitle(subject)
            when (requestCode) {
                0 -> {
                    setContentText("\"$subject\" is about to due!")
                }
                1 -> {
                    setContentText("You need to start task \"$subject\" now!")
                }
                2 -> {
                    setContentText("Prepare to start task \"$subject\" later :)")
                }
                in 3..5 -> {
                    setContentText("Don't forget task \"$subject\" :)")
                }
            }
            setVibrate(longArrayOf(100, 400))
            setSmallIcon(R.drawable.small_icon)
            setLargeIcon(BitmapFactory.decodeResource(
                TodoPerfectApplication.context.resources, R.drawable.large_icon))
            setContentIntent(pi)
            setAutoCancel(true)
        }.build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id.toInt() + 1, notification)
    }
}