package com.jeva.jeva.images


import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.jeva.jeva.Database
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.R
import com.jeva.jeva.images.adapters.ImageAdapter


class dataPointMenu {

    companion object {
        lateinit var title:String
        lateinit var description:String
        lateinit var fotos: ArrayList<Uri>

        lateinit var dialogBuilder: AlertDialog.Builder
        lateinit var adapter: ImageAdapter
        lateinit var activity: Activity
        lateinit var context: Context

        lateinit var routeId: String
        lateinit var markerId: String
        private var db = Database()

        val REQUEST_CODE = 1

        fun setInfo(title:String, description:String, fotos:Array<Uri>, routeId: String, markerId: String, activity: Activity, context: Context) {
            this.title = title
            this.description = description
            this.fotos = fotos.toCollection(ArrayList())
            this.markerId = markerId
            this.routeId = routeId
            this.activity = activity
            this.context = context
        }

        fun showMenu(layoutInflater: LayoutInflater, navigation: NavController?, editable: Boolean) {
            dialogBuilder = AlertDialog.Builder(activity)
            val popUp: View = layoutInflater.inflate(R.layout.popup,null)
            adapter = ImageAdapter(context, fotos, editable)

            //añadimos nombre y descripción
            var puntoname: EditText = popUp.findViewById(R.id.puntoName)
            var puntodescripcion: EditText = popUp.findViewById(R.id.puntoDescripcion)
            (puntoname as TextView).text = title
            (puntodescripcion as TextView).text = description

            editable(puntoname, editable)
            editable(puntodescripcion, editable)

            //creación de galería de imágenes
            var photogrid: GridView = popUp.findViewById(R.id.photo_grid)
            photogrid.adapter = adapter

            if(editable) {
                val uri : Uri = toUri(R.drawable.imagen_anadir)
                this.fotos.add(uri)
                photogrid.setOnItemLongClickListener { parent, view, position, id ->
                    if (position+1 != adapter.getDataSource().size) {
                        adapter.remove(position)
                    }
                    true
                }
            }

            loadImagesFromDB(routeId, markerId, adapter)

            //creamos el cuadro de diálogo y añadimos listener al boton
            dialogBuilder.setView(popUp)
            var dialog = dialogBuilder.create()

            var cerrar: Button = popUp.findViewById(R.id.cerrar)
            cerrar.setOnClickListener { dialog.dismiss() }

            photogrid.setOnItemClickListener { parent, view, position, id ->
                if(navigation!=null) {
                    if(editable) {
                        if (position+1 != adapter.getDataSource().size) {
                            dialog.dismiss()
                            var img: Uri = fotos.get(position)
                            val bundle = bundleOf("title" to title, "pos" to position, "edit" to editable)
                            navigation.navigate(R.id.swipeImages, bundle)
                        }
                    } else {
                        dialog.dismiss()
                        var img: Uri = fotos.get(position)
                        val bundle = bundleOf("title" to title, "pos" to position, "edit" to editable)
                        navigation.navigate(R.id.swipeImages, bundle)
                    }
                }
                if(editable) {
                    if (position+1 == adapter.getDataSource().size) {
                        GestionarPermisos.requestStoragePermissions(activity)
                        if (GestionarPermisos.accessStorageIsGranted(activity)) {
                            pickImageFromGallery()
                        }
                    }
                }
            }

            //mostramos el dialogo
            dialog.show()
        }

        //CARGAR IMÁGENES DE DB
        private fun loadImagesFromDB(routeId: String, markerId: String, adapter: ImageAdapter) {
            db.getMarkerPhotosRefs(routeId,markerId) {
                if (it!=null) {
                    it.forEach { it2 ->
                        it2.downloadUrl.addOnSuccessListener { Ref ->
                            adapter.add(Ref)
                        }
                    }
                } else {
                    Log.d("hello", "it es null")
                }
            }
        }

        //SUBIDA DE IMÁGENES
        fun uploadImageShow(imageRef: Uri) {
            uploadImage(routeId, markerId, imageRef) {
                if(it) {
                    //Sólo si se ha subido a la BD, se muestra al usuario la imagen
                    adapter.add(imageRef)
                } else {
                    Toast.makeText(activity, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun uploadImage(routeId: String, markerId: String, imageRef: Uri, callback: (Boolean) -> Unit) {
            db.uploadMarkerPhoto(imageRef, routeId, markerId, context) {
                if (!it) {
                    Toast.makeText(activity, "Hubo un error", Toast.LENGTH_SHORT).show()
                }

                callback(it)
            }
        }

        //TOMAR IMAGEN DE GALERÍA
        private fun pickImageFromGallery() {
            //Cuando obtiene la imagen de la galería ActivityCompat llama a la clase del activity
            //(HomeActivity) al método  onActivityResult. Desde ahí se llamará al metodo uploadImageShow
            //de esta clase.
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            ActivityCompat.startActivityForResult(
                activity,
                intent,
                REQUEST_CODE,
                null
            )
        }

        //DADO UN INT RETORNA Uri
        private fun toUri(resource: Int) : Uri {
            val resources: Resources = context.resources
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resource))
                .appendPath(resources.getResourceTypeName(resource))
                .appendPath(resources.getResourceEntryName(resource))
                .build()
            return uri
        }

        //AUXILIAR
        private fun editable(cuadroTexto: EditText, editable: Boolean) {
            cuadroTexto.setFocusable(editable)
            cuadroTexto.setClickable(editable)
            cuadroTexto.setFocusableInTouchMode(editable)
            cuadroTexto.setLongClickable(editable)
        }




        /*
        fun checkSize() {
            val popUp: View = fragmentCaller.layoutInflater.inflate(R.layout.popup,null)
            var photogrid: GridView = popUp.findViewById(R.id.photo_grid)
            var params: ViewGroup.LayoutParams = photogrid.layoutParams
            var tam: Int = 175
            if (adapter.getDataSource().size>2) {
                tam = DpToPixels(350)
            }
            photogrid.layoutParams.height = tam
            photogrid.requestLayout()
        }

        private fun DpToPixels(dp: Int) : Int {
            val escala: Float = fragmentCaller.requireContext().resources.displayMetrics.density;
            var tam: Int = (dp * escala + 0.5f).toInt()
            return tam
        }*/
     }

}