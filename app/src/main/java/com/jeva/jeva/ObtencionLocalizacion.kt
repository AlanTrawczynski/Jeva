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

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun localizacion(actividad: Activity): Task<Location> {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(actividad)
        return getLastKnownLocation()
    }

    @SuppressLint("MissingPermission") //ya compruebo anteriormente que se hayan permitido los permisos (llamando a gestionarPermisos)
    private fun getLastKnownLocation(): Task<Location> {
        val servicioLoc = fusedLocationClient.lastLocation
        return servicioLoc
    }

}