package com.jeva.jeva.images.adapters
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jeva.jeva.R
import com.jeva.jeva.images.Icon
import com.jeva.jeva.images.dataPointMenu


// un adapter está entre medias del View y la fuente de datos. El view le pregunta al adapter qué
// debe mostrar y el adapter responde, transformando uno de los elementos del dataSource a View.
class ImageAdapter(private val context: Context, private val dataSource: ArrayList<Pair<String,Uri>>, private val editable: Boolean) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        lateinit var IconView: Icon
        if (convertView== null) {
            IconView  = Icon(context)
            IconView.setLayoutParams(AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        } else {
            //por motivos de eficiencia, siempre que convertView no sea null, emplearlo.
            IconView = convertView as Icon
        }
        Glide.with(context)
            .load(dataSource[position].second)
            .apply(RequestOptions()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error_image)
            )
            .into(IconView)
        IconView.cutImage()
        return IconView
    }

    fun add(photoId: String, resource: Uri) {
        var pos : Int = dataSource.size
        if (editable) {
            pos -= 1
        }
        var par : Pair<String, Uri> = Pair(photoId, resource)
        dataSource.add(pos,par)
        dataPointMenu.refreshTam()
        notifyDataSetChanged()
    }

    fun remove(pos: Int) {
        dataSource.removeAt(pos)
        dataPointMenu.refreshTam()
        notifyDataSetChanged()
    }

    fun getDataSource() : ArrayList<Pair<String,Uri>> {
        return dataSource
    }
}