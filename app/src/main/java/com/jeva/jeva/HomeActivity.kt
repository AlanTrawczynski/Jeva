package com.jeva.jeva

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        pruebaSignout.setOnClickListener {
            auth.signOut()
            finish()
        }

        auth.currentUser?.uid?.let {  uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener  {  doc ->
                    if (doc != null) {
                        val user = doc.data
                        pruebaText.text = "(uid, name, username) = (${uid}, ${user?.get("name")}, ${user?.get("username")})"
                    }   else {
                        pruebaText.text = "no user"
                    }
                }
                .addOnFailureListener {
                    pruebaText.text = "no user"
                }
        }
    }


    override fun onBackPressed() {
        moveTaskToBack(true)
    }


}