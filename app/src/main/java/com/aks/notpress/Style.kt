package com.aks.notpress

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat


fun Activity.setStyle(colorStatus: Int? = null,
                      colorNavigation: Int? = colorStatus,
                      translucent: Boolean = false,
                      isReverse: Boolean = true,
                      isWhiteTextStatusBar: Boolean = true){
    this.translucent(translucent)
    colorStatus?.let { this.setBarColor(it, isReverse, isWhiteTextStatusBar) }
    colorNavigation?.let { this.navigationBarColor(it) }
}

fun Activity.setBarColor(color: Int, isReverse: Boolean = true, isWhite: Boolean = false){
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && isWhite)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    else    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    this.window.statusBarColor =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || isReverse) ContextCompat.getColor(this, color)
        else ContextCompat.getColor(this, R.color.colorPrimary)
}

fun Activity.translucent(translucent: Boolean = false){
    if(translucent) this.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    else            this.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    /*Не знаю почему не работает
    findViewById<FrameLayout>(R.id.container).apply {
        Log.d("Activity.translucent","$translucent")
        fitsSystemWindows = translucent
        invalidate()
    }*/
}

fun Activity.navigationBarColor(color: Int){
    this.window.navigationBarColor = ContextCompat.getColor(this, color)
}