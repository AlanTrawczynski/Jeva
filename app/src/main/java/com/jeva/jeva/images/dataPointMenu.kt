package com.jeva.jeva.images


import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.Task
import com.jeva.jeva.Database
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.R
import com.jeva.jeva.images.adapters.ImageAdapter
import kotlin.properties.Delegates


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
        var aSubir: ArrayList<Uri> = ArrayList()
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
            adapter = ImageAdapter(context, fotos)

            //añadimos nombre y descripción
            var puntoname: EditText = popUp.findViewById(R.id.puntoName)
            var puntodescripcion: EditText = popUp.findViewById(R.id.puntoDescripcion)
            (puntoname as TextView).text = title
            (puntodescripcion as TextView).text = description

            editable(puntoname, editable)
            editable(puntodescripcion, editable)

            if (editable) {
                // esta imagen será el botón que se empleará para añadir imágenes.
                val uri : Uri = toUri(R.drawable.imagen_anadir)
                this.fotos.add(uri)
            }

            //creación de galería de imágenes
            var photogrid: GridView = popUp.findViewById(R.id.photo_grid)
            photogrid.adapter = adapter
            loadImagesFromDB(routeId, markerId, adapter)

            //creamos el cuadro de diálogo y añadimos listener al boton
            dialogBuilder.setView(popUp)
            var dialog = dialogBuilder.create()

            var cerrar: Button = popUp.findViewById(R.id.cerrar)
            cerrar.setOnClickListener {
                uploadImages(routeId, markerId)
                dialog.dismiss()
            }

            photogrid.setOnItemClickListener { parent, view, position, id ->
                if(navigation!=null) {
                    if(editable) {
                        if (position+1 != adapter.getDataSource().size) {
                            dialog.dismiss()
                            var img: Uri = fotos.get(position)
                            val bundle = bundleOf("title" to title, "pos" to position, "edit" to editable)
                            navigation.navigate(R.id.swipeImages, bundle)
                        } else {
                            GestionarPermisos.requestStoragePermissions(activity)
                            if (GestionarPermisos.accessStorageIsGranted(activity)) {
                                pickImageFromGallery()
                            }
                        }
                    } else {
                        dialog.dismiss()
                        var img: Uri = fotos.get(position)
                        val bundle = bundleOf("title" to title, "pos" to position, "edit" to editable)
                        navigation.navigate(R.id.swipeImages, bundle)
                    }
                } else {
                    if(editable) {
                        if (position+1 == adapter.getDataSource().size) {
                            GestionarPermisos.requestStoragePermissions(activity)
                            if (GestionarPermisos.accessStorageIsGranted(activity)) {
                                pickImageFromGallery()
                            }
                        }
                    }
                }

            }

            if(editable) {
                photogrid.setOnItemLongClickListener { parent, view, position, id ->
                    if (position+1 != adapter.getDataSource().size) {
                        adapter.remove(position)
                    }
                    true
                }
            }
            //mostramos el dialogo
            dialog.show()
        }

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

        private fun editable(cuadroTexto: EditText, editable: Boolean) {
            cuadroTexto.setFocusable(editable)
            cuadroTexto.setClickable(editable)
            cuadroTexto.setFocusableInTouchMode(editable)
            cuadroTexto.setLongClickable(editable)
        }

        private fun loadImagesFromDB(routeId: String, markerId: String, adapter: ImageAdapter) {
            db.getMarkerPhotosRefs(routeId,markerId) {
                if (it!=null) {
                    Log.d("hello", it!!.toString())
                    Log.d("hello", routeId)
                    Log.d("hello", markerId)
                    it.forEach { it2 ->
                        it2.downloadUrl.addOnSuccessListener { Ref ->
                            Log.d("hello", Ref.toString())
                            adapter.add(Ref)
                        }
                    }
                } else {
                    Log.d("hello", "it es null")
                }
            }
        }

        private fun uploadImages(routeId: String, markerId: String) {
            Log.d("hello", "subida")
            Log.d("hello", aSubir.toString())
            aSubir.forEach {
                db.uploadMarkerPhoto(it,routeId,markerId) {
                    if (it!=true) {
                        Toast.makeText(activity, "Hubo un error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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