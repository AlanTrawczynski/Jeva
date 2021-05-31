package com.jeva.jeva.images

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.StorageReference
import com.jeva.jeva.R
import com.jeva.jeva.database.Database
import com.jeva.jeva.home.EditRouteActivity

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
    private val glide = Glide.with(context)
        .applyDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error_image)
        )

    private var db = Database()

    val REQUEST_CODE = 2 //el de datapoint es 1

    fun show(editable:Boolean) {
        dialogBuilder = AlertDialog.Builder(activity)
        popUp = layoutInflater.inflate(R.layout.popup_route,null)
        var isUpdate = true

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
        val deleteRoute: Button = popUp.findViewById(R.id.borrar_ruta)
        val spacebar: View = popUp.findViewById(R.id.spaceBetweenBtns)
        deleteRoute.setOnClickListener {
            //Añadir el método que elimina
            isUpdate = false
            db.deleteRoute(routeId = routeId){
                if(it){
                    activity.finish()
                }
                else{
                    Toast.makeText(activity, context.getString(R.string.deleteRouteError), Toast.LENGTH_SHORT).show()
                }
            }
        }

        var cerrar: Button = popUp.findViewById(R.id.cerrar)
        cerrar.setOnClickListener {
            dialog.dismiss()
        }

        var photoContainer : LinearLayout = popUp.findViewById(R.id.photoContainer)
        imagen = layoutInflater.inflate(R.layout.icon_item,null) as Icon
        photoContainer.addView(imagen)

        var notImageCase = R.drawable.imagen_anadir


        if(!editable){
            deleteRoute.visibility = View.GONE
            spacebar.visibility = View.GONE
            notImageCase = R.drawable.error_image
        }

        loadRouteImageFromDB(notImageCase)

        if (editable) {
            imagen.setOnClickListener {
                pickImageFromGallery()
            }
        }

        dialog.setOnDismissListener {
            if (isUpdate && editable){
                EditRouteActivity.updateRoute(tit = rutaname.text.toString(), desc = rutadescripcion.text.toString())
            }
        }

        dialog.show()
    }

    fun uploadPhotoDBShow(ref: Uri) {
        db.uploadRoutePhoto(ref,routeId,context) {
            if(it != null) {
                glide.load(ref).into(imagen)
                imagen.cutImage()
            }
            else {
                Toast.makeText(context, "Ha ocurrido un error al subir la foto", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadRouteImageFromDB(placeholder: Int) {
        val ref: StorageReference = db.getRoutePhotoRef(routeId)

        ref.downloadUrl.addOnSuccessListener {
            glide.load(it).into(imagen)
            imagen.cutImage()
        }
        .addOnFailureListener {
            glide.load(placeholder).into(imagen)
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