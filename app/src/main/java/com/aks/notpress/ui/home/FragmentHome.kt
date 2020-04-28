package com.aks.notpress.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentHomeBinding
import com.aks.notpress.service.notification.Notification
import com.aks.notpress.utils.*

class FragmentHome: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private val activityUtil = ActivityUtil()
    private val permissionUtil = PermissionUtil()
    private lateinit var factory: ViewModelFactory

    private lateinit var viewModel:HomeViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory(PreferencesBasket(activity?: return))
        viewModel = ViewModelProvider(this, factory).get(HomeViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
        activityUtil.observe(this, viewModel, activity)
        permissionUtil.observe(this, viewModel, activity)
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        checkGrantedPermission()
        viewModel.onUpdate()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        Notification().init(activity)
        viewModel.textFreeDay = String.format(context?.getString(R.string.value_free_day)?:"",
                viewModel.daySubscription.value!!)
        viewModel.initChecked(Notification().isActiveNotification(context))

        viewModel.isChecked.observe(viewLifecycleOwner, Observer {
            if (it) Notification().statNotification(context)
            else    Notification().stopNotification(context)
        })
        viewModel.isCheckPermissionOverlay.observe(viewLifecycleOwner, Observer { checkPermission() })
        viewModel.isGrantedPermission.observe(viewLifecycleOwner, Observer { checkGrantedPermission() })
        binding.setLifecycleOwner(this)
        return binding.root
    }

    private fun checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!Settings.canDrawOverlays(activity!!.baseContext)) viewModel.checkPermissionDialog()
    }
    private fun checkGrantedPermission(){
        if (context == null) return
        if (ContextCompat.checkSelfPermission(context!!, PermissionType.READ_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED  ||
            ContextCompat.checkSelfPermission(context!!, PermissionType.WRITE_EXTERNAL_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED )
            viewModel.checkGrantedPermissionDialog()
    }

    companion object {
        fun newInstance() = FragmentHome()
    }
}