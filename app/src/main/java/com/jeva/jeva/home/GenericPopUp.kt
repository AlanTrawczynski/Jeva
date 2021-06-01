package com.jeva.jeva.home

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.graphics.drawable.toDrawable
import com.jeva.jeva.R


class GenericPopUp {

    fun showPopupWindow(view: View, layout : Int, f: () -> Unit) {
        val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val popupView: View = inflater.inflate(layout, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        val closeBtn : Button = popupView.findViewById(R.id.popupBtnClose)
        val applyBtn : Button = popupView.findViewById(R.id.popupBtnApply)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        closeBtn.setOnClickListener { popupWindow.dismiss() }
        applyBtn.setOnClickListener {
            f()
        }
    }

}