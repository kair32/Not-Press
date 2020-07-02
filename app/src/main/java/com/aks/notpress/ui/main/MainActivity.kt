package com.aks.notpress.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.utils.FragmentUtil
import com.aks.notpress.R
import com.aks.notpress.utils.PreferencesBasket
import com.aks.notpress.utils.ViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var factory: ViewModelFactory
    private val fragmentUtil = FragmentUtil()
    lateinit var preference : PreferencesBasket

    private lateinit var viewModel: MainViewModel

    val deleteValue: Boolean= false
    val updateDate: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (deleteValue){
            Log.d("MainActivity ","deleteSharedPreferences " + deleteSharedPreferences(PreferencesBasket.PREFERENCES_NAME))
        }
        else {
            preference = PreferencesBasket(this)
            if (updateDate) preference.rewriteToOldFile("13dEf.18435")
            else {
                factory = ViewModelFactory(preference)
                viewModel = ViewModelProvider(this, factory).get(MainViewModelImpl::class.java)
                fragmentUtil.observe(this, viewModel, supportFragmentManager)
            }
        }
        setContentView(R.layout.activity_main)
    }
}
