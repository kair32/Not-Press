package com.aks.notpress

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import kotlin.math.cos
import kotlin.math.sqrt


@BindingAdapter("background")
fun setButtonBackground(view: View, color: Int){
    view.background?.let {
        if (it is LayerDrawable) {
            val drawable = (it.findDrawableByLayerId(R.id.background))
            drawable.mutate().colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}

@BindingAdapter("isVisible")
fun setVisibility(view: View, isVisible: Boolean) {
    Log.d("BindingAdapter","isVisible = $isVisible")
    view.visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("transitionManager")
fun setTransition(view: View, ignor: Boolean) {
    try { val viewGroup = view.rootView as ViewGroup
        TransitionManager.beginDelayedTransition(viewGroup)
    } catch (ignored: NullPointerException) { }
}

@BindingAdapter("alphaAnimation")
fun setAlpha(view: View, isAlpha: Boolean) {
    val start = 0.3f
    val fadeOut = if (isAlpha)
    AlphaAnimation(start, 1f).apply {
        duration = 1500
        repeatMode = Animation.REVERSE
        repeatCount = 1
        this.fillAfter = true
    }
    else AlphaAnimation(1f, start).apply {
        duration = 1500
        repeatMode = Animation.REVERSE
        repeatCount = 1
        this.fillAfter = true
    }
    //view.alpha = if (isAlpha) start else 1.0f
    view.startAnimation(fadeOut)
}

@BindingAdapter("backgroundIv")
fun setIvBackground(view: ImageView, color: Int){
    if (color==0)return
    view.drawable?.let {
        if (it is LayerDrawable) {
            val drawable = (it.findDrawableByLayerId(R.id.password_background))
            drawable.mutate().colorFilter = PorterDuffColorFilter(ContextCompat.getColor(view.context, color), PorterDuff.Mode.SRC_IN)
        }
    }
}

@BindingAdapter("text")
fun setText(view: TextView, res: Int?) = if (res!=null && res != 0) view.setText(res) else Unit

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

@BindingAdapter(value = ["buttonBackgroundRounded", "buttonRadius", "buttonRadiusFloat"], requireAll = false)
fun setButtonBackground(view: View, color: Int?, radius: Int?, radiusFloat: Float?){
    fun edit(it: LayerDrawable, id: Int){
        (it.findDrawableByLayerId(id).mutate() as GradientDrawable ?: null)?.let { drawable ->
            color?.let {
                when {
                    it > 0 -> drawable.setColor(ContextCompat.getColor(view.context, it))
                    it < 0 -> drawable.setColor(it)
                }
            }
            radius?.let { drawable.cornerRadius = view.context.resources.getDimension(it) }
            radiusFloat?.let { drawable.cornerRadius = it }
        }
    }
    view.background?.let {
        if (it is LayerDrawable) {
            edit(it, R.id.stroke)
            (it.findDrawableByLayerId(R.id.background).mutate() ?:null)?.let { draw ->
                edit((draw as LayerDrawable), R.id.button_background)
            }
        }
    }

}



