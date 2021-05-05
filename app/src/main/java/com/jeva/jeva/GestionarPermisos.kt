package com.jeva.jeva

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale


class GestionarPermisos {

    companion object {
        @JvmStatic //si se da el caso de que se use la clase en Java la interpretará como estática.
        val REQUEST_PERMISSIONS_OK = 0
        val FINE_LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION

        fun requestLocationPermissions(contexto: Activity) {
            //Si el permiso de ubicación no está habilitado
            if (!fineLocationIsGranted(contexto)) {
                //Nos dice si al usuario se le debe de mostrar una ui que explique el por qué es necesario el permiso
                if(shouldShowRequestPermissionRationale(contexto, FINE_LOCATION_PERMISSION)) {
                    // recibe un título y una explicación. Tras presionar vale, llama a la función que se le pasa como parámetro.
                    cuadroExplicativo("Acepte los permisos","Empleamos la ubicación para ofrecerle rutas cercanas", contexto, Companion::askPermission)
                } else {
                    askPermission(contexto)
                }
            }
        }

        private fun cuadroExplicativo(titulo: String, mensaje: String, contexto: Activity, funcionAlAceptar: (cont: Activity) -> Unit) {
            var dialogo = AlertDialog.Builder(contexto)
            dialogo.setTitle(titulo)
            dialogo.setMessage(mensaje)
            dialogo.setPositiveButton("Vale") { dialog, which -> funcionAlAceptar(contexto)}
            dialogo.show()
        }
        private fun askPermission(contexto: Activity) {
            val permisos = arrayOf(FINE_LOCATION_PERMISSION)
            ActivityCompat.requestPermissions(contexto, permisos, REQUEST_PERMISSIONS_OK)
        }

        fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
            return grantResults.all { elem -> elem == PackageManager.PERMISSION_GRANTED }
        }

        fun fineLocationIsGranted(contexto:Activity): Boolean {
            return ActivityCompat.checkSelfPermission(contexto, FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }
    }
}