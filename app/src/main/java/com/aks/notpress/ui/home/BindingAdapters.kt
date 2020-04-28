package com.aks.notpress.ui.home

import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.BindingAdapter
import com.aks.notpress.R
import com.aks.notpress.utils.StateSubscription
import com.aks.notpress.utils.StateSubscription.*

@BindingAdapter("stateSubscription")
fun setStateSubscription(root: ConstraintLayout, stateSubscription: StateSubscription){
    Log.d("stateSubscription", stateSubscription.name)
    when(stateSubscription){
        HAVE_SUB -> {
            ConstraintSet().apply {
                clone(root)
                setVisibility(R.id.switch_container, View.VISIBLE)
                setVisibility(R.id.bt_gifts, View.GONE)
                setVisibility(R.id.bt_days, View.GONE)
                setVisibility(R.id.ll_insta, View.VISIBLE)
                connect(R.id.butterfly, ConstraintSet.TOP, R.id.switch_container, ConstraintSet.BOTTOM, 0)
                TransitionManager.beginDelayedTransition(root)
                applyTo(root)
            }
        }
        NOT_ACTIVE -> {}
        FREE_DAY, ENDED -> {
            ConstraintSet().apply {
                clone(root)
                setVisibility(R.id.switch_container, View.VISIBLE)
                setVisibility(R.id.bt_days, View.GONE)
                setVisibility(R.id.ll_insta, View.GONE)
                connect(R.id.butterfly, ConstraintSet.TOP, R.id.switch_container, ConstraintSet.BOTTOM, 0)
                connect(R.id.butterfly, ConstraintSet.BOTTOM, R.id.bt_gifts, ConstraintSet.TOP, 0)
                clear(R.id.bt_gifts, ConstraintSet.TOP)
                connect(R.id.bt_gifts, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, root.context.resources.getDimension(R.dimen.margin_20).toInt())
                TransitionManager.beginDelayedTransition(root)
                applyTo(root)
            }
        }
    }
}

@BindingAdapter("switchWidth")
fun setSwitchWidth(root: ConstraintLayout, margin: Float){
        root.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                root.findViewById<SwitchCompat>(R.id.switch_varenik).switchMinWidth = root.width - (margin*2).toInt()
                if (root.width>0)
                    root.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }})
}