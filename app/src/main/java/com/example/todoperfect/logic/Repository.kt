package com.example.todoperfect.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.liveData
import com.example.todoperfect.LogUtil
import com.example.todoperfect.TodoPerfectApplication
import com.example.todoperfect.logic.dao.AppDatabase
import com.example.todoperfect.logic.model.Task
import com.example.todoperfect.logic.model.User
import com.example.todoperfect.ui.TaskAlarmService
import kotlinx.coroutines.*
import java.sql.Timestamp
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

object Repository {
    const val SECOND = 1000
    const val MINUTE = 1000 * 60
    const val HOUR = 1000 * 60 * 60
    const val DAY = 1000 * 60 * 60 * 24 * 3

    lateinit var taskAlarmBinder: TaskAlarmService.TaskAlarmBinder
    private val taskDao = AppDatabase.getDatabase(TodoPerfectApplication.context).taskDao()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            taskAlarmBinder = service as TaskAlarmService.TaskAlarmBinder
//            thread {
//                val listOfTasks = taskDao.loadAllTasks()
//                for (task in listOfTasks) {
//                    if (task.due.time > System.currentTimeMillis()) {
//                        taskAlarmBinder.useAlarmForTask(task)
//                    }
//                }
//            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            val intent = Intent("com.example.todoperfect.TASK_ALARM_SERVICE")
            TodoPerfectApplication.context.sendBroadcast(intent)
        }
    }
    init {
        val intent = Intent(TodoPerfectApplication.context, TaskAlarmService::class.java)
        TodoPerfectApplication.context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

    fun sync(remoteTasks: List<Task>) {
        thread {
            runBlocking {
                val localTasks = withContext(Dispatchers.IO) {
                    taskDao.loadAllTasks(TodoPerfectApplication.user!!.email)
                }
                LogUtil.i("Local: $localTasks")
                val remoteSet = HashSet(remoteTasks)
                val localMap = HashMap<Long, Task>()
                for (task in localTasks) {
                    localMap[task.id] = task
                }
                val add = ArrayList<Task>()
                val update = ArrayList<Task>()
                val delete = ArrayList<Task>()
                for (task in remoteTasks) {
                    if (!localMap.containsKey(task.id)) {
                        add.add(task)
                    } else if (localMap[task.id] != task) {
                        update.add(task)
                    }
                }
                for (task in localTasks) {
                    if (!remoteSet.contains(task)) {
                        delete.add(task)
                    }
                }
                withContext(Dispatchers.IO) {
                    for (task in add) {
                        LogUtil.i("Add: $task")
                        taskDao.insertTask(task)
                    }
                }
                withContext(Dispatchers.IO) {
                    for (task in update) {
                        LogUtil.i("Update: $task")
                        taskDao.updateTask(task)
                    }
                }
                withContext(Dispatchers.IO) {
                    for (task in delete) {
                        LogUtil.i("Delete: $task")
                        taskDao.deleteTask(task)
                    }
                }
            }
        }
    }

    fun push() {
        thread {
            runBlocking {
                withContext(Dispatchers.Default) {
                    val waitingAdd = ArrayList(SharedPreference.waitingAdd.values)
                    val waitingUpdate = ArrayList(SharedPreference.waitingUpdate.values)
                    val waitingDelete = ArrayList(SharedPreference.waitingDelete.values)
                    var temp = true
                    if (waitingAdd.isEmpty() && waitingAdd.isEmpty() && waitingDelete.isEmpty()) {
                        return@withContext
                    }
                    LogUtil.d("Try upload: \n Add: $waitingAdd \n" +
                            "Update: $waitingUpdate \n" +
                            "Delete: $waitingDelete")
                    async {
                        try {
                            Cloud.insertTasks(waitingAdd)
                        } catch (e: Exception) {
                            LogUtil.e("Push insert failed: " + e.toString())
                            temp = false
                        }
                    }.await()
                    async {
                        try {
                            Cloud.updateTasks(waitingUpdate)
                        } catch (e: Exception) {
                            LogUtil.e("Push update failed: " + e.toString())
                            temp = false
                        }
                    }.await()
                    async {
                        try {
                            Cloud.deleteTasks(waitingDelete)
                        } catch (e: Exception) {
                            LogUtil.e("Push delete failed: " + e.toString())
                            temp = false
                        }
                    }.await()
                    if (temp) {
                        SharedPreference.freeWaitingList()
                    }
                }
            }
        }
    }
    fun insertTask(task: Task) {
        thread {
            runBlocking {
                withContext(Dispatchers.Default) {
                    try {
                        task.id = taskDao.insertTask(task)
                        Cloud.insertTasks(listOf(task))
                    } catch (e: Exception) {
                        LogUtil.e(e.toString())
                        SharedPreference.waitAdd(task)
                    }
                }
                taskAlarmBinder.useAlarmForTask(task)
            }
        }
    }

    fun updateTask(task: Task) {
        thread {
            runBlocking {
                withContext(Dispatchers.Default) {
                    try {
                        taskDao.updateTask(task)
                        Cloud.updateTasks(listOf(task))
                    } catch (e: Exception) {
                        LogUtil.e(e.toString())
                        SharedPreference.waitUpdate(task)
                    }
                }
                taskAlarmBinder.useAlarmForTask(task)
            }
        }
    }

    fun deleteTask(task: Task) {
        thread {
            runBlocking {
                withContext(Dispatchers.Default) {
                    try {
                        taskDao.deleteTask(task)
                        Cloud.deleteTasks(listOf(task))
                    } catch (e: Exception) {
                        LogUtil.e(e.toString())
                        SharedPreference.waitDelete(task)
                    }
                }
                taskAlarmBinder.deleteAlarmForTask(task)
            }
        }
    }

    fun refreshTaskList(current: Calendar) = fire(Dispatchers.IO) {
        //Log.i("TodoPerfect", "Processing refresh")
        coroutineScope {
            val duplicate = GregorianCalendar()
            duplicate.set(Calendar.HOUR_OF_DAY, 0)
            duplicate.set(Calendar.MINUTE, 0)
            duplicate.set(Calendar.SECOND, 0)
            duplicate.set(Calendar.MILLISECOND, 0)
            val now = Timestamp(current.timeInMillis)
            duplicate.add(Calendar.DAY_OF_MONTH, 1)
            val tomorrow = Timestamp(duplicate.timeInMillis)
            duplicate.add(Calendar.DAY_OF_MONTH, 2)
            val threeDays = Timestamp(duplicate.timeInMillis)
            duplicate.add(Calendar.DAY_OF_MONTH, 4)
            val weekLater = Timestamp(duplicate.timeInMillis)
            val email = TodoPerfectApplication.user!!.email
            val result = ArrayList<List<Task>>()
            val overdue = taskDao.loadTasksEarlierThan(email, now)
            val today = taskDao.loadTasksBetween(email, now, tomorrow)
            val recent = taskDao.loadTasksBetween(email, tomorrow, threeDays)
            val inWeek = taskDao.loadTasksBetween(email, threeDays, weekLater)
            val future = taskDao.loadTasksAfter(email, weekLater)
            val stared = taskDao.loadStaredTasks(email)
            result.add(overdue)
            result.add(today)
            result.add(recent)
            result.add(inWeek)
            result.add(future)
            result.add(stared)
            Result.success(result)
        }
    }
}