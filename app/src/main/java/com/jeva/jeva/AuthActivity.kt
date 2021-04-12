package com.jeva.jeva

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth_login.*
import kotlinx.android.synthetic.main.activity_auth_signup.*
import java.util.regex.Pattern


class AuthActivity : AppCompatActivity() {

    private val auth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_login)

        setup()
    }


    private fun setup() {
        loginBtnGoToSignup.setOnClickListener { setContentView(R.layout.activity_auth_signup) }
        loginLinkForgotPassword.setOnClickListener { setContentView(R.layout.activity_auth_forgotpassword) }
        signupLinkGoToLogin.setOnClickListener { setContentView(R.layout.activity_auth_login) }
        forgotpwdLinkGoToLogin.setOnClickListener { setContentView(R.layout.activity_auth_login) }

        loginBtnLogin.setOnClickListener {
            val email = loginEmail.text.toString()
            val pwd = loginPassword.text.toString()

            if (isValidEmail(email)) {
                logIn(email, pwd)
            }
            else {
                Log.d("loginError", "Email no válido")
                authToast("Introduce un email válido")
            }
        }

        loginBtnGoToSignup.setOnClickListener {
            val name = signupName.text.toString()
            val username = signupUsername.text.toString()
            val email = signupEmail.text.toString()
            val pwd1 = signupPassword.text.toString()
            val pwd2 = signupPasswordRepeat.text.toString()

            if (!isValidEmail(email)) {
                Log.d("signupError", "Email no válido")
                authToast("Introduce un email válido")
            }
            else if (!isValidPassword(pwd1)) {
                Log.d("signupError", "Contraseña no válida")
                authToast("Introduce una contraseña válida: 6 o más caracteres con una letra mayúscula, una minúscula y un número")
            }
            else if (pwd1 != pwd2) {
                Log.d("signupError", "Las contraseñas no coinciden")
                authToast("Las contraseñas no coinciden")
            }
            else {
                // creo que el resto de datos de la cuenta no pueden asignarse en la creación de la cuenta
                // y hay que actualizarla a posteriori
                signUp(email, pwd1, username, name)
            }
        }
    }


    private fun logIn(email: String, pwd: String) {
        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = auth.currentUser
                Log.i("User data", user!!.toString())
                // go to home activity
            }
            else {
                try {
                    throw it.exception!!
                }
                catch (_: FirebaseAuthInvalidUserException) {
                    Log.d("loginError", "Usuario no registrado o deshabilitado")
                    authToast("El email no se encuentra registrado o ha sido deshabilitado")
                }
                catch (_: FirebaseAuthInvalidCredentialsException) {
                    Log.d("loginError", "Contraseña incorrecta")
                    authToast("Contraseña incorrecta")
                }
                catch (e: Exception) {
                    Log.d("loginError", "Se ha producido el error: $e")
                    authToast("Ha ocurrido un error, inténtelo de nuevo")
                }
            }
        }
    }


    private fun signUp(email: String, pwd: String, username: String, name: String) {
        auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener {
            if (it.isSuccessful) {
                // Aquí crearemos lo que sea que vayamos a meter en la DB
            }
            else {
                try {
                    throw it.exception!!
                }
                catch (_: FirebaseAuthUserCollisionException) {
                    Log.d("signupError", "Email en uso")
                    authToast("El email ya se encuentra en uso")
                }
                catch (e: Exception) {
                    Log.d("signupError", "Se ha producido un error: $e")
                    authToast("Ha ocurrido un error, inténtelo de nuevo")
                }
            }
        }
    }


    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    private fun isValidPassword(pwd: String): Boolean {
        return Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$").matcher(pwd).matches();
    }


    private fun authToast(message: CharSequence) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

}