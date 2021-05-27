package com.jeva.jeva.images

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.jeva.jeva.Database
import com.jeva.jeva.R

class routesPopUp(title: String, description: String, routeId: String, activity: Activity, context: Context, layoutInflater: LayoutInflater) {

    var title:String = title
    var description:String = description
    val routeId: String = routeId
    val activity: Activity = activity
    val context: Context = context
    val layoutInflater: LayoutInflater = layoutInflater

    lateinit var dialogBuilder: AlertDialog.Builder
    lateinit var popUp: View

    private var db = Database()

    val REQUEST_CODE = 1

    fun show(editable:Boolean) {
        dialogBuilder = AlertDialog.Builder(dataPointMenu.activity)
        val popUp : View = layoutInflater.inflate(R.layout.popup_route,null)

        //añadimos nombre y descripción
        var rutaname: EditText = popUp.findViewById(R.id.routeName)
        var rutadescripcion: EditText = popUp.findViewById(R.id.routeDescription)
        (rutaname as TextView).text = title
        (rutadescripcion as TextView).text = description

        editable(rutaname, editable)
        editable(rutadescripcion, editable)

        //creamos el cuadro de diálogo y añadimos listener al boton
        dialogBuilder.setView(popUp)
        var dialog = dialogBuilder.create()

        var cerrar: Button = popUp.findViewById(R.id.cerrar)
        cerrar.setOnClickListener {
            //AQUÍ IRIA LO DE ACTUALIZAR NOMBRE Y DESCRIPCIÓN
            dialog.dismiss()
        }

        //iconview
        var imagen : Icon = popUp.findViewById(R.id.icon2)
        loadRouteImageFromDB(routeId,imagen)
    }

    private fun loadRouteImageFromDB(routeId: String, imagen: Icon) {
        //
    }

    //AUXILIAR
    private fun editable(cuadroTexto: EditText, editable: Boolean) {
        cuadroTexto.setFocusable(editable)
        cuadroTexto.setClickable(editable)
        cuadroTexto.setFocusableInTouchMode(editable)
        cuadroTexto.setLongClickable(editable)
    }
}