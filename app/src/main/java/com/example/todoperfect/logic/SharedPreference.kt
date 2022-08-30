package com.example.todoperfect.logic

import android.content.Context
import androidx.lifecycle.liveData
import com.example.todoperfect.TodoPerfectApplication
import com.example.todoperfect.logic.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import java.io.FileNotFoundException
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

object SharedPreference {

    init {
        thread {
            val editor =
                TodoPerfectApplication.context.getSharedPreferences(
                    "data", Context.MODE_PRIVATE)
        }
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