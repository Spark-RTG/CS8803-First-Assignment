package com.example.todoperfect.ui.login

import android.text.Editable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.todoperfect.logic.Cloud
import com.example.todoperfect.logic.SharedPreference
import com.example.todoperfect.logic.model.*

class LoginViewModel : ViewModel() {
    private val localUser = MutableLiveData<User>()
    val userLiveData = Transformations.switchMap(localUser) {
        SharedPreference.loadUser()
    }

    private val userSignUpRequest = MutableLiveData<UserRequest>()
    val userSignUpLiveData = Transformations.switchMap(userSignUpRequest) { userRequest ->
        Cloud.userRegister(userRequest)
    }

    private val verifyRequest = MutableLiveData<VerifyRequest>()
    val verifyLiveData = Transformations.switchMap(verifyRequest) { verifyRequest ->
        Cloud.userVerify(verifyRequest)
    }

    private val userLoginRequest = MutableLiveData<UserRequest>()
    val userLoginLiveData = Transformations.switchMap(userLoginRequest) { userRequest ->
        Cloud.userLogin(userRequest)
    }

    var login = true
    var email : Editable
    var password : Editable
    var confirmPassword : Editable
    var verificationCode : Editable

    init {
        email = Editable.Factory().newEditable("")
        password = Editable.Factory().newEditable("")
        confirmPassword = Editable.Factory().newEditable("")
        verificationCode = Editable.Factory().newEditable("")
    }
    fun loadUser() {
        localUser.value = User("")
    }

    fun userSignUp() {
        userSignUpRequest.value = UserRequest(UserRequestBodyJSON(
            email.toString(), password.toString()))
    }

    fun userVerify() {
        verifyRequest.value = VerifyRequest(VerifyRequestBodyJSON(
            email.toString(), verificationCode.toString()))
    }

    fun userLogin() {
        userLoginRequest.value = UserRequest(UserRequestBodyJSON(
            email.toString(), password.toString()))
    }
}