package com.jeva.jeva.images


import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.R
import com.jeva.jeva.images.adapters.ImageAdapter
import kotlin.properties.Delegates


class dataPointMenu {

    companion object {
        lateinit var title:String
        lateinit var description:String
        lateinit var fotos: ArrayList<Uri>
        lateinit var fragmentCaller: Fragment
        lateinit var dialogBuilder: AlertDialog.Builder
        lateinit var adapter: ImageAdapter
        val REQUEST_CODE = 1

        fun setInfo(title:String, description:String, fotos:Array<Uri>, caller: Fragment) {
            this.title = title
            this.description = description
            this.fotos = fotos.toCollection(ArrayList())
            this.fragmentCaller = caller
        }

        fun showMenu(editable: Boolean) {
            dialogBuilder = AlertDialog.Builder(fragmentCaller.requireActivity())
            val popUp: View = fragmentCaller.layoutInflater.inflate(R.layout.popup,null)
            adapter = ImageAdapter(fragmentCaller.requireContext(), fotos)

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

            //creamos el cuadro de diálogo y añadimos listener al boton
            dialogBuilder.setView(popUp)
            var dialog = dialogBuilder.create()

            var cerrar: Button = popUp.findViewById(R.id.cerrar)
            cerrar.setOnClickListener { dialog.dismiss() }
            photogrid.setOnItemClickListener { parent, view, position, id ->
                if(editable) {
                    if (position+1 != adapter.getDataSource().size) {
                        dialog.dismiss()
                        var img: Uri = fotos.get(position)
                        val bundle = bundleOf("title" to title, "pos" to position)
                        Navigation.findNavController(fragmentCaller.requireView())
                            .navigate(R.id.swipeImages, bundle)
                    } else {
                        GestionarPermisos.requestStoragePermissions(fragmentCaller.requireActivity())
                        if (GestionarPermisos.accessStorageIsGranted(fragmentCaller.requireActivity())) {
                            pickImageFromGallery()
                        }
                    }
                } else {
                    dialog.dismiss()
                    var img: Uri = fotos.get(position)
                    val bundle = bundleOf("title" to title, "pos" to position)
                    Navigation.findNavController(fragmentCaller.requireView())
                        .navigate(R.id.swipeImages, bundle)
                }
            }
            photogrid.setOnItemLongClickListener { parent, view, position, id ->
                if (position+1 != adapter.getDataSource().size) {
                    adapter.remove(position)
                }
                true
            }
            //mostramos el dialogo
            dialog.show()
            //checkSize()
        }

        private fun pickImageFromGallery() {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            ActivityCompat.startActivityForResult(
                fragmentCaller.requireActivity(),
                intent,
                REQUEST_CODE,
                null
            )
        }

        private fun toUri(resource: Int) : Uri {
            val resources: Resources = fragmentCaller.requireContext().resources
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