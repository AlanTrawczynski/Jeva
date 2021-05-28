package com.jeva.jeva.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jeva.jeva.home.HomeActivity
import com.jeva.jeva.R
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        setup()
    }


    private fun setup() {
        signupLinkGoToLogin.setOnClickListener { finish() }

        signupBtnSubmit.setOnClickListener {
            val name = signupName.text.toString()
            val username = signupUsername.text.toString()
            val email = signupEmail.text.toString()
            val pwd1 = signupPassword.text.toString()
            val pwd2 = signupPasswordRepeat.text.toString()

            if (!AuthUtils.isValidName(name)) {
                Log.e("signupError", "Name no válido")
                AuthUtils.authToast("El nombre debe de tener al menos 4 caracteres", applicationContext)
            }
            else if (!AuthUtils.isValidUsername(username)) {
                Log.e("signupError", "Username no válido")
                AuthUtils.authToast("El nombre de usuario debe de tener al menos 4 caracteres", applicationContext)
            }
            else if (!AuthUtils.isValidEmail(email)) {
                Log.e("signupError", "Email no válido")
                AuthUtils.authToast("Introduce un email válido", applicationContext)
            }
            else if (!AuthUtils.isValidPassword(pwd1)) {
                Log.e("signupError", "Contraseña no válida")
                AuthUtils.authToast("La contraseña debe de tener al menos 6 caracteres", applicationContext)
            }
            else if (pwd1 != pwd2) {
                Log.e("signupError", "Las contraseñas no coinciden")
                AuthUtils.authToast("Las contraseñas no coinciden", applicationContext)
            }
            else {
                signUp(email, pwd1, username, name)
            }
        }
    }


    private fun signUp(email: String, pwd: String, username: String, name: String) {
        auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = hashMapOf(
                    "name"      to name,
                    "username"  to username,
                )

                db.collection("users").document(auth.currentUser!!.uid)
                    .set(user)
                    .addOnSuccessListener {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        e -> Log.e("signupError", "Error writing document", e)
                        AuthUtils.authToast("Error al crear el documento de usuario", applicationContext)
                    }
            }
            else {
                try {
                    throw task.exception!!
                }
                catch (_: FirebaseAuthUserCollisionException) {
                    Log.d("signupError", "Email en uso")
                    AuthUtils.authToast("El email ya se encuentra en uso", applicationContext)
                }
                catch (e: Exception) {
                    Log.d("signupError", "Se ha producido un error: $e")
                    AuthUtils.authToast("Ha ocurrido un error, inténtelo de nuevo", applicationContext)
                }
            }
        }
    }


}