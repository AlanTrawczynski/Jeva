package com.jeva.jeva

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*

class PruebasActivity : AppCompatActivity() {

    private val auth = Firebase.auth
//    private val db = Firebase.firestore
    private val db = Database()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        pruebaSignout.setOnClickListener {
            auth.signOut()
            finish()
        }


        pruebaBtn.setOnClickListener {
            db.getCurrentUser { user ->
                if (user != null) {
                    db.updateRoute((user["routes"] as List<String>)[0], mapOf(
                        "title" to "prueba"
                    )) { success ->
                        pruebaText.text = if (success) "oki" else "not oki"
                    }
                }   else {
                    pruebaText.text = "tenemo problema"
                }
            }
        }





        /*
//        ----------------------------
        val uid = db.getCurrentUserUid()

        // no se diferencia entre null debido a un fallo de conexión o porque el doc no existe
        // se utiliza directamente el usuario en el callback
        db.getCurrentUser() { user ->
            if (user != null) {
                pruebaText.text =
                    "(uid, name, username) = (${uid}, ${user["name"]}, ${user["username"]})"
            } else {
                pruebaText.text = "null doc or error"
            }
        }

        // se devuelve referencia al documento -> control total de errores
        // mucho menos cómodo que el anterior
        db.getCurrentUserTask()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = doc.data!!
                    pruebaText2.text = "(uid, name, username) = (${uid}, ${user["name"]}, ${user["username"]})"
                }   else {
                    pruebaText2.text = "null doc"
                }
            }
            .addOnFailureListener {
                pruebaText2.text = "error"
            }
//        ----------------------------
        */

    }


    override fun onBackPressed() {
        moveTaskToBack(true)
    }


}