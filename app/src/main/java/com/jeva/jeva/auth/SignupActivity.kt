package com.jeva.jeva.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
                AuthUtils.authToast("Introduce un email válido", applicationContext)
            }
            else if (!AuthUtils.isValidPassword(pwd1)) {
                AuthUtils.authToast("La contraseña debe de tener al menos 6 caracteres", applicationContext)
            }
            else if (pwd1 != pwd2) {
                AuthUtils.authToast("Las contraseñas no coinciden", applicationContext)
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
                }
                else {
                    try {
                        throw task.exception!!
                    }
                    catch (_: FirebaseAuthUserCollisionException) {
                        AuthUtils.authToast("El email ya se encuentra en uso", applicationContext)
                    }
                }
            }
            .addOnFailureListener {
                AuthUtils.authToast("Ha ocurrido un error, inténtelo de nuevo", applicationContext)
            }
    }

}