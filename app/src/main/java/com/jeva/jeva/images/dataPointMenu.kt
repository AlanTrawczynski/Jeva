package com.jeva.jeva.images


import android.app.AlertDialog
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.jeva.jeva.R
import com.jeva.jeva.images.adapters.ImageAdapter

class dataPointMenu {

    companion object {
        lateinit var title:String
        lateinit var description:String
        lateinit var fotos: ArrayList<Int>
        lateinit var fragmentCaller: Fragment
        lateinit var dialogBuilder: AlertDialog.Builder

        fun setInfo(title:String, description:String, fotos:Array<Int>, caller: Fragment) {
            this.title = title
            this.description = description
            this.fotos = fotos.toCollection(ArrayList())
            this.fragmentCaller = caller
        }

        fun showMenu() {
            dialogBuilder = AlertDialog.Builder(fragmentCaller.requireActivity())
            val popUp: View = fragmentCaller.layoutInflater.inflate(R.layout.popup,null)
            val adapter: ImageAdapter = ImageAdapter(fragmentCaller.requireContext(), fotos)

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
            cerrar.setOnClickListener { dialog.dismiss() }

            photogrid.setOnItemClickListener( AdapterView.OnItemClickListener { parent, view, position, id ->
                dialog.dismiss()
                var img: Int = fotos.get(position)
                val bundle = bundleOf("title" to title,"pos" to position)
                Navigation.findNavController(fragmentCaller.requireView()).navigate(R.id.swipeImages,bundle)
            })
            //mostramos el dialogo
            dialog.show()
        }
    }
}