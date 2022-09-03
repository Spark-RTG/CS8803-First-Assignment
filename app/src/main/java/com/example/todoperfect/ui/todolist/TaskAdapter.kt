package com.example.todoperfect.ui.todolist

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todoperfect.*
import com.example.todoperfect.logic.Repository
import com.example.todoperfect.logic.model.Task
import com.example.todoperfect.logic.model.TaskImportanceColor
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_start_task.*
import java.lang.StringBuilder
import java.sql.Timestamp
import java.util.*

class TaskAdapter (val taskList: ArrayList<Task>, val activity: AppCompatActivity) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private val startEdit: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val updatedTask = it.data?.getSerializableExtra("new_task") as Task
                Repository.updateTask(updatedTask)
                (activity as MainActivity).editingAdapter?.notifyDataSetChanged()
                val duplicate = GregorianCalendar()
                duplicate.set(Calendar.HOUR_OF_DAY, 0)
                duplicate.set(Calendar.MINUTE, 0)
                duplicate.set(Calendar.SECOND, 0)
                duplicate.set(Calendar.MILLISECOND, 0)
                val now = Timestamp(GregorianCalendar().timeInMillis)
                duplicate.add(Calendar.DAY_OF_MONTH, 1)
                val tomorrow = Timestamp(duplicate.timeInMillis)
                duplicate.add(Calendar.DAY_OF_MONTH, 2)
                val threeDays = Timestamp(duplicate.timeInMillis)
                duplicate.add(Calendar.DAY_OF_MONTH, 4)
                val weekLater = Timestamp(duplicate.timeInMillis)
                if (updatedTask.stared) {
                    activity.editingAdapter = activity.adapters[5]
                } else {
                    when {
                        updatedTask.due < now -> {
                            activity.editingAdapter = activity.adapters[0]
                        }
                        updatedTask.due < tomorrow -> {
                            activity.editingAdapter = activity.adapters[1]
                        }
                        updatedTask.due < threeDays -> {
                            activity.editingAdapter = activity.adapters[2]
                        }
                        updatedTask.due < weekLater -> {
                            activity.editingAdapter = activity.adapters[3]
                        }
                        else -> {
                            activity.editingAdapter = activity.adapters[4]
                        }
                    }
                }
                activity.editingAdapter?.notifyDataSetChanged()
            } else if (it.resultCode == Activity.RESULT_CANCELED) {
                //
            }
        }

    private val startTask: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                Snackbar.make(activity.findViewById(R.id.mainView),
                    "Congrats! You've accomplished a task!",
                    Snackbar.LENGTH_SHORT).show()
                val finishedTask = it.data?.getSerializableExtra("after_task") as Task
                Repository.deleteTask(finishedTask)
                (activity as MainActivity).refresh()
            } else if (it.resultCode == Activity.RESULT_CANCELED) {
                val remainingTask = it.data?.getSerializableExtra("after_task") as Task
                Repository.updateTask(remainingTask)
                (activity as MainActivity).refresh()
            }
        }

    companion object {
        var taskEditing : Task? = null
        var editMode : Boolean = false
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText : TextView = view.findViewById(R.id.taskTimeText)
        val monthText : TextView = view.findViewById(R.id.monthText)
        val dateText : TextView = view.findViewById(R.id.dateText)
        val dateLayout : ConstraintLayout = view.findViewById(R.id.taskDateLayout)
        val subjectText : TextView = view.findViewById(R.id.subjectText)
//        val descriptionText : TextView = view.findViewById(R.id.descriptionText)
//        val timeCostText : TextView = view.findViewById(R.id.timeCostText)
        val dividerLayout : List<FrameLayout> = listOf(
            view.findViewById(R.id.dividerHorizontal))
//            view.findViewById(R.id.dividerVertical))
        val dateSlash: TextView = view.findViewById(R.id.dateSlash)
        val startTaskButton : ImageButton = view.findViewById(R.id.startTaskBtn)
        val deleteTaskButton : ImageButton = view.findViewById(R.id.deleteTaskBtn)
        val topTaskButton : ImageButton = view.findViewById(R.id.topTaskButton)
        val editTaskButton : ImageButton = view.findViewById(R.id.editTaskBtn)
        val dragButton : ImageButton = view.findViewById(R.id.dragBtn)
        val taskCard : MaterialCardView = view.findViewById(R.id.taskCard)
        val innerCard : ConstraintLayout = view.findViewById(R.id.innerCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]
        val duplicate = GregorianCalendar()
        duplicate.set(Calendar.HOUR_OF_DAY, 0)
        duplicate.set(Calendar.MINUTE, 0)
        duplicate.set(Calendar.SECOND, 0)
        duplicate.set(Calendar.MILLISECOND, 0)
        val today = Timestamp(duplicate.timeInMillis)
        val now = Timestamp(GregorianCalendar().timeInMillis)
        duplicate.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrow = Timestamp(duplicate.timeInMillis)
        duplicate.add(Calendar.DAY_OF_MONTH, 2)
        val threeDays = Timestamp(duplicate.timeInMillis)
        duplicate.add(Calendar.DAY_OF_MONTH, 4)
        val weekLater = Timestamp(duplicate.timeInMillis)
        val taskTime = Date(task.due.time)
        holder.apply {
            subjectText.text = task.subject
            //descriptionText.text = task.description
            timeText.text = StringBuilder().let {
                if (taskTime.hours < 10) {
                    it.append("0")
                }
                it.append(taskTime.hours)
                it.append(":")
                if (taskTime.minutes < 10) {
                    it.append("0")
                }
                it.append(taskTime.minutes)
            }
            dateText.text = StringBuilder()
                .append(taskTime.date)
            monthText.text = (taskTime.month + 1).toString()
//            timeCostText.text = StringBuilder().apply {
//                if (task.hour > 0) {
//                    append(task.hour)
//                    append("h ")
//                }
//                append(task.min)
//                append("m")
//            }
            for (layout in dividerLayout) {
                TaskImportanceColor[task.importance]?.let { layout.setBackgroundColor(it) }
            }
            TaskImportanceColor[task.importance]?.let { taskCard.strokeColor = it }

            when {
                editMode -> {
                    dragButton.visibility = View.GONE
                    editTaskButton.visibility = View.GONE
                    deleteTaskButton.visibility = View.VISIBLE
                    topTaskButton.visibility = View.VISIBLE
                    startTaskButton.visibility = View.GONE
                }
                taskList[position].id == taskEditing?.id ?: -1 -> {
                    dragButton.visibility = View.GONE
                    editTaskButton.visibility = View.VISIBLE
                    deleteTaskButton.visibility = View.VISIBLE
                    topTaskButton.visibility = View.GONE
                    startTaskButton.visibility = View.GONE
                }
                else -> {
                    dragButton.visibility = View.GONE
                    editTaskButton.visibility = View.GONE
                    deleteTaskButton.visibility = View.GONE
                    topTaskButton.visibility = View.GONE
                    startTaskButton.visibility = View.VISIBLE
                }
            }

            when {
                task.due < today -> {
//                    innerCard.setBackgroundColor(ContextCompat.getColor(
//                        TodoPerfectApplication.context, R.color.lightPink))
                    timeText.visibility = View.GONE
                    dateText.visibility = View.VISIBLE
                    monthText.visibility = View.VISIBLE
                    dateSlash.visibility = View.VISIBLE
                    dateText.setTextColor(ContextCompat.getColor(
                        TodoPerfectApplication.context, R.color.red))
                    monthText.setTextColor(ContextCompat.getColor(
                        TodoPerfectApplication.context, R.color.red))
                    dateSlash.setTextColor(ContextCompat.getColor(
                        TodoPerfectApplication.context, R.color.red))
                }
                task.due < now -> {
//                    innerCard.setBackgroundColor(ContextCompat.getColor(
//                        TodoPerfectApplication.context, R.color.lightPink))
                    timeText.visibility = View.VISIBLE
                    dateText.visibility = View.GONE
                    monthText.visibility = View.GONE
                    dateSlash.visibility = View.GONE
                    timeText.setTextColor(ContextCompat.getColor(
                        TodoPerfectApplication.context, R.color.red))
                }
                task.due < tomorrow -> {
                    timeText.visibility = View.VISIBLE
                    dateText.visibility = View.GONE
                    monthText.visibility = View.GONE
                    dateSlash.visibility = View.GONE
                }
                else -> {
                    timeText.visibility = View.GONE
                    dateText.visibility = View.VISIBLE
                    monthText.visibility = View.VISIBLE
                    dateSlash.visibility = View.VISIBLE
                }
            }
            if (Timestamp(task.due.time - task.hour * 3600000 - task.min * 60000) < now) {
                innerCard.setBackgroundColor(ContextCompat.getColor(
                    TodoPerfectApplication.context, R.color.lightPink))
            } else {
                innerCard.setBackgroundColor(ContextCompat.getColor(
                    TodoPerfectApplication.context, R.color.transparentWhite))
            }
            itemView.setOnClickListener {
                taskEditing = null
                (activity as MainActivity).editingAdapter?.notifyDataSetChanged()
                (activity as MainActivity).editingAdapter = null
            }
            taskCard.setOnLongClickListener {
                taskEditing = taskList[position]
                this@TaskAdapter.notifyDataSetChanged()
                (activity as MainActivity).editingAdapter?.notifyDataSetChanged()
                (activity as MainActivity).editingAdapter = this@TaskAdapter
                true
            }
            topTaskButton.setOnClickListener {
                val task = taskList[position]
                task.stared = !(task.stared)
                Repository.updateTask(task)
                (activity as MainActivity).refresh()
            }
            if (task.stared) {
                topTaskButton.setBackgroundResource(R.drawable.ic_baseline_star_24)
            } else {
                topTaskButton.setBackgroundResource(R.drawable.ic_baseline_star_border_24)
            }

            startTaskButton.setOnClickListener {
                val intent = Intent(activity, StartTaskActivity::class.java)
                intent.putExtra("current_task", taskList[position])
                startTask.launch(intent)
            }
            editTaskButton.setOnClickListener {
                val intent = Intent(activity, AddActivity::class.java)
                intent.putExtra("current_task", taskList[position])
                startEdit.launch(intent)
            }
            deleteTaskButton.setOnClickListener {
                Repository.deleteTask(taskList[position])
                if (editMode) {
                    taskList.removeAt(position)
                    (activity as MainActivity).notifyAdapters()
                } else {
                    taskList.removeAt(position)
                    notifyDataSetChanged()
                    (activity as MainActivity).editingAdapter = null
                }
            }
        }
    }

    override fun getItemCount() = taskList.size

    private fun setAlarm() {

    }
}