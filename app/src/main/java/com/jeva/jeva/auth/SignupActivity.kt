package com.jeva.jeva.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jeva.jeva.R
import com.jeva.jeva.home.HomeActivity
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {

    private val auth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        setup()
    }


    private fun setup() {
        signupLinkGoToLogin.setOnClickListener { finish() }

        signupBtnSubmit.setOnClickListener {
            val email = signupEmail.text.toString()
            val pwd1 = signupPassword.text.toString()
            val pwd2 = signupPasswordRepeat.text.toString()

            if (!AuthUtils.isValidEmail(email)) {
                AuthUtils.authToast(R.string.not_valid_email, applicationContext)
            }
            else if (!AuthUtils.isValidPassword(pwd1)) {
                AuthUtils.authToast(R.string.not_valid_password, applicationContext)
            }
            else if (pwd1 != pwd2) {
                AuthUtils.authToast(R.string.passwords_dont_match, applicationContext)
            }
            else {
                signUp(email, pwd1)
            }
        }
    }


    private fun signUp(email: String, pwd: String) {
        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                else {
                    try {
                        throw task.exception!!
                    }
                    catch (_: FirebaseAuthUserCollisionException) {
                        AuthUtils.authToast(R.string.email_in_use, applicationContext)
                    }
                }
            }
            .addOnFailureListener {
                AuthUtils.authToast(R.string.error_occurred, applicationContext)
            }
    }

}