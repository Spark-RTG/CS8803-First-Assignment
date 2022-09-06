package com.example.todoperfect.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.todoperfect.LogUtil
import com.example.todoperfect.TodoPerfectApplication
import com.example.todoperfect.logic.dao.AppDatabase
import com.example.todoperfect.logic.model.Task
import kotlin.concurrent.thread

class TaskAlarmService : Service() {

    private val mBinder = TaskAlarmBinder()

    class TaskAlarmBinder : Binder() {

        fun useAlarmForTask(task: Task) {
            registerAlarmForTask(task, false)
        }

        fun deleteAlarmForTask(task: Task) {
            registerAlarmForTask(task, true)
        }

        private fun registerAlarmForTask(task: Task, delete: Boolean) {
            val now = System.currentTimeMillis()
            val interval = task.hour * 3600000 + task.min * 60000
            if (task.due.time < now) {
                return
            }
            for (i in 0..5) {
                setAlarm(i, task, delete)
            }
        }

        private fun setAlarm(alarmNumber: Int, task: Task, delete: Boolean) {
            val interval = task.hour * 3600000 + task.min * 60000
            val alarmTime = task.due.time - interval * alarmNumber
            val alarmManager: AlarmManager = TodoPerfectApplication.context
                .getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent("com.example.todoperfect.TASK_ALARM")
            intent.setPackage(TodoPerfectApplication.context.packageName)
            intent.putExtra("current_task_subject", task.subject)
            intent.putExtra("time_of_alarm", "$alarmNumber")
            intent.putExtra("current_task_id", task.id.toString())
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            val pi1 = PendingIntent.getBroadcast(
                TodoPerfectApplication.context,
                task.id.toInt() * 5 + alarmNumber, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            if (!delete && alarmTime > System.currentTimeMillis() && task.importance >= alarmNumber
                && (task.hour + task.min > 0 || alarmNumber == 0)) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pi1)
                LogUtil.d("Alarm $alarmNumber set for ${task.subject}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        thread {
            val taskDao = AppDatabase.getDatabase(TodoPerfectApplication.context).taskDao()
            val listOfTasks = taskDao.loadAllTasks(TodoPerfectApplication.user!!.email)
            for (task in listOfTasks) {
                if (task.due.time > System.currentTimeMillis()) {
                    mBinder.useAlarmForTask(task)
                }
            }
        }
//        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel("task_alarm_service", "Front desk service",
//                NotificationManager.IMPORTANCE_DEFAULT)
//            manager.createNotificationChannel(channel)
//        }
//        val intent = Intent(this, MainActivity::class.java)
//        val pi = PendingIntent.getActivity(this, 0, intent, 0)
//        val notification = NotificationCompat.Builder(this, "task_alarm_service")
//            .setContentTitle("Alarm service for tasks")
//            .setContentText("Service to keep you noticed about your tasks.")
//            .setSmallIcon(R.drawable.small_icon)
//            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.large_icon))
//            .setContentIntent(pi)
//            .build()
//        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent("com.example.todoperfect.TASK_ALARM_SERVICE")
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}