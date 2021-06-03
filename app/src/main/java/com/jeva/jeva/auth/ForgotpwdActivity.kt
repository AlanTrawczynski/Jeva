package com.jeva.jeva.auth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jeva.jeva.R
import kotlinx.android.synthetic.main.activity_forgotpwd.*

class ForgotpwdActivity : AppCompatActivity() {

    private val auth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpwd)

        setup()
    }


    private fun setup() {
        forgotpwdLinkGoToLogin.setOnClickListener { finish() }

        forgotpwdBtnSubmit.setOnClickListener {
            val email = forgotpwdEmail.text.toString()
            if (AuthUtils.isValidEmail(email)) {
                resetPassword(email)
            }
            else {
                AuthUtils.authToast(R.string.not_valid_email, applicationContext)
            }
        }
    }


    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                AuthUtils.authToast(R.string.password_reset_sent, applicationContext)
                finish()
            }
            else {
                try {
                    throw task.exception!!
                }
                catch (_: FirebaseAuthInvalidUserException) {
                    AuthUtils.authToast(R.string.email_not_registered, applicationContext)
                }
                catch (e: Exception) {
                    AuthUtils.authToast(R.string.error_occurred, applicationContext)
                }
            }
        }
    }

}