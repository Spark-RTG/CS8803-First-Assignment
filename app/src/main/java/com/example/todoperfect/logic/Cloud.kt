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
}