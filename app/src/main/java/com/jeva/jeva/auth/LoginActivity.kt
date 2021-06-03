package com.jeva.jeva.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jeva.jeva.R
import com.jeva.jeva.database.Database
import com.jeva.jeva.home.HomeActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val auth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Database().isUserLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        setup()
    }


    private fun setup() {
        loginBtnGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        loginLinkForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotpwdActivity::class.java))
        }

        loginBtnSubmit.setOnClickListener {
            val email = loginEmail.text.toString()
            val pwd = loginPassword.text.toString()

            if (!AuthUtils.isValidEmail(email)) {
                AuthUtils.authToast(R.string.not_valid_email, applicationContext)
            }
            else if (!AuthUtils.isValidPassword(pwd)) {
                AuthUtils.authToast(R.string.not_valid_password, applicationContext)
            }
            else {
                logIn(email, pwd)
            }
        }
    }


    private fun logIn(email: String, pwd: String) {
        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            else {
                try {
                    throw task.exception!!
                }
                catch (_: FirebaseAuthInvalidUserException) {
                    AuthUtils.authToast(R.string.email_not_registered, applicationContext)
                }
                catch (_: FirebaseAuthInvalidCredentialsException) {
                    AuthUtils.authToast(R.string.incorrect_password, applicationContext)
                }
                catch (e: Exception) {
                    AuthUtils.authToast(R.string.error_occurred, applicationContext)
                }
            }
        }
    }


    override fun onStop() {
        loginPassword.text.clear()
        super.onStop()
    }


}