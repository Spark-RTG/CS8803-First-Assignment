package com.example.todoperfect.logic.network

import com.example.todoperfect.LogUtil
import com.example.todoperfect.logic.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object TodoPerfectNetwork {
    private val userService = ServiceCreator.create<UserService>()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object: Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null"))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    LogUtil.e("Failure: $t")
                    continuation.resumeWithException(t)
                }
            })
        }
    }

    suspend fun userLogin(userRequest: UserRequest) =
        userService.userLogin(userRequest).await()
    suspend fun userRegister(userRequest: UserRequest) =
        userService.userRegister(userRequest).await()
    suspend fun userVerify(verifyRequest: VerifyRequest) =
        userService.userVerify(verifyRequest).await()
}