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
            if (Auth.isValidEmail(email)) {
                resetPassword(email)
            }
            else {
                Log.e("forgotpwdError", "Email no válido")
                Auth.authToast("Introduce un email válido", applicationContext)
            }
        }
    }


    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("forgotPwd", "Email sent.")
                Auth.authToast("Email de recuperación enviado", applicationContext)
                finish()
            }
            else {
                try {
                    throw task.exception!!
                }
                catch (_: FirebaseAuthInvalidUserException) {
                    Log.d("forgotpwdError", "Email no registrado")
                    Auth.authToast("El email no se encuentra registrado", applicationContext)
                }
                catch (e: Exception) {
                    Log.d("forgotpwdError", "Se ha producido un error: $e")
                    Auth.authToast("Ha ocurrido un error, inténtelo de nuevo", applicationContext)
                }
            }
        }
    }

}