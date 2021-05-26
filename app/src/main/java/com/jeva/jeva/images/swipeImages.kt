package com.jeva.jeva.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jeva.jeva.R
import com.jeva.jeva.images.adapters.swipeAdapter

class swipeImages : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val supportActionBar =
                (requireActivity() as AppCompatActivity).supportActionBar
        supportActionBar?.title = requireArguments().getString("title")!!

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_swipe_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var pos : Int = requireArguments().getInt("pos") //pos de la imagen tocada
        var edit: Boolean = requireArguments().getBoolean("edit")
        var swipeView: ViewPager = requireActivity().findViewById(R.id.viewSwipe)
        var source = dataPointMenu.fotos.map { par -> par.second }.toCollection(ArrayList())

        if (edit) {
            source = ArrayList(source.slice(IntRange(0,source.size-2)))
        }

        var adapter: swipeAdapter = swipeAdapter(this.requireContext(), source)
        swipeView.adapter = adapter
        swipeView.setCurrentItem(pos)
    }


    override fun onResume() {
        super.onResume()
        var navbar = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        //ocultamos la navbar de abajo pues queremos ver las fotos completas
        navbar.visibility = View.INVISIBLE
    }

    override fun onStop() {
        super.onStop()
        var navbar = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        //volvemos a mostrar navbar
        navbar.visibility = View.VISIBLE
    }


}