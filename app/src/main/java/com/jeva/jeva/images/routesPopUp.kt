package com.jeva.jeva.images

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.StorageException
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
        imagen = popUp.findViewById(R.id.icon2)
        loadRouteImageFromDB() {
            if (!it) {
                uploadPhoto()
            }
        }
    }

    //el icono tiene que tener el aspecto de añadir foto, y al darle tener el listener para subir foto.
    private fun uploadPhoto() {
        Glide.with(context)
            .load(R.drawable.imagen_anadir)
            .into(imagen)
        imagen.cutImage()
        imagen.setOnClickListener {
            pickImageFromGallery()
        }
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
                imagen.setOnClickListener(null) //quitamos el click listener de añadir foto
                imagen.setOnLongClickListener {
                    //delete route foto
                    true
                }
            }
        }
    }

    //gestionar caso de no haber imagen
    //0 esperanza en que funcione
    private fun loadRouteImageFromDB(callback: (Boolean) -> Unit) {
        db.getRoutePhotoRef(routeId).downloadUrl
            .addOnSuccessListener {
                Glide.with(context)
                    .load(it)
                    .apply(
                        RequestOptions()
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.error_image)
                    )
                    .into(imagen)
                imagen.cutImage()
                callback(true) //la descarga ha ido bien. En principio.
            }
            .addOnFailureListener {
                if((it as StorageException).errorCode==StorageException.ERROR_OBJECT_NOT_FOUND) {
                    callback(false) //la descarga ha ido mal pq no hay foto.
                } else {
                    Glide.with(context)
                        .load(R.drawable.error_image)
                        .into(imagen)
                    imagen.cutImage() //la descarga ha ido mal .
                    callback(true)
                }

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