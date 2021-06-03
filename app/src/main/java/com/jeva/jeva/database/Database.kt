package com.jeva.jeva.database

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import id.zelory.compressor.Compressor
import java.io.File
import java.util.*

class Database {

    private val auth = Firebase.auth
    private val fs = Firebase.firestore
    private val cs = Firebase.storage.reference



//    AUTH
    fun isUserLoggedIn() : Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser() : FirebaseUser {
        assert(auth.currentUser != null)
        return auth.currentUser!!
    }


    fun getCurrentUserUid() : String {
        return getCurrentUser().uid
    }




//    USERS
//    Update user data
    enum class UpdateUserEmailStatus { SERVER_FAIL, INVALID_CREDENTIALS, ACCOUNT_DISABLED_OR_DELETED, EMAIL_MALFORMED, EMAIL_ALREADY_IN_USE, OK }
    fun updateUserEmail(newEmail: String, oldEmail: String, password: String, callback: (UpdateUserEmailStatus) -> Unit) {
        val user = getCurrentUser()

        user.reauthenticate(EmailAuthProvider.getCredential(oldEmail, password))
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updateEmail(newEmail)
                        .addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful) {
                                callback(UpdateUserEmailStatus.OK)
                            }
                            else {
                                try {
                                    throw emailTask.exception!!
                                }
                                catch (_: FirebaseAuthInvalidCredentialsException) {
                                    callback(UpdateUserEmailStatus.EMAIL_MALFORMED)
                                }
                                catch (_: FirebaseAuthUserCollisionException) {
                                    callback(UpdateUserEmailStatus.EMAIL_ALREADY_IN_USE)
                                }
                            }
                        }
                        .addOnFailureListener { callback(UpdateUserEmailStatus.SERVER_FAIL) }
                }
                else {
                    try {
                        throw reauthTask.exception!!
                    }
                    catch (_: FirebaseAuthInvalidCredentialsException) {
                        callback(UpdateUserEmailStatus.INVALID_CREDENTIALS)
                    }
                    catch (_: FirebaseAuthInvalidUserException) {
                        callback(UpdateUserEmailStatus.ACCOUNT_DISABLED_OR_DELETED)
                    }
                }
            }
            .addOnFailureListener { callback(UpdateUserEmailStatus.SERVER_FAIL) }
    }


    enum class UpdateUserPasswordStatus { SERVER_FAIL, INVALID_CREDENTIALS, ACCOUNT_DISABLED_OR_DELETED, PASSWORD_WEAK, OK }
    fun updateUserPassword(newPassword: String, email: String, oldPassword: String, callback: (UpdateUserPasswordStatus) -> Unit) {
        val user = getCurrentUser()

        user.reauthenticate(EmailAuthProvider.getCredential(email, oldPassword))
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { pwdTask ->
                            if (pwdTask.isSuccessful) {
                                callback(UpdateUserPasswordStatus.OK)
                            }
                            else {
                                try {
                                    throw pwdTask.exception!!
                                }
                                catch (_: FirebaseAuthWeakPasswordException) {
                                    callback(UpdateUserPasswordStatus.PASSWORD_WEAK)
                                }
                            }
                        }
                        .addOnFailureListener { callback(UpdateUserPasswordStatus.SERVER_FAIL) }
                }
                else {
                    try {
                        throw reauthTask.exception!!
                    }
                    catch (_: FirebaseAuthInvalidCredentialsException) {
                        callback(UpdateUserPasswordStatus.INVALID_CREDENTIALS)
                    }
                    catch (_: FirebaseAuthInvalidUserException) {
                        callback(UpdateUserPasswordStatus.ACCOUNT_DISABLED_OR_DELETED)
                    }
                }
            }
            .addOnFailureListener { callback(UpdateUserPasswordStatus.SERVER_FAIL) }
    }




//    ROUTES
//    Get routes
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
                        .map { qdsToRoute(it) }
                        .filter { filterByLat(it) }
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
                                .map { qdsToRoute(it) }
                                .filter { filterByLat(it) }
                            )
                        }
                        .addOnFailureListener { callback(null) }
                }
                .addOnFailureListener { callback(null) }
        }
    }


    fun getCurrentUserRoutes(callback: (List<Map<String, Any>>?) -> Unit) {
        fs.collection("routes")
            .whereEqualTo("owner", getCurrentUserUid())
            .get()
            .addOnSuccessListener { docs ->
                callback(docs.map { qdsToRoute(it) }
                )
            }
            .addOnFailureListener { callback(null) }
    }


    private fun qdsToRoute(doc: QueryDocumentSnapshot) : Map<String, Any> {
        val route = doc.data
        route["id"] = doc.id
        return route
    }


    fun getRoute(routeId: String, callback: (Map<String, Any>?) -> Unit) {
        fs.collection("routes").document(routeId)
            .get()
            .addOnSuccessListener {
                callback(it?.data?.let { route ->
                    route["id"] = routeId
                    return@let route
                })
            }
            .addOnFailureListener { callback(null) }
    }



//    Create and update routes
    fun newRoute(markers: List<Marker> = listOf(), title: String = "", description: String = "", callback: (String?) -> Unit) {
        val data = mapOf(
            "title" to title,
            "description" to description,
            "owner" to getCurrentUserUid(),
            "markers" to markers.map { markerToMap(it) }
        )

        fs.collection("routes").add(data)
            .addOnSuccessListener { callback(it?.id) }
            .addOnFailureListener { callback(null) }
    }


    fun updateRoute(routeId: String, markers: List<Marker>? = null, title: String? = null, description: String? = null, callback: (Boolean) -> Unit) {
        val data = mutableMapOf<String, Any>()
        title?.let { data["title"] = it }
        description?.let { data["description"] = it }
        markers?.let {
            data["markers"] = it.map { marker -> markerToMap(marker) }
            data["position"] = if (markers.isEmpty()) FieldValue.delete() else mapOf("lat" to markers[0].position.latitude, "lng" to markers[0].position.longitude)
        }

        fs.collection("routes").document(routeId).update(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }


    private fun markerToMap(marker: Marker): Map<String, Any> {
        assert(marker.tag != null)
        return mapOf(
            "lat" to marker.position.latitude,
            "lng" to marker.position.longitude,
            "tag" to marker.tag!!
        )
    }



//    Get marker and route photos
    fun getMarkerPhotosRefs(routeId: String, markerId: String, callback: (List<StorageReference>?) -> Unit) {
        cs.child("routes/${routeId}/${markerId}")
            .listAll()
            .addOnSuccessListener { callback(it?.items) }
            .addOnFailureListener { callback(null) }
    }


    fun getRoutePhotoRef(routeId: String) : StorageReference {
        return cs.child("routes/${routeId}/route-pic")
    }



//    Upload markers and route photos
    fun uploadMarkerPhoto(uri: Uri, routeId: String, markerId: String, context: Context, callback: (String?) -> Unit) {
        try {
            val img = compressPhoto(uri, context)

            cs.child("routes/${routeId}/${markerId}/${UUID.randomUUID()}")
                .putFile(img.toUri())
                .addOnSuccessListener {
                    callback(it.metadata?.name.toString()) }
                .addOnFailureListener { callback(null) }
        }
        catch (_: Exception) {
            callback(null)
        }
    }


    fun uploadRoutePhoto(uri: Uri, routeId: String, context: Context, callback: (String?) -> Unit) {
        try {
            val img = compressPhoto(uri, context)

            cs.child("routes/${routeId}/route-pic")
                .putFile(img.toUri())
                .addOnSuccessListener {
                    callback(it.metadata?.name.toString()) }
                .addOnFailureListener { callback(null) }
        }
        catch (_: Exception) {
            callback(null)
        }
    }


    private fun compressPhoto(uri: Uri, context: Context) : File {
        return Compressor(context)
                .setQuality(30)
                .compressToFile(FileUtil.from(context, uri))
    }



//    Delete routes and photos
    fun deleteRoute(routeId: String, callback: (Boolean) -> Unit) {
        deleteAllRoutePhotos(routeId) {
            if (it) {
                fs.collection("routes").document(routeId)
                    .delete()
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            }
            else {
                callback(false)
            }
        }
    }


    private fun deleteAllRoutePhotos(routeId: String, callback: (Boolean) -> Unit) {
        cs.child("routes/${routeId}").listAll()
            .addOnSuccessListener { folders ->      // list folders
                folders?.prefixes?.forEach { folderRef ->
                    folderRef.listAll()         // list photos
                        .addOnSuccessListener { photos ->
                            photos?.items?.forEach { photoRef ->
                                photoRef.delete()
                            }
                        }
                }
                callback(true)      // no se asegura la eliminación de todas las fotos
            }
            .addOnFailureListener { callback(false) }
    }


    fun deleteMarkerPhotos(routeId: String, markerId: String, callback: (Boolean) -> Unit) {
        cs.child("routes/${routeId}/${markerId}").listAll()
            .addOnSuccessListener { photos ->
                photos?.items?.forEach { photoRef ->
                    photoRef.delete()
                }
                callback(true)      // no se asegura la eliminación de todas las fotos
            }
            .addOnFailureListener { callback(false) }
    }


    fun deleteMarkerPhoto(routeId: String, markerId: String, photoId: String, callback: (Boolean) -> Unit) {
        cs.child("routes/${routeId}/${markerId}/${photoId}")
            .delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

}
