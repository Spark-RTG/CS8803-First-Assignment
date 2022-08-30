package com.example.todoperfect.logic.network

import com.example.todoperfect.logic.model.UserRequest
import com.example.todoperfect.logic.model.UserResponse
import com.example.todoperfect.logic.model.VerifyRequest
import com.example.todoperfect.logic.model.VerifyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {

    @POST("login")
    fun userLogin(@Body userRequest: UserRequest): Call<UserResponse>

    @POST("signup")
    fun userRegister(@Body userRequest: UserRequest): Call<UserResponse>

    @POST("confirm_email")
    fun userVerify(@Body verifyRequest: VerifyRequest): Call<VerifyResponse>
}