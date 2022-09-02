package com.example.todoperfect.ui

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.todoperfect.R
import kotlinx.android.synthetic.main.title.view.*

class TitleLayout(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.title, this)
    }
}