package com.aks.notpress.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preference = PreferencesBasket(this)
        factory = ViewModelFactory(preference)
        viewModel = ViewModelProvider(this, factory).get(MainViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, supportFragmentManager)
        setContentView(R.layout.activity_main)
    }
}
