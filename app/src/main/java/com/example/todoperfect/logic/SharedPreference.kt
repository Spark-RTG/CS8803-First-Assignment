package com.example.todoperfect.logic

import android.content.Context
import androidx.lifecycle.liveData
import com.example.todoperfect.TodoPerfectApplication
import com.example.todoperfect.logic.model.Task
import com.example.todoperfect.logic.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import java.io.FileNotFoundException
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

object SharedPreference {

    lateinit var waitingAdd: HashMap<Long, Task>
    lateinit var waitingUpdate: HashMap<Long, Task>
    lateinit var waitingDelete: HashMap<Long, Task>

    init {
        thread {
            val editor =
                TodoPerfectApplication.context.getSharedPreferences(
                    "data", Context.MODE_PRIVATE)
            val gson = Gson()
            val addJson = editor.getString("add", "[]")
            val updateJson = editor.getString("update", "[]")
            val deleteJson = editor.getString("delete", "[]")
            val myType = object : TypeToken<HashMap<Long, Task>>() {}.type
            waitingAdd = gson.fromJson(addJson, myType)
            waitingUpdate = gson.fromJson(updateJson, myType)
            waitingDelete = gson.fromJson(deleteJson, myType)
        }
    }

    fun waitAdd(task: Task) {
        waitingAdd[task.id] = task
        val editor =
            TodoPerfectApplication.context.getSharedPreferences(
                "data", Context.MODE_PRIVATE
            ).edit()
        val gson = Gson()
        val json = gson.toJson(waitingAdd)
        editor.apply {
            putString("add", json)
        }
        editor.apply()
    }

    fun waitUpdate(task: Task) {
        val editor =
            TodoPerfectApplication.context.getSharedPreferences(
                "data", Context.MODE_PRIVATE
            ).edit()
        val gson = Gson()
        if (waitingAdd.containsKey(task.id)) {
            waitingAdd.remove(task.id)
            waitingAdd[task.id] = task
            val json = gson.toJson(waitingAdd)
            editor.apply {
                putString("add", json)
            }
        }
        waitingUpdate[task.id] = task
        val json = gson.toJson(waitingUpdate)
        editor.apply {
            putString("update", json)
        }
        editor.apply()
    }

    fun waitDelete(task: Task) {
        val editor =
            TodoPerfectApplication.context.getSharedPreferences(
                "data", Context.MODE_PRIVATE
            ).edit()
        val gson = Gson()
        if (waitingAdd.containsKey(task.id)) {
            waitingAdd.remove(task.id)
            val json = gson.toJson(waitingAdd)
            editor.apply {
                putString("add", json)
            }
        }
        if (waitingUpdate.containsKey(task.id)) {
            waitingUpdate.remove(task.id)
            val json = gson.toJson(waitingUpdate)
            editor.apply {
                putString("update", json)
            }
        }
        waitingDelete[task.id] = task
        val json = gson.toJson(waitingDelete)
        editor.apply {
            putString("delete", json)
        }
        editor.apply()
    }

    fun freeWaitingList() {
        waitingAdd.clear()
        waitingUpdate.clear()
        waitingDelete.clear()
        val editor =
            TodoPerfectApplication.context.getSharedPreferences(
                "data", Context.MODE_PRIVATE
            ).edit()
        val gson = Gson()
        val addJson = gson.toJson(waitingAdd)
        val updateJson = gson.toJson(waitingUpdate)
        val deleteJson = gson.toJson(waitingDelete)
        editor.apply {
            putString("add", addJson)
            putString("update", updateJson)
            putString("delete", deleteJson)
        }
        editor.apply()
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

    fun saveUser(user: User) {
        TodoPerfectApplication.user = user
        thread {
            val editor =
                TodoPerfectApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE).edit()
            editor.apply {
                putString("email", user.email)
                //putString("id", user.id)
            }
            editor.apply()
        }
    }

    fun removeUser() {
        TodoPerfectApplication.user = null
        thread {
            val editor =
                TodoPerfectApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE).edit()
            editor.apply {
                remove("email")
            }
            editor.apply()
        }
    }
    fun loadUser() = fire(Dispatchers.IO) {
        coroutineScope {
            if (TodoPerfectApplication.user != null) {
                Result.success(TodoPerfectApplication.user)
            } else {
                val prefs =
                    TodoPerfectApplication.context.getSharedPreferences(
                        "data",
                        Context.MODE_PRIVATE
                    )
                val email = prefs.getString("email", "")!!

                //val id = prefs.getString("id", "")!!
                if (email == "") {
                    Result.failure(FileNotFoundException())
                } else {
                    val user = User(email)
                    TodoPerfectApplication.user = user
                    Result.success(user)
                }
            }
        }
    }
}