package com.example.todoperfect

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.todoperfect.logic.Repository
import com.example.todoperfect.logic.model.Task
import com.example.todoperfect.ui.add.DayOfWeekPicker
import com.example.todoperfect.ui.add.EditTaskViewModel
import com.example.todoperfect.ui.add.EditTaskViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.edit_task_fragment.*
import java.lang.StringBuilder
import java.sql.Timestamp
import java.util.*

class EditTaskFragment : Fragment() {

    val viewModel by lazy {
        ViewModelProvider(this,
            EditTaskViewModelFactory(activity?.intent?.
            getSerializableExtra("current_task") as Task?))
            .get(EditTaskViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.edit_task_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpTitle()
        setUpRequiredFields()
        setUpPickers()
        viewModel.refreshDue()
        viewModel.refreshImportance()
    }

    private fun setUpTitle() {
        if (activity?.intent?.getSerializableExtra("current_task") != null) {
            titleText.text = "Edit Task"
        } else {
            titleText.text = "New Task"
        }

        cancelBtn.setOnClickListener {
            activity?.setResult(Activity.RESULT_CANCELED)
            activity?.finish()
        }
        completeBtn.setOnClickListener {
            if (subjectEdit.text.isNullOrBlank()) {
                Snackbar.make(subjectLayout, "Please fill in the subject field.",
                    Snackbar.LENGTH_SHORT).show()
            } else {
                val selectedDate = Date(dueDatePicker.year - 1900,
                    dueDatePicker.month,
                    dueDatePicker.dayOfMonth, dueTimePicker.hour, dueTimePicker.minute)
                val newTask : Task
                if (activity?.intent?.getSerializableExtra("current_task") != null) {
                    val currentTask = activity?.intent?.
                    getSerializableExtra("current_task") as Task
                    currentTask.subject = subjectEdit.text.toString()
                    if (descriptionEdit.text.isNullOrBlank()) {
                        currentTask.description = "No Description"
                    } else {
                        currentTask.description = descriptionEdit.text.toString()
                    }
                    currentTask.hour = hourPicker.value
                    currentTask.min = minutePicker.value
                    currentTask.due = Timestamp(selectedDate.time)
                    currentTask.importance = viewModel.importanceLiveData.value!!
                    newTask = currentTask
                } else {
                    val tempDescription: String
                    if (descriptionEdit.text.isNullOrBlank()) {
                        tempDescription = "No Description"
                    } else {
                        tempDescription = descriptionEdit.text.toString()
                    }
                    newTask = Task(subjectEdit.text.toString(),
                        tempDescription,
                        hourPicker.value, minutePicker.value,
                        Timestamp(selectedDate.time),
                        viewModel.importanceLiveData.value!!, false)
                }
                val intent = Intent()
                intent.putExtra("new_task", newTask)
                activity?.setResult(Activity.RESULT_OK, intent)
                activity?.finish()
            }
        }
    }

    private fun setUpRequiredFields() {
        subjectEdit.text = viewModel.subject
        descriptionEdit.text = viewModel.description
        subjectEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.subject = s
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        descriptionEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.description = s
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        importanceBtns.check(EditTaskViewModel.
        importanceToBtn[viewModel.importanceLiveData.value!!]!!)
        importanceBtns.setOnCheckedChangeListener { _, id ->
            viewModel.importanceLiveData.value = when (id) {
                R.id.trivialBtn -> Task.TRIVIAL
                R.id.normalBtn -> Task.NORMAL
                R.id.importantBtn -> Task.IMPORTANT
                R.id.milestoneBtn -> Task.MILESTONE
                R.id.memorialBtn -> Task.MEMORIAL
                else -> Task.TRIVIAL
            }
        }
        viewModel.importanceLiveData.observe(viewLifecycleOwner) { importance ->
            trivialText.visibility = View.GONE
            normalText.visibility = View.GONE
            importantText.visibility = View.GONE
            milestoneText.visibility = View.GONE
            memorialText.visibility = View.GONE
            when (importance) {
                Task.TRIVIAL -> trivialText.visibility = View.VISIBLE
                Task.NORMAL -> normalText.visibility = View.VISIBLE
                Task.IMPORTANT -> importantText.visibility = View.VISIBLE
                Task.MILESTONE -> milestoneText.visibility = View.VISIBLE
                Task.MEMORIAL -> memorialText.visibility = View.VISIBLE
            }
        }
    }

    private fun setUpPickers() {
        viewModel.dueLiveData.observe(viewLifecycleOwner) { date ->
            dueDatePicker.updateDate(date.year + 1900, date.month, date.date)
            dueTimePicker.hour = date.hours
            dueTimePicker.minute = date.minutes
            hourPicker.minValue = 0
            hourPicker.maxValue = 23
            dayPicker.setItemClickListener(object: DayOfWeekPicker.ItemClickListener {
                override fun onClick(dayOfWeek: Int, position: Int) {
                    val newTime = Date(date.time + 86400000 * (dayOfWeek - date.day))
                    viewModel.refreshTimeSpan(newTime.time)
                }
            })
            dayPicker.checkDay(date.day)
        }
        dueTimePicker.setIs24HourView(true)
        val hours = Array(24) { i ->
            StringBuilder().apply {
                if (i < 10) {
                    append("0")
                }
                append(i)
            }.toString()
        }
        hourPicker.displayedValues = hours
        hourPicker.value = viewModel.hourCost
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        val mins = Array(60) { i ->
            StringBuilder().apply {
                if (i < 10) {
                    append("0")
                }
                append(i)
            }.toString()
        }
        minutePicker.displayedValues = mins
        minutePicker.value = viewModel.minuteCost
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dueDatePicker.setOnDateChangedListener{ _, year, month, date ->
                viewModel.refreshTimeSpan(year - 1900, month, date,
                    dueTimePicker.hour, dueTimePicker.minute)
            }
            dueTimePicker.setOnTimeChangedListener { _, hour, minute ->
                viewModel.refreshTimeSpan(dueDatePicker.year - 1900, dueDatePicker.month,
                    dueDatePicker.dayOfMonth, hour, minute)
            }
        } else {
            timeSpanText.text = "Have a great day!"
        }
        viewModel.timeSpanLiveData.observe(viewLifecycleOwner) { text ->
            timeSpanText.text = text
        }
    }
}