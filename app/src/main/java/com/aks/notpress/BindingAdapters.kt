package com.aks.notpress

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.BindingAdapter
import kotlin.math.cos
import kotlin.math.sqrt


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

@BindingAdapter("youtubeSize")
fun setYoutubeSize(view: ConstraintLayout, ignore: Boolean){
    val ivButterfly = view.findViewById<ImageView>(R.id.iv_youtube)
    val ivYoutubeIcon = view.findViewById<ImageView>(R.id.iv_icon)

    ivButterfly.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener{
        override fun onGlobalLayout() {
            Log.d("youtubeSize", "width = ${ivButterfly.width} + height = ${ivButterfly.height}")
            ConstraintSet().apply {
                clone(view)
                val relation = 40//40% - соотношение бабочки к кнопке
                val y = 65.0//65 градусов - угол на который отклоняется кнопка ютуба
                val b = (ivButterfly.height/100) * 72 //72% это отношение высоты фигуры к границам бабочки
                val c = (ivButterfly.width / 2)//ширина бабочки деленная на 2
                val a = (sqrt(b*b + c*c - 2*b*c* cos(Math.toRadians(y)))).toInt()-((ivButterfly.width)/100*relation) / 3//расстояние от центра бабочки, до начала кнопки с вычетом 1/3 фигуры
                constrainHeight(ivYoutubeIcon.id,(ivButterfly.width)/100*relation)
                constrainWidth(ivYoutubeIcon.id,(ivButterfly.width)/100*relation)
                constrainCircle(ivYoutubeIcon.id,ivButterfly.id, a,y.toFloat())
                applyTo(view)
            }
            ivButterfly.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

