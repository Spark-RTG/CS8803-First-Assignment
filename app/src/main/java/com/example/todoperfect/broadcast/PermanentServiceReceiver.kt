package com.example.todoperfect.ui.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todoperfect.ui.TaskAlarmService

class PermanentServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startService(Intent(context, TaskAlarmService::class.java))
    }

}