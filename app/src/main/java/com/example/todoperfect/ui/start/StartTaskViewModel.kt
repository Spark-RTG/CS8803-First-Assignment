package com.example.todoperfect.ui.start

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todoperfect.LogUtil
import com.example.todoperfect.TodoPerfectApplication
import com.example.todoperfect.logic.model.Task
import com.example.todoperfect.ui.TimeUpService

class StartTaskViewModel(val currentTask: Task) : ViewModel() {

    companion object {
        const val STOP = false
        const val ONGOING = true
        const val INTERVAL = 10
    }
    private val mediaPlayer = MediaPlayer()
    lateinit var timeUpServiceBinder: TimeUpService.TimeUpBinder
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            timeUpServiceBinder = service as TimeUpService.TimeUpBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }

    }
    val hourTotal: Int = currentTask.hour
    val minTotal: Int = currentTask.min
    private var hourLeft: Int = currentTask.hour
    private var minLeft: Int = currentTask.min
    private var secLeft: Int = 0
    private var progress: Int = 0
    private lateinit var countDownTimer: CountDownTimer

    val statusLiveData = MutableLiveData<Boolean>()

    val finishedLiveData = MutableLiveData<Boolean>()

    val chronometerLiveData = MutableLiveData<ArrayList<Int>>()

    var ongoingProgressBar = "A"

    var moreTimeCount = 0

    init {
        statusLiveData.value = STOP
        finishedLiveData.value = false
        val valueList = ArrayList<Int>()
        valueList.add(hourLeft)
        valueList.add(minLeft)
        valueList.add(secLeft)
        valueList.add(progress)
        chronometerLiveData.value = valueList
        refreshCountDownTimer()
        setUpService()
        prepareMediaPlayer()
    }

    private fun setUpService() {
        val intent = Intent(TodoPerfectApplication.context, TimeUpService::class.java)
        TodoPerfectApplication.context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun refreshCountDownTimer() {
        val millisInFuture = (hourLeft * 3600000 + minLeft * 60000
                + secLeft * 1000).toLong()
        countDownTimer = object : CountDownTimer(millisInFuture, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                hourLeft = (millisUntilFinished / 3600000).toInt()
                minLeft = ((millisUntilFinished % 3600000) / 60000).toInt()
                secLeft = ((millisUntilFinished % 60000) / 1000).toInt()
                val millisTotal = (hourTotal* 3600000 + minTotal * 60000
                        + INTERVAL * 60000 * moreTimeCount).toLong()
                progress = ((millisTotal - millisUntilFinished) * 86400 / millisTotal).toInt()
                val valueList = ArrayList<Int>()
                valueList.add(hourLeft)
                valueList.add(minLeft)
                valueList.add(secLeft)
                valueList.add(progress)
                chronometerLiveData.value = valueList
                LogUtil.d("$hourLeft h $minLeft m $secLeft s")
            }

            override fun onFinish() {
                hourLeft = 0
                minLeft = 0
                secLeft = 0
                progress = 86400
                val valueList = ArrayList<Int>()
                valueList.add(hourLeft)
                valueList.add(minLeft)
                valueList.add(secLeft)
                valueList.add(progress)
                chronometerLiveData.value = valueList
                finishedLiveData.value = true
                statusLiveData.value = STOP
                mediaPlayer.start()
            }
        }
    }

    private fun prepareMediaPlayer() {
        val fd = TodoPerfectApplication.context.assets.openFd("time_up.mp3")
        mediaPlayer.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
        mediaPlayer.prepare()
    }

    fun resetMediaPlayer() {
        mediaPlayer.reset()
        prepareMediaPlayer()
    }

    fun refresh() {
        statusLiveData.value = statusLiveData.value
        val valueList = ArrayList<Int>()
        valueList.add(hourLeft)
        valueList.add(minLeft)
        valueList.add(secLeft)
        valueList.add(progress)
        chronometerLiveData.value = valueList
        finishedLiveData.value = finishedLiveData.value
    }

    fun cancel() {
        countDownTimer.cancel()
    }

    fun moreTime() {
        moreTimeCount++
        hourLeft = 0
        minLeft = INTERVAL
        secLeft = 0
        ongoingProgressBar = "B"
        statusLiveData.value = ONGOING
        finishedLiveData.value = false
        refreshCountDownTimer()
        countDownTimer.start()
        setTimeUpAlarm(true)
    }

    fun statusChange() {
        if (hourLeft + minLeft + secLeft > 0) {
            statusLiveData.value = !(statusLiveData.value)!!
            val temp = statusLiveData.value!!
            if (temp) {
                setTimeUpAlarm(true)
                countDownTimer.start()
            } else {
                setTimeUpAlarm(false)
                countDownTimer.cancel()
                refreshCountDownTimer()
            }
        }
    }

    private fun setTimeUpAlarm(newRegister: Boolean) {
        timeUpServiceBinder.registerTimeUp(
            (hourLeft * 3600000 + minLeft * 60000 + secLeft * 1000).toLong(),
            currentTask.subject, newRegister)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.stop()
        mediaPlayer.release()
        countDownTimer.cancel()
        setTimeUpAlarm(false)
        TodoPerfectApplication.context.unbindService(connection)
    }
}