package com.jeva.jeva.images.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.jeva.jeva.R
import java.util.*

class swipeAdapter(private val context: Context, private val dataSource:ArrayList<Uri>) : PagerAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        //Este método se ha de implementar. Hay que comprobar si view está asociado con object
        //el adapter emplea object como key de la página a mostrar.
        return view == `object` as LinearLayout
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun instantiateItem(container: ViewGroup, position:  Int): View {
        var swipeView : View = inflater.inflate(R.layout.photo_item,container,false)
        var imageView : ImageView = swipeView.findViewById(R.id.swipedPhoto)
        Glide.with(context)
            .load(dataSource[position])
            .into(imageView)
        Objects.requireNonNull(container).addView(swipeView)

        return swipeView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout?)
    }
}