package com.example.todoperfect

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.*
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoperfect.logic.model.Task
import com.example.todoperfect.ui.todolist.TaskAdapter
import com.example.todoperfect.ui.todolist.TodoListViewModel
import com.example.todoperfect.ui.todolist.TodoListViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.title.*
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var viewModel: TodoListViewModel
    var editingAdapter: TaskAdapter? = null
    private val taskLists = ArrayList<ArrayList<Task>>()
    val adapters = ArrayList<TaskAdapter>()
    private val recyclers = ArrayList<RecyclerView>()
    private val timeLines = ArrayList<LinearLayout>()
    private val startAdd: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val newTask = it.data?.getSerializableExtra("new_task") as Task
                viewModel.insertTask(newTask)
                viewModel.refresh()
            } else if (it.resultCode == RESULT_CANCELED) {
                //
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this, TodoListViewModelFactory())
            .get(TodoListViewModel::class.java)
        initList()
        initRecyclers()
        initLines()
        initNotificationChannel()
        setUpPeriodicalUpdate()
        setUpClickEvents()
        setUpStatusBar()
        setUpRefresh()
        refreshTaskList()
    }

    override fun onResume() {
        super.onResume()
        randomTitleText()
        viewModel.refresh()
    }

    override fun onPause() {
        super.onPause()
        viewModel.tryUploadBackend()
    }

    // TODO: setUpRefresh

    private fun refreshTaskList() {
        LogUtil.i("Refreshed")
        viewModel.refreshFromCloud()
    }

    // TODO: groupTasks

    private fun randomTitleText() {
        val randomText = listOf(
            "Welcome to TodoPerfect!",
            "Change the world by being yourself.",
            "Every moment is a fresh beginning.",
            "Aspire to inspire before we expire.",
            "Everything you can imagine is real.",
            "Whatever you do, do it well.",
            "What we think, we become.",
            "All limitations are self-imposed.",
            "Be so good they can’t ignore you.",
//          "Yesterday you said tomorrow. Just do it.",
            "Any noble work is impossible at first.",
            "Just do it!",
            "Strive for greatness.",
            "Believe yourself!",
            "You can, you will.",
            "You are the best!",
            "Turn your wounds into wisdom.",
            "You can totally do this.",
            "No pressure, no diamonds.",
            "Try everything!",
            "Nothing is impossible.",
            "Take the risk or lose the chance.",
            "Prove yourself.",
            "Practice makes perfect.",
            "You never fail until you stop trying.",
            "Failure is the mom of success.",
            "Make the world a better place!",
            "Build a window to see opportunity.",
            "Never too old to learn.",
            "Make each day your masterpiece.",
            "Have a good day!",
            "Stay hungry. Stay foolish.",
            "Dream big, dare to fail.",
            "Take rest when proper.",
            "Try again, fail better.",
            "You can if you think you can.",
            "Open your mind, be yourself.",
            "Always do what you are afraid to do.",
            "Pain is temporary.",
            "Begin anywhere.",
            "Do it now!",
            "Live your dreams.",
            "Happiness depends upon ourselves.",
            "Tough times never last.",
            "All will be the past.",
            "There is no substitute for hard work.",
            "Only the paranoid survive.",
            "Let's GOOOOOOOO!",
            "The choice is yours.",
            "Hold the vision, trust the process.",
            "One day or day one. You decide."
        )
        titleBarRandomText.text = randomText[Random().nextInt(randomText.size - 1)]
    }

    // TODO: initNotificationChannel

    // TODO: setUpPeriodicalUpdate

    // TODO: refreshRecyclers

    private fun setUpClickEvents() {
        menuBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        addBtn.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startAdd.launch(intent)
        }
        editModeBtn.setOnClickListener {
            TaskAdapter.editMode = !(TaskAdapter.editMode)
            TaskAdapter.taskEditing = null
            notifyAdapters()
        }
        mainView.setOnClickListener {
            TaskAdapter.taskEditing = null
            notifyAdapters()
        }
    }

    private fun setUpStatusBar() {
        window.statusBarColor = resources.getColor(R.color.lightBlue)
    }

    private fun initLines() {
        timeLines.add(tomorrowLine)
        timeLines.add(recentLine)
        timeLines.add(weekLine)
    }

    private fun initRecyclers() {
        recyclers.add(overdueView)
        recyclers.add(todayView)
        recyclers.add(recentView)
        recyclers.add(weekView)
        recyclers.add(futureView)
        recyclers.add(starView)
        for (recycler in recyclers) {
            recycler.layoutManager = object: LinearLayoutManager(this) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        }
        for (i in 0..5) {
            adapters.add(TaskAdapter(taskLists[i], this))
        }
        for (i in 0..5) {
            recyclers[i].adapter = adapters[i]
        }
        //staredAdapter.add(adapters[5])
    }

    // TODO: fillList

    private fun initList() {
        repeat(6) {
            taskLists.add(ArrayList<Task>())
        }
    }


    fun notifyAdapters(position: Int = -1) {
        if (position == -1) {
            for (adapter in adapters) {
                adapter.notifyDataSetChanged()
            }
        } else {
            adapters[position].notifyDataSetChanged()
        }
    }

    fun refresh() {
        viewModel.refresh()
    }
}