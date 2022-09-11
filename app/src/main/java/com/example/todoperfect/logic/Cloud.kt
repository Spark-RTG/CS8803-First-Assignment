package com.example.todoperfect.logic

import androidx.lifecycle.liveData
import com.example.todoperfect.LogUtil
import com.example.todoperfect.TodoPerfectApplication
import com.example.todoperfect.logic.model.*
import com.example.todoperfect.logic.network.TodoPerfectNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

import kotlin.coroutines.CoroutineContext

object Cloud {
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                LogUtil.e(e.toString())
                Result.failure<T>(e)
            }
            emit(result)
        }

    fun userLogin(userRequest: UserRequest) = fire(Dispatchers.IO) {
        coroutineScope {
            val userResponse = TodoPerfectNetwork.userLogin(userRequest)
            if (userResponse.body.statusCode == 200) {
                val responseData = userResponse.body.body
                Result.success(responseData)
            } else {
                Result.failure(RuntimeException("Register failed"))
            }
        }
    }

    fun userRegister(userRequest: UserRequest) = fire(Dispatchers.IO) {
        coroutineScope {
            val userResponse = TodoPerfectNetwork.userRegister(userRequest)
            if (userResponse.body.statusCode == 200) {
                val responseData = userResponse.body.body
                Result.success(responseData)
            } else {
                Result.failure(RuntimeException("Register failed"))
            }
        }
    }

    fun userVerify(verifyRequest: VerifyRequest) = fire(Dispatchers.IO) {
        coroutineScope {
            val verifyResponse = TodoPerfectNetwork.userVerify(verifyRequest)
            if (verifyResponse.body.statusCode == 200) {
                val responseData = verifyResponse.body.body
                Result.success(responseData)
            } else {
                Result.failure(RuntimeException("Verification failed"))
            }
        }
    }

    fun pullAllTasks(taskPullRequest: TaskPullRequest) = fire(Dispatchers.IO) {
        coroutineScope {
            val taskResponse = TodoPerfectNetwork.pullAllTasks(taskPullRequest).body
            if (taskResponse.statusCode == 200) {
                val tasks = taskResponse.tasks
                LogUtil.i("Pulled: $tasks")
                Result.success(tasks)
            } else {
                Result.failure(RuntimeException("Pull failed"))
            }
        }
    }

    suspend fun insertTasks(tasks: List<Task>): String {
        LogUtil.i("Inserting $tasks")
        val taskResponse =
            TodoPerfectNetwork.insertTasks(TaskRequest(TaskRequestBodyJSON(tasks))).body
        if (taskResponse.statusCode == 200) {
            return taskResponse.body
        } else {
            LogUtil.e(taskResponse.body)
            throw RuntimeException(taskResponse.body)
        }
    }

    suspend fun updateTasks(tasks: List<Task>): String {
        LogUtil.i("Updating $tasks")
        val taskResponse =
            TodoPerfectNetwork.updateTasks(TaskRequest(TaskRequestBodyJSON(tasks))).body
        if (taskResponse.statusCode == 200) {
            return taskResponse.body
        } else {
            LogUtil.e(taskResponse.body)
            throw RuntimeException(taskResponse.body)
        }
    }

    suspend fun deleteTasks(tasks: List<Task>): String {
        LogUtil.i("Deleting $tasks")
        val taskResponse =
            TodoPerfectNetwork.deleteTasks(TaskRequest(TaskRequestBodyJSON(tasks))).body
        if (taskResponse.statusCode == 200) {
            return taskResponse.body
        } else {
            LogUtil.e(taskResponse.body)
            throw RuntimeException(taskResponse.body)
        }
    }
}