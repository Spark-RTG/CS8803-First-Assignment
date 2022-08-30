package com.example.todoperfect.logic.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserRequest(@SerializedName("body-json") val bodyJSON: UserRequestBodyJSON)
data class UserRequestBodyJSON(val email: String, val password: String)

data class UserResponse(@SerializedName("body-json") val body: UserResponseBody)
data class UserResponseBody(val statusCode: Int, val body: UserResponseData)
data class UserResponseData(val result: Boolean, val message: String,
                            @SerializedName("userdata")val userData: User)
data class User (var email: String) : Serializable

data class VerifyRequest(@SerializedName("body-json") val bodyJSON: VerifyRequestBodyJSON)
data class VerifyRequestBodyJSON(val email: String, val code: String)

data class VerifyResponse(@SerializedName("body-json") val body: VerifyResponseBody)
data class VerifyResponseBody(val statusCode: Int, val body: VerifyResponseData)
data class VerifyResponseData(val result: Boolean, val message: String)