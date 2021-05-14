package com.jeva.jeva.images


import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.R
import com.jeva.jeva.images.adapters.ImageAdapter

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

        fun showMenu() {
            dialogBuilder = AlertDialog.Builder(fragmentCaller.requireActivity())
            val popUp: View = fragmentCaller.layoutInflater.inflate(R.layout.popup,null)
            adapter = ImageAdapter(fragmentCaller.requireContext(), fotos)

            //añadimos nombre y descripción
            var puntoname: EditText = popUp.findViewById(R.id.puntoName)
            var puntodescripcion: EditText = popUp.findViewById(R.id.puntoDescripcion)
            (puntoname as TextView).text = title
            (puntodescripcion as TextView).text = description

            //creación de galería de imágenes
            var photogrid: GridView = popUp.findViewById(R.id.photo_grid)
            photogrid.adapter = adapter

            //creamos el cuadro de diálogo y añadimos listener al boton
            dialogBuilder.setView(popUp)
            var dialog = dialogBuilder.create()
            var cerrar: Button = popUp.findViewById(R.id.cerrar)
            var anadir: Button = popUp.findViewById(R.id.anadir)
            cerrar.setOnClickListener { dialog.dismiss() }
            anadir.setOnClickListener {
                GestionarPermisos.requestStoragePermissions(fragmentCaller.requireActivity())
                if (GestionarPermisos.accessStorageIsGranted(fragmentCaller.requireActivity())) {
                    pickImageFromGallery()
                }
            }
            photogrid.setOnItemClickListener( AdapterView.OnItemClickListener { parent, view, position, id ->
                dialog.dismiss()
                var img: Uri = fotos.get(position)
                val bundle = bundleOf("title" to title,"pos" to position)
                Navigation.findNavController(fragmentCaller.requireView()).navigate(R.id.swipeImages,bundle)
            })
            //mostramos el dialogo
            dialog.show()
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
    }
}