package com.jeva.jeva

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

/*
CLASE no pensada para obtener la ubicación en tiempo real.
Retorna un Task<Location>
 */
class ObtencionLocalizacion {

    companion object {
        private lateinit var fusedLocationClient: FusedLocationProviderClient

        fun localizacion(actividad: Activity): Task<Location> {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(actividad)
            return getLastKnownLocation()
        }

        @SuppressLint("MissingPermission") //ya compruebo anteriormente que se hayan permitido los permisos (llamando a gestionarPermisos)
        private fun getLastKnownLocation(): Task<Location> {
            var servicioLoc = fusedLocationClient.lastLocation
            return servicioLoc
        }
    }

    /* EJEMPLO DE USO
           ObtencionLocalizacion.localizacion(this@MapActivity)
            .addOnSuccessListener {
                latlng = LatLng(it.latitude,it.longitude)
            }
            .addOnCompleteListener {
                //cuando se obtenga la localización se representa el mapa. NO antes.
                val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
     */
}