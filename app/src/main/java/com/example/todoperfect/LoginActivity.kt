package com.example.todoperfect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.todoperfect.logic.SharedPreference
import com.example.todoperfect.logic.model.User
import com.example.todoperfect.ui.login.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    companion object {
        const val INFO_VALID = 0
        const val EMAIL_INVALID = 1
        const val PASSWORD_INVALID = 2
        const val PASSWORD_MISMATCH = 3
        const val VERIFY_FAILED = 4
        const val LOGIN_FAILED = 5
        const val INTERNET_ISSUE = 6
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar()
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        checkLocal()
        setUpEdits()
        logInRegister.setOnClickListener {
            if (viewModel.login) {
                val err = checkValidity(viewModel.email.toString())
                if (err != INFO_VALID) {
                    errMessage(err)
                } else {
                    login(viewModel.email.toString(),
                        viewModel.password.toString())
                }
            } else {
                val err = checkValidity(viewModel.email.toString(),
                    viewModel.password.toString(),
                    viewModel.confirmPassword.toString())
                if (err != INFO_VALID) {
                    errMessage(err)
                } else {
                    verifyCode(
                        viewModel.email.toString(),
                        viewModel.password.toString(),
                        viewModel.verificationCode.toString()
                    )
                }
            }
        }
        changeLogInBehavior.setOnClickListener {
            viewModel.login = !viewModel.login
            refresh()
        }
        verifyBtn.setOnClickListener {
            val err = checkValidity(viewModel.email.toString(),
                viewModel.password.toString(),
                viewModel.confirmPassword.toString())
            if (err != INFO_VALID) {
                errMessage(err)
            } else {
                signUpWithoutVerification()
            }
        }
        refresh()
    }

    private fun checkLocal() {
        viewModel.loadUser()
        viewModel.userLiveData.observe(this) { result ->
            val user = result.getOrNull()
            if (user != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            } else {
                coverLayout.visibility = View.GONE
            }
        }
    }

    private fun setStatusBar() {
        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
    }
    private fun login(email: String, password: String) {
        viewModel.userLogin()
        viewModel.userLoginLiveData.observe(this) { result ->
            val userResponseData = result.getOrNull()
            if (userResponseData != null) {
                if (userResponseData.result) {
                    loginRegisterSuccess(email)
                } else {
                    Snackbar.make(logInRegister, userResponseData.message,
                        Snackbar.LENGTH_SHORT).show()
                }
            } else {
                errMessage(INTERNET_ISSUE)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.login = true
        verifyBtn.isClickable = true
        refresh()
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private fun signUpWithoutVerification() {
        viewModel.userSignUp()
        viewModel.userSignUpLiveData.observe(this) { result ->
            val userResponseData = result.getOrNull()
            if (userResponseData != null) {
                if (userResponseData.result) {
                    verifyBtn.isClickable = false
                    verifyBtn.setTextColor(resources.getColor(R.color.white))
                    verifyBtn.backgroundTintList = resources.getColorStateList(R.color.disable_button)
                    hideSoftInput()
                    Snackbar.make(logInRegister, "Please check your email for verification!",
                        Snackbar.LENGTH_SHORT).show()
                } else {
                    hideSoftInput()
                    Snackbar.make(logInRegister, userResponseData.message,
                        Snackbar.LENGTH_SHORT).show()
                }
            } else {
                errMessage(INTERNET_ISSUE)
            }
        }
    }
    private fun verifyCode(email: String, password: String, verificationCode: String) {
        viewModel.userVerify()
        viewModel.verifyLiveData.observe(this) { result ->
            val verifyResponseData = result.getOrNull()
            if (verifyResponseData != null) {
                if (verifyResponseData.result) {
                    loginRegisterSuccess(viewModel.email.toString())
                } else {
                    hideSoftInput()
                    Snackbar.make(logInRegister, verifyResponseData.message,
                        Snackbar.LENGTH_SHORT).show()
                }
            } else {
                errMessage(INTERNET_ISSUE)
            }
        }
        loginRegisterSuccess(email)
    }

    private fun loginRegisterSuccess(email: String) {
        val user = User(email)
        SharedPreference.saveUser(user)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun setUpEdits() {
        emailEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.email = s
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        passwordEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.password = s
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        confirmPasswordEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.confirmPassword = s
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        verificationCodeEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.verificationCode = s
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun hideSoftInput() {
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(loginLayout.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun errMessage(err: Int) {
        hideSoftInput()
        when (err) {
            EMAIL_INVALID -> {
                Snackbar.make(logInRegister, "Please enter a valid email!",
                    Snackbar.LENGTH_SHORT).show()
            }
            PASSWORD_INVALID -> {
                Toast.makeText(this, "Passwords should contain " +
                        "at least 8 characters, " +
                        "at least 1 digit, " +
                        "at least 1 uppercase letter, " +
                        "at least 1 lowercase letter, " +
                        "at least 1 special character, " +
                        "and no white spaces", Toast.LENGTH_LONG).show()
            }
            PASSWORD_MISMATCH -> {
                Snackbar.make(logInRegister, "Passwords don't match!",
                    Snackbar.LENGTH_SHORT).show()
            }
            VERIFY_FAILED -> {
                Snackbar.make(logInRegister, "Please check your verification code!",
                    Snackbar.LENGTH_SHORT).show()
            }
            LOGIN_FAILED -> {
                Snackbar.make(logInRegister, "Please check your email and password!",
                    Snackbar.LENGTH_SHORT).show()
            }
            INTERNET_ISSUE -> {
                Snackbar.make(logInRegister, "Please check your internet!",
                    Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkValidity(email: String): Int {
        val emailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return if (emailValid) {
            INFO_VALID
        } else {
            EMAIL_INVALID
        }
    }

    private fun checkValidity(email: String, password: String, confirm: String): Int {
        val emailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val passwordMatch = password == confirm
        val passwordValid = isValidPasswordFormat(password)
        return if (emailValid && passwordValid && passwordMatch) {
            INFO_VALID
        } else if (!emailValid) {
            EMAIL_INVALID
        } else if (!passwordValid) {
            PASSWORD_INVALID
        } else {
            PASSWORD_MISMATCH
        }
    }

    fun isValidPasswordFormat(password: String): Boolean {
        val passwordREGEX = Pattern.compile("^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[\\-\\*@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{8,}" +               //at least 8 characters
                "$")
        return passwordREGEX.matcher(password).matches()
    }

    private fun refresh() {
        emailEdit.text = viewModel.email
        passwordEdit.text = viewModel.password
        confirmPasswordEdit.text = viewModel.confirmPassword
        verificationCodeEdit.text = viewModel.verificationCode
        val logInText = "Log In"
        val signUpText = "Sign Up"
        if (viewModel.login) {
            logInRegister.text = logInText
            changeLogInBehavior.text = signUpText
            signUpLayout.visibility = View.GONE
        } else {
            logInRegister.text = signUpText
            changeLogInBehavior.text = logInText
            signUpLayout.visibility = View.VISIBLE
        }
    }
}