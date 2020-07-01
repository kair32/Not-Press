package com.aks.notpress.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.utils.FragmentUtil
import com.aks.notpress.R
import com.aks.notpress.utils.PreferencesBasket

class MainActivity : AppCompatActivity() {
    private val fragmentUtil = FragmentUtil()
    lateinit var preference : PreferencesBasket

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, supportFragmentManager)
        setContentView(R.layout.activity_main)
        preference = PreferencesBasket(this)
    }
}
