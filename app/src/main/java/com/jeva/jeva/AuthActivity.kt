package com.jeva.jeva

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_auth.*
import sun.rmi.runtime.Log


class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        setup()
    }


    private fun setup() {
        auth = Firebase.auth

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


    private fun logIn(email: String, pwd: String){

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->

            if (task.isSuccessful) {

                val user = auth.currentUser

            } else {

                try {
                    // Lanzamos error si se produce en la tarea petición como tal
                    throw task.getException()

                }

                // El usuario introduce un email que no existe
                catch (invalidEmail: FirebaseAuthInvalidUserException) {
                    Log.d(TAG, "El email introducido no es correcto")

                }

                // El usuario introduce una contraseña errónea
                catch (wrongPassword: FirebaseAuthInvalidCredentialsException) {
                    Log.d(TAG, "La contraseña introducida no es correcta")

                }

                // Capturamos cualquier otro error que pueda suceder
                catch (e: Exception) {
                    Log.d(TAG, "Se ha producido el error: " + e.message)
                }

            }

        }

    }



    private fun signIn(email: String, pwd1: String, pwd2: String){
        if ( ! pwd1.equals(pwd2) ) {
            Log.e(WARN, "La contraseña está mal repetida")
        }

        else {

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){ task ->

                if (task.isSuccessful()) {

                    // Aquí crearemos lo que sea que vayamos a meter en la DB

                }else{
                    try {
                        // Si sucede un error en la tarea de petición
                        throw task.getException()

                    }

                    // Si la contraseña introducida es demasiado débil
                    catch (weakPassword: FirebaseAuthWeakPasswordException) {

                        Log.d(TAG, "La contraseña introducida es demasiado débil")

                    }

                    // Si la dirección de email está mal escrita
                    catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {

                        Log.d(TAG, "La dirección de email está mal escrita")

                    }

                    // Si el email que se pretende usar ya tiene una cuenta asociada
                    catch (existEmail: FirebaseAuthUserCollisionException) {

                        Log.d(TAG, "El email introducido ya tiene una cuenta asociada")

                    }

                    // Cualquier otro tipo de error
                    catch (e: Exception) {

                        Log.d(TAG, "Se ha producido el error: " + e.message)

                    }
                }

            }

        }
    }


}