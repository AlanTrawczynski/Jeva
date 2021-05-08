package com.jeva.jeva.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jeva.jeva.Database
import com.jeva.jeva.FragmentsActivity
import com.jeva.jeva.R
import com.jeva.jeva.home.HomeActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val auth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Database().isUserLoggedIn()) {
            startActivity(Intent(this, FragmentsActivity::class.java))
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

            if (!Auth.isValidEmail(email)) {
                Log.e("loginError", "Email no válido")
                Auth.authToast("Introduce un email válido", applicationContext)
            }
            else if (!Auth.isValidPassword(pwd)) {
                Log.e("loginError", "Contraseña no válida")
                Auth.authToast("Introduce una contraseña válida", applicationContext)
            }
            else {
                logIn(email, pwd)
            }
        }
    }


    private fun logIn(email: String, pwd: String) {
        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, FragmentsActivity::class.java))
            }
            else {
                try {
                    throw task.exception!!
                }
                catch (_: FirebaseAuthInvalidUserException) {
                    Log.e("loginError", "Usuario no registrado o deshabilitado")
                    Auth.authToast("El email no se encuentra registrado o ha sido deshabilitado", applicationContext)
                }
                catch (_: FirebaseAuthInvalidCredentialsException) {
                    Log.e("loginError", "Contraseña incorrecta")
                    Auth.authToast("Contraseña incorrecta", applicationContext)
                }
                catch (e: Exception) {
                    Log.e("loginError", "Se ha producido el error: $e")
                    Auth.authToast("Ha ocurrido un error, inténtelo de nuevo", applicationContext)
                }
            }
        }
    }


    override fun onStop() {
        loginPassword.text.clear()
        super.onStop()
    }


}