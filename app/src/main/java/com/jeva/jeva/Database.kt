package com.jeva.jeva

import android.net.Uri
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class Database {

    private val auth = Firebase.auth
    private val fs = Firebase.firestore
    private val cs = Firebase.storage.reference



//    Auth
    fun isUserLoggedIn() : Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserUid() : String {
        return auth.currentUser?.uid ?: throw Exception("Sesión no iniciada")
        // sign-out & reload ?
    }


//    Users
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


    fun getUserProfilePicRef(uid: String) : StorageReference {
        return cs.child("profilePics/${uid}")
    }

    fun getCurrentUserProfilePicRef() : StorageReference {
        return getUserProfilePicRef(getCurrentUserUid())
    }

    fun changeProfilePic(path: String, callback: (Boolean) -> Unit) {
        cs.child("profilePics/${getCurrentUserUid()}").putFile(Uri.parse(path))
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }



//    Routes
    fun getAllRoutes(callback: (List<Map<String, Any>>?) -> Unit) {
        fs.collection("routes").get()
            .addOnSuccessListener { docs ->
                callback(docs.map { it.data })
            }
            .addOnFailureListener { callback(null) }
    }

    fun getNearbyRoutes(bounds: LatLngBounds, callback: (List<Map<String, Any>>?) -> Unit) {
        fun filterByLat(route: Map<String, Any>) : Boolean {
            val lat = (route["position"] as Map<*, *>)["lat"] as Double
            return lat >= bounds.southwest.latitude && lat <= bounds.northeast.latitude
        }

        if (bounds.southwest.longitude < bounds.northeast.longitude) {
            fs.collection("routes")
                .whereGreaterThanOrEqualTo("position.lng", bounds.southwest.longitude)
                .whereLessThanOrEqualTo("position.lng", bounds.northeast.longitude)
                .get()
                .addOnSuccessListener { docs ->
                    callback(docs
                        .filter { doc -> filterByLat(doc.data) }
                        .map { doc -> doc.data }
                    )
                }
                .addOnFailureListener { callback(null) }
        }
        else {
            fs.collection("routes")
                .whereGreaterThanOrEqualTo("position.lng", bounds.southwest.longitude)
                .get()
                .addOnSuccessListener { docs1 ->
                    fs.collection("routes")
                        .whereLessThanOrEqualTo("position.lng", bounds.northeast.longitude)
                        .get()
                        .addOnSuccessListener { docs2 ->
                            callback((docs1 + docs2)
                                .filter { doc -> filterByLat(doc.data) }
                                .map { doc -> doc.data }
                            )
                        }
                        .addOnFailureListener { callback(null) }
                }
                .addOnFailureListener { callback(null) }
        }
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


    fun newRoute(markers: List<Marker>, title: String = "", description: String = "", callback: (Boolean) -> Unit) {
        val userRef = fs.collection("users").document(getCurrentUserUid())
        val routeRef = fs.collection("routes").document()
        val data = mapOf(
            "title" to title,
            "description" to description,
            "position" to mapOf(
                "lat" to markers[0].position.latitude,
                "lng" to markers[0].position.longitude),
            "markers" to markers.map { markerToMap(it) }
        )

        fs.runBatch { batch ->
            batch.update(userRef, "routes", FieldValue.arrayUnion(routeRef.id))
            batch.set(routeRef, data)
        }   .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    private fun markerToMap(marker: Marker) : Map<String, Any> {
        return mapOf(
            "lat" to marker.position.latitude,
            "lng" to marker.position.longitude,
            "tag" to (marker.tag ?: emptyMap<String, Any>())
        )
    }


}
