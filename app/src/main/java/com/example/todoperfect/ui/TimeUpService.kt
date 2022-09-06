package com.example.todoperfect.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.todoperfect.LogUtil
import com.example.todoperfect.TodoPerfectApplication
import com.example.todoperfect.logic.model.Task
import java.util.*

class TimeUpService : Service() {
    private val mBinder = TimeUpBinder()

    inner class TimeUpBinder : Binder() {
        fun registerTimeUp(timeInMillis: Long, subject: String, newRegister: Boolean) {
            val alarmTime = System.currentTimeMillis() + timeInMillis
            val intent = Intent("com.example.todoperfect.TIME_UP")
            intent.putExtra("current_task", subject)
            intent.setPackage(TodoPerfectApplication.context.packageName)
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            val pi1 = PendingIntent.getBroadcast(
                TodoPerfectApplication.context,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            if (newRegister) {
                LogUtil.i("${Date(alarmTime).hours} : ${Date(alarmTime).minutes} " +
                        ": ${Date(alarmTime).seconds}")
                val alarmManager: AlarmManager = TodoPerfectApplication.context
                    .getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pi1)
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

}