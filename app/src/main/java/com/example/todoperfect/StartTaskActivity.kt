package com.example.todoperfect

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.todoperfect.logic.model.Task
import com.example.todoperfect.ui.TimeUpService
import com.example.todoperfect.ui.start.StartTaskViewModel
import com.example.todoperfect.ui.start.StartTaskViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_start_task.*

class StartTaskActivity : AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProvider(this,
            StartTaskViewModelFactory(intent.getSerializableExtra("current_task") as Task)
        ).get(StartTaskViewModel::class.java)
    }

    lateinit var currentTask: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_task)
        currentTask = intent.getSerializableExtra("current_task") as Task
        setGradientBackground()
        setUpHeaders()

        cancelTaskBtn.setOnClickListener {
            onBackPressed()
        }

        completeTaskBtn.setOnClickListener {
            viewModel.cancel()
            val intent = Intent()
            intent.putExtra("after_task", currentTask)
            setResult(RESULT_OK, intent)
            finish()
        }

        viewModel.finishedLiveData.observe(this) { finished ->
            if (finished) {
//                LogUtil.i("finished!")
//                if (viewModel.ongoingProgressBar == "A") {
//                    progressBarA.progress = 86400
//                    LogUtil.i("progressA updated 86400.")
//                }
//                progressBarB.progress = 86400
//                LogUtil.i("progressB updated 86400.")
                taskStatusBtn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24)
                Snackbar.make(clock, "Time's Up!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("More time") {
                        viewModel.resetMediaPlayer()
                        viewModel.moreTime()
                        currentTask.hour = 0
                        currentTask.min = StartTaskViewModel.INTERVAL
                        taskStatusBtn
                            .setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24)
                        val tempTotal = viewModel.hourTotal * 3600000 + viewModel.minTotal * 60000 +
                                StartTaskViewModel.INTERVAL * viewModel.moreTimeCount * 60000
                        val preTotal = viewModel.hourTotal * 3600000 + viewModel.minTotal * 60000
                        progressBarA.progress = (preTotal.toLong() * 86400 / tempTotal).toInt()
                        progressBarB.progress = ((tempTotal - StartTaskViewModel.INTERVAL * 60000)
                            .toLong() / tempTotal * 86400).toInt()
                    }
                    .show()
            }
        }

        viewModel.statusLiveData.observe(this) { status ->
            if (status) {
                taskStatusBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24)
            } else {
                taskStatusBtn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24)
            }
        }

        viewModel.chronometerLiveData.observe(this) { list ->
            chronometer.text = StringBuilder().apply {
                val hr = list[0]
                if (hr < 10) {
                    append("0")
                }
                append(hr)
                append(":")
                val min = list[1]
                if (min < 10) {
                    append("0")
                }
                append(min)
                append(":")
                val sec = list[2]
                if (sec < 10) {
                    append("0")
                }
                append(sec)
            }.toString()
            LogUtil.i("text: ${chronometer.text}")
            if (list[2] == 0) {
                currentTask.hour = list[0]
                currentTask.min = list[1]
            }
            if (viewModel.ongoingProgressBar == "A") {
                progressBarA.progress = list[3]
                LogUtil.i("progressA: ${progressBarA.progress}")
            }
            progressBarB.progress = list[3]
            LogUtil.i("progressB: ${progressBarB.progress}")
        }

        taskStatusBtn.setOnClickListener {
            viewModel.statusChange()
        }
        //viewModel.refresh()
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("after_task", currentTask)
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
        hideSystemBar()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    private fun setUpHeaders() {
        subjectIntro.text = currentTask.subject
        descriptionIntro.text = currentTask.description
        descriptionIntro.requestFocus()
    }

    private fun setGradientBackground() {
        val start = resources.getColor(R.color.lightBlue)
        val end = resources.getColor(R.color.lightGreen)
        val colorAnim: ValueAnimator = ObjectAnimator.ofInt(generalLayout, "backgroundColor",
            start, end)
        colorAnim.duration = 10000
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.repeatCount = ValueAnimator.INFINITE
        colorAnim.repeatMode = ValueAnimator.REVERSE
        colorAnim.start()
    }

    private fun hideSystemBar() {
        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }
}