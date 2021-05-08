package com.jeva.jeva

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class Database {

    private val auth = Firebase.auth
    private val fs = Firebase.firestore
    private val cs = Firebase.storage



//    Auth
    fun isUserLoggedIn() : Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserUid() : String {
        return auth.currentUser?.uid ?: throw Exception("Sesión no iniciada")
    }


//    Firestore - Users
    fun getUserTask(uid: String) : Task<DocumentSnapshot> {
        return fs.collection("users").document(uid).get()
    }

    fun getUser(uid: String, callback: (Map<String, Any>?) -> Unit) {
        getUserTask(uid)
            .addOnSuccessListener { callback(it?.data) }
            .addOnFailureListener { callback(null) }
    }

    fun getCurrentUserTask() : Task<DocumentSnapshot> {
        return getUserTask(getCurrentUserUid())
    }

    fun getCurrentUser(callback: (Map<String, Any>?) -> Unit) {
        getUser(getCurrentUserUid(), callback)
    }


    fun updateUser(uid: String, data: Map<String, Any>, callback: (Boolean) -> Unit) {
        fs.collection("users").document(uid).update(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun updateCurrentUser(data: Map<String, Any>, callback: (Boolean) -> Unit) {
        updateUser(getCurrentUserUid(), data, callback)
    }



//    Firestore - Routes
    fun getAllRoutes(callback: (List<Map<String, Any>>?) -> Unit) {
        fs.collection("routes").get()
            .addOnSuccessListener { docs ->
                callback(docs.map { it.data })
            }
            .addOnFailureListener { callback(null) }
    }

    fun getRouteTask(routeId: String) : Task<DocumentSnapshot> {
        return fs.collection("routes").document(routeId).get()
    }

    fun getRoute(routeId: String, callback: (Map<String, Any>?) -> Unit) {
        getRouteTask(routeId)
            .addOnSuccessListener { callback(it?.data) }
            .addOnFailureListener { callback(null) }
    }


    fun updateRoute(routeId: String, data: Map<String, Any>, callback: (Boolean) -> Unit) {
        fs.collection("routes").document(routeId).update(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }


    fun newRoute(data: Map<String, Any>, callback: (Boolean) -> Unit) {
        val userRef = fs.collection("users").document(getCurrentUserUid())
        val routeRef = fs.collection("routes").document()

        fs.runBatch { batch ->
            batch.update(userRef, "routes", FieldValue.arrayUnion(routeRef.id))
            batch.set(routeRef, data)
        }   .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }


}
