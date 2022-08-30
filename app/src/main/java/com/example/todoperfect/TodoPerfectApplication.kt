package com.example.todoperfect

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.todoperfect.logic.model.User

class TodoPerfectApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var user: User? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}