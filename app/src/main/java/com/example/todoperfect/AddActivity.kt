package com.example.todoperfect

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        window.statusBarColor = resources.getColor(R.color.lightBlue)
    }
}