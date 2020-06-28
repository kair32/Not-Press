package com.aks.notpress.ui.book

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentHomeBinding
import com.aks.notpress.databinding.FragmentPayBookBinding
import com.aks.notpress.service.notification.Notification
import com.aks.notpress.ui.home.FragmentHome
import com.aks.notpress.ui.home.HomeViewModel
import com.aks.notpress.ui.home.HomeViewModelImpl
import com.aks.notpress.utils.*

class PayBookFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private val activityUtil = ActivityUtil()
    private lateinit var factory: ViewModelFactory
    private val permissionUtil = PermissionUtil()

    private lateinit var viewModel: PayBookViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory(PreferencesBasket(activity?: return))
        viewModel = ViewModelProvider(this).get(PayBookViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
        activityUtil.observe(this, viewModel, activity)
        permissionUtil.observe(this, viewModel, activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPayBookBinding.inflate(inflater, container, false)

        binding.setLifecycleOwner(this)
        return binding.root
    }

    companion object {
        fun newInstance() = PayBookFragment()
    }
}