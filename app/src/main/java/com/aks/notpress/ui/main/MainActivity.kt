package com.aks.notpress.ui.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.utils.FragmentUtil
import com.aks.notpress.utils.PreferencesBasket
import com.aks.notpress.utils.ViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var factory: ViewModelFactory
    private val fragmentUtil = FragmentUtil()
    var preference : PreferencesBasket? = null

    private lateinit var viewModel: MainViewModel

    val deleteValue: Boolean= false
    val updateDate: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        App.get(this).createPreference(this)
        preference = App.get(this).preference
        super.onCreate(savedInstanceState)

        if (deleteValue){
            Log.d("MainActivity ","deleteSharedPreferences " + deleteSharedPreferences(PreferencesBasket.PREFERENCES_NAME))
        }
        else {

            if (updateDate) preference?.rewriteToOldFile("13dEf.18435")
            else {
                preference?.let { factory = ViewModelFactory(it)}
                //viewModel = ViewModelProvider(this, factory).get(MainViewModelImpl::class.java)
                viewModel = obtainViewModel(this, MainViewModelImpl::class.java, factory) as MainViewModel
                fragmentUtil.observe(this, viewModel, supportFragmentManager)
            }
        }
        setContentView(R.layout.activity_main)
    }
    private fun <T : ViewModel?> obtainViewModel(activity: AppCompatActivity, modelClass: Class<T>, factory: ViewModelProvider.Factory): ViewModel {
        //val factory = AndroidViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(modelClass)!!
    }
}
