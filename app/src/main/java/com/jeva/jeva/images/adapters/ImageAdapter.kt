package com.jeva.jeva.images.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import com.jeva.jeva.images.Icon


// un adapter está entre medias del View y la fuente de datos. El view le pregunta al adapter qué
// debe mostrar y el adapter responde, transformando uno de los elementos del dataSource a View.
class ImageAdapter(private val context: Context, private val dataSource: ArrayList<Int>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //1
    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        lateinit var IconView: Icon
        if (convertView== null) {
            IconView  = Icon(context)
            IconView.setLayoutParams(GridView@ AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        } else {
            //por motivos de eficiencia, siempre que convertView no sea null, emplearlo.
            IconView = convertView as Icon
        }

        IconView.setImage(dataSource[position])
        return IconView
    }

    fun addAll(resources: ArrayList<Int>) {
        dataSource.addAll(resources)
        notifyDataSetChanged()
    }
    fun add(resource: Int) {
        dataSource.add(resource)
        notifyDataSetChanged()
    }
}