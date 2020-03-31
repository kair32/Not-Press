package com.aks.notpress.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.databinding.FragmentHomeBinding
import com.aks.notpress.service.notification.Notification
import com.aks.notpress.utils.ActivityUtil
import com.aks.notpress.utils.FragmentUtil
import com.aks.notpress.utils.PreferencesBasket
import com.aks.notpress.utils.ViewModelFactory

class FragmentHome: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private val activityUtil = ActivityUtil()
    private lateinit var factory: ViewModelFactory

    private lateinit var viewModel:HomeViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory(PreferencesBasket(context ?: return))
        viewModel = ViewModelProvider(this, factory).get(HomeViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
        activityUtil.observe(this, viewModel, activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        binding.setLifecycleOwner(this)
        Notification().init(activity)

        viewModel.isChecked.observe(viewLifecycleOwner, Observer {
            if (it) Notification().statNotification(context)
            else    Notification().stopNotification(context)
        })
        return binding.root
    }

    companion object {
        fun newInstance() = FragmentHome()
    }
}