package com.jeva.jeva

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        setup()
    }


    private fun setup() {
        val auth = Firebase.auth

        loginButton.setOnClickListener {
            // try login -> init home activity
        }

        signupButton.setOnClickListener {
            if (!checkEmail()) {
                // email error
            }
            else if (!checkPassword()) {
                // pw error
            }
            else {
                // try signup
                // init home activity
            }
        }
    }


    private fun checkEmail(): Boolean {
        return true
    }


    private fun checkPassword(): Boolean {
        return true
    }

}