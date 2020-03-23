package com.aks.notpress

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.BindingAdapter

@BindingAdapter("buttonBackground")
fun setButtonBackground(view: View, color: Int){
    view.background?.let {
        if (it is LayerDrawable) {
            val drawable = (it.findDrawableByLayerId(R.id.background))
            drawable.mutate().colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}

@BindingAdapter("switchWidth")
fun setSwitchWidth(view: SwitchCompat, margin: Float){
    view.switchMinWidth = view.rootView.width - (margin * 2).toInt()
}