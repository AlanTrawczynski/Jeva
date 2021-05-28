package com.jeva.jeva.images

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.jeva.jeva.Database
import com.jeva.jeva.R

// AÚN NO FUNCIONAL
class routesPopUp(title: String, description: String, routeId: String, activity: Activity, context: Context, layoutInflater: LayoutInflater) {

    var title:String = title
    var description:String = description
    val routeId: String = routeId
    val activity: Activity = activity
    val context: Context = context
    val layoutInflater: LayoutInflater = layoutInflater

    lateinit var dialogBuilder: AlertDialog.Builder
    lateinit var popUp: View
    lateinit var imagen: Icon

    private var db = Database()

    val REQUEST_CODE = 2 //el de datapoint es 1

    fun show(editable:Boolean) {
        dialogBuilder = AlertDialog.Builder(activity)
        popUp = layoutInflater.inflate(R.layout.popup_route,null)

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

        var photoContainer : LinearLayout = popUp.findViewById(R.id.photoContainer)
        imagen = layoutInflater.inflate(R.layout.icon_item,null) as Icon
        photoContainer.addView(imagen)

        var notImageCase = R.drawable.imagen_anadir
        if (!editable) {
            notImageCase = R.drawable.error_image
        }
        loadRouteImageFromDB(notImageCase)

        if (editable) {
            imagen.setOnClickListener {
                pickImageFromGallery()
            }
        }

        dialog.show()
    }

    fun uploadPhotoDBShow(ref: Uri) {
        db.uploadRoutePhoto(ref,routeId,context) {
            if(it!=null) {
                Glide.with(context)
                    .load(ref)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.loading)
                            .error(R.drawable.error_image)
                    )
                    .into(imagen)
                imagen.cutImage()
            }
        }
    }

    private fun loadRouteImageFromDB(placeholder: Int) {
        val ref: StorageReference = db.getRoutePhotoRef(routeId)

        ref.downloadUrl.addOnSuccessListener {
            Glide.with(context)
                .load(it)
                .apply(
                    RequestOptions()
                    .placeholder(R.drawable.loading)
                    .error(placeholder)
                )
                .into(imagen)
            imagen.cutImage()
        }
        .addOnFailureListener {
            Glide.with(context)
                .load(placeholder)
                .into(imagen)
            imagen.cutImage()
        }

    }

    //TOMAR IMAGEN DE GALERÍA
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        ActivityCompat.startActivityForResult(
            activity,
            intent,
            REQUEST_CODE,
            null
        )
    }

    //AUXILIAR
    private fun editable(cuadroTexto: EditText, editable: Boolean) {
        cuadroTexto.setFocusable(editable)
        cuadroTexto.setClickable(editable)
        cuadroTexto.setFocusableInTouchMode(editable)
        cuadroTexto.setLongClickable(editable)
    }
}