package com.jeva.jeva

import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplicaciondepruebas.GestionarPermisos
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth_forgotpassword.*
import kotlinx.android.synthetic.main.activity_auth_login.*
import kotlinx.android.synthetic.main.activity_auth_signup.*
import java.util.regex.Pattern


class AuthActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private enum class LayoutType { LOGIN, SIGNUP, FORGOTPWD}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_login)

        setupLogin()
        setupSignup()
        setupForgotpassword()

        showLayout(LayoutType.LOGIN)
    }


    private fun showLayout(layout: LayoutType) {
        loginLayout.visibility = GONE
        signupLayout.visibility = GONE
        forgotpwdLayout.visibility = GONE

        when (layout){
            LayoutType.LOGIN -> loginLayout.visibility = VISIBLE
            LayoutType.SIGNUP -> signupLayout.visibility = VISIBLE
            LayoutType.FORGOTPWD -> forgotpwdLayout.visibility = VISIBLE
        }
    }


    private fun setupLogin() {
        loginBtnGoToSignup.setOnClickListener { showLayout(LayoutType.SIGNUP) }
        loginLinkForgotPassword.setOnClickListener { showLayout(LayoutType.FORGOTPWD) }

        loginBtnSubmit.setOnClickListener {
            val email = loginEmail.text.toString()
            val pwd = loginPassword.text.toString()

            if (!isValidEmail(email)) {
                Log.e("loginError", "Email no válido")
                authToast("Introduce un email válido")
            }
            else if (!isValidPassword(pwd)) {
                Log.e("loginError", "Contraseña no válida")
                authToast("Introduce una contraseña válida")
            }
            else {
                logIn(email, pwd)
            }
        }
    }


    private fun setupSignup() {
        signupLinkGoToLogin.setOnClickListener { showLayout(LayoutType.LOGIN) }

        signupBtnSubmit.setOnClickListener {
            val name = signupName.text.toString()
            val username = signupUsername.text.toString()
            val email = signupEmail.text.toString()
            val pwd1 = signupPassword.text.toString()
            val pwd2 = signupPasswordRepeat.text.toString()

            if (!isValidEmail(email)) {
                Log.e("signupError", "Email no válido")
                authToast("Introduce un email válido")
            }
            else if (!isValidPassword(pwd1)) {
                Log.e("signupError", "Contraseña no válida")
                authToast("Introduce una contraseña válida: 6 o más caracteres con una letra mayúscula, una minúscula y un número")
            }
            else if (pwd1 != pwd2) {
                Log.e("signupError", "Las contraseñas no coinciden")
                authToast("Las contraseñas no coinciden")
            }
            else {
                signUp(email, pwd1, username, name)
            }
        }
    }


    private fun setupForgotpassword() {
        forgotpwdLinkGoToLogin.setOnClickListener { showLayout(LayoutType.LOGIN) }
        forgotpwdBtnSubmit.setOnClickListener { forgotPwd() }
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
                    Log.e("loginError", "Usuario no registrado o deshabilitado")
                    authToast("El email no se encuentra registrado o ha sido deshabilitado")
                }
                catch (_: FirebaseAuthInvalidCredentialsException) {
                    Log.e("loginError", "Contraseña incorrecta")
                    authToast("Contraseña incorrecta")
                }
                catch (e: Exception) {
                    Log.e("loginError", "Se ha producido el error: $e")
                    authToast("Ha ocurrido un error, inténtelo de nuevo")
                }
            }
        }
    }


    private fun signUp(email: String, pwd: String, username: String, name: String) {
        auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener {
            if (it.isSuccessful) {
                val city = hashMapOf(
                    "name" to name,
                    "username" to username
                )

                db.collection("users").document(auth.currentUser.uid)
                    .set(city)
                    .addOnSuccessListener {
                        // Go to home activity
                        authToast("Usuario añadido a la base de datos")
                    }
                    .addOnFailureListener {
                        e -> Log.e("signupError", "Error writing document", e)
                        authToast("Error al crear el documento de usuario")
                    }
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


    private fun forgotPwd() {
        val email = forgotpwdEmail.text.toString()

        if (isValidEmail(email)) {
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("forgotPwd", "Email sent.")
                    authToast("Se ha enviado el email")
                }
                else {
                    try {
                        throw task.exception!!
                    }
                    catch (_: FirebaseAuthInvalidUserException) {
                        Log.d("forgotpwdError", "Email no registrado")
                        authToast("El email no se encuentra registrado")
                    }
                    catch (e: Exception) {
                        Log.d("forgotpwdError", "Se ha producido un error: $e")
                        authToast("Ha ocurrido un error, inténtelo de nuevo")
                    }
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