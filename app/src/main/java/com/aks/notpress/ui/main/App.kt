package com.aks.notpress.ui.main

import android.app.Activity
import android.app.Application
import android.content.Context
import com.aks.notpress.utils.PreferencesBasket

class App: Application(){
    var preference : PreferencesBasket? = null
    override fun onCreate() {
        super.onCreate()
    }
    fun createPreference(activity: Activity){
        if (preference == null)
            preference = PreferencesBasket(activity)
    }
    companion object {
        fun get(context: Context) = context.applicationContext as App
    }
}