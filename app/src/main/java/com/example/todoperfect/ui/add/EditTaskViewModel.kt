package com.example.todoperfect.ui.add

import android.text.Editable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.todoperfect.R
import com.example.todoperfect.logic.model.Task
import java.lang.StringBuilder
import java.util.*


class EditTaskViewModel(currentTask: Task?) : ViewModel() {
    companion object {
        val importanceToBtn = mapOf(
            Task.TRIVIAL to R.id.trivialBtn,
            Task.NORMAL to R.id.normalBtn,
            Task.IMPORTANT to R.id.importantBtn,
            Task.MILESTONE to R.id.milestoneBtn,
            Task.MEMORIAL to R.id.memorialBtn
        )
    }
    var subject: Editable

    var description: Editable

    val importanceLiveData = MutableLiveData<Int>()

    val dueLiveData = MutableLiveData<Date>()

    val timeSpanLiveData: LiveData<String> = Transformations.map(dueLiveData) { date ->
        val due = date.time
        val current = GregorianCalendar().time.time
        if (due < current) {
            "That's already an overdue task."
        } else {
            StringBuilder().apply {
                append("That's ")
                val days = ((due - current) / 86400000).toInt()
                if (days >= 1) {
                    append(days)
                    if (days == 1) {
                        append(" day ")
                    } else {
                        append(" days ")
                    }
                }
                val hours = (((due - current) % 86400000) / 3600000).toInt()
                if (hours >= 1) {
                    append(hours)
                    if (hours == 1) {
                        append(" hour ")
                    } else {
                        append(" hours ")
                    }
                }
                val mins = (((due - current) % 3600000) / 60000).toInt()
                if (mins >= 0) {
                    append(mins)
                    if (mins <= 1) {
                        append(" min ")
                    } else {
                        append(" mins ")
                    }
                }
                append("from now.")
            }.toString()
        }
    }

    var hourCost: Int

    var minuteCost: Int

    init {
        if (currentTask != null) {
            hourCost = currentTask.hour
            minuteCost = currentTask.min
            subject = Editable.Factory().newEditable(currentTask.subject)
            description = Editable.Factory().newEditable(currentTask.description)
            importanceLiveData.value = currentTask.importance
            dueLiveData.value = currentTask.due
        } else {
            subject = Editable.Factory().newEditable("Trivial Task")
            description = Editable.Factory().newEditable("")
            importanceLiveData.value = Task.TRIVIAL
            dueLiveData.value = Date(GregorianCalendar().time.time + 3 * 3600000)
            hourCost = 0
            minuteCost = 20
        }
    }

    fun refreshTimeSpan(year: Int, month: Int, date: Int, hour: Int, min: Int) {
        val newDate = Date(year, month, date, hour, min)
        dueLiveData.value = newDate
    }

    fun refreshTimeSpan(time: Long) {
        val newDate = Date(time)
        dueLiveData.value = newDate
    }
    fun refreshImportance() {
        importanceLiveData.value = importanceLiveData.value
    }

    fun refreshDue() {
        dueLiveData.value = dueLiveData.value
    }
}