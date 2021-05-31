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
        val READ_EXTERNAL_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE
        val WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

        fun requestLocationPermissions(contexto: Activity) {
            //Si el permiso de ubicación no está habilitado
            if (!fineLocationIsGranted(contexto)) {
                //Nos dice si al usuario se le debe de mostrar una ui que explique el por qué es necesario el permiso
                val permisos = arrayOf(FINE_LOCATION_PERMISSION)
                if(shouldShowRequestPermissionRationale(contexto, FINE_LOCATION_PERMISSION)) {
                    // recibe un título y una explicación. Tras presionar vale, llama a la función que se le pasa como parámetro.
                    cuadroExplicativo(contexto.getString(R.string.accept_permissions),contexto.getString(
                                            R.string.location_justification), contexto, permisos, ::askPermission)
                } else {
                    askPermission(contexto,permisos)
                }
            }
        }

        fun requestStoragePermissions(actividad:Activity) {
            if (!accessStorageIsGranted(actividad)) {
                //Nos dice si al usuario se le debe de mostrar una ui que explique el por qué es necesario el permiso
                val permisos = arrayOf(READ_EXTERNAL_STORAGE)
                if(shouldShowRequestPermissionRationale(actividad, READ_EXTERNAL_STORAGE)) {
                    // recibe un título y una explicación. Tras presionar vale, llama a la función que se le pasa como parámetro.
                    cuadroExplicativo(actividad.getString(R.string.accept_permissions),actividad.getString(
                                            R.string.gallery_justification), actividad, permisos,::askPermission)
                } else {
                    askPermission(actividad,permisos)
                }
            }
        }

        fun requestRWStoragePermissions(actividad:Activity) {
            if(!accessStorageIsGranted(actividad) || !writeStorageIsGranted(actividad)) {
                val permisos = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
                if(shouldShowRequestPermissionRationale(actividad, READ_EXTERNAL_STORAGE)
                    ||shouldShowRequestPermissionRationale(actividad, WRITE_EXTERNAL_STORAGE)) {
                    // recibe un título y una explicación. Tras presionar vale, llama a la función que se le pasa como parámetro.
                    cuadroExplicativo(actividad.getString(R.string.accept_permissions),actividad.getString(
                                            R.string.storage_language_justification),
                                            actividad, permisos,::askPermission)
                } else {
                    askPermission(actividad,permisos)
                }
            }
        }

        private fun cuadroExplicativo(titulo: String, mensaje: String, contexto: Activity, permisos:Array<String>, funcionAlAceptar: (cont: Activity,perm: Array<String>) -> Unit) {
            var dialogo = AlertDialog.Builder(contexto)
            dialogo.setTitle(titulo)
            dialogo.setMessage(mensaje)
            dialogo.setPositiveButton(contexto.getString(R.string.okay)) { dialog, which -> funcionAlAceptar(contexto,permisos)}
            dialogo.show()
        }

        private fun askPermission(contexto: Activity, permisos: Array<String>) {
            ActivityCompat.requestPermissions(contexto, permisos, REQUEST_PERMISSIONS_OK)
        }

        fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
            return grantResults.all { elem -> elem == PackageManager.PERMISSION_GRANTED }
        }

        fun fineLocationIsGranted(actividad:Activity): Boolean {
            return ActivityCompat.checkSelfPermission(actividad, FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }

        fun accessStorageIsGranted(actividad:Activity): Boolean {
            return ActivityCompat.checkSelfPermission(actividad, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        fun writeStorageIsGranted(actividad:Activity): Boolean {
            return ActivityCompat.checkSelfPermission(actividad, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
}