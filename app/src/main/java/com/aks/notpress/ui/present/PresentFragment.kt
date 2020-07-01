package com.aks.notpress.ui.present

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentPresentBinding
import com.aks.notpress.setStyle
import com.aks.notpress.ui.main.MainActivity
import com.aks.notpress.utils.*

class PresentFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private lateinit var factory: ViewModelFactory
    private val permissionUtil = PermissionUtil()
    private val activityUtil = ActivityUtil()

    private lateinit var viewModel: PresentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory((activity as? MainActivity)?.preference?:return)
        viewModel = ViewModelProvider(this, factory).get(PresentViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
        permissionUtil.observe(this, viewModel, activity)
        activityUtil.observe(this, viewModel, activity)
    }

    override fun onResume() {
        super.onResume()
        checkGrantedPermission()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPresentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        viewModel.isGrantedPermission.observe(viewLifecycleOwner, Observer { checkGrantedPermission() })
        activity?.setStyle(R.color.colorLightBlue, R.color.colorPrimary)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    private fun checkGrantedPermission(){
        if (context == null) return
        if (ContextCompat.checkSelfPermission(context!!, PermissionType.READ_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED  ||
            ContextCompat.checkSelfPermission(context!!, PermissionType.WRITE_EXTERNAL_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED )
            viewModel.checkGrantedPermissionDialog()
    }
    companion object {
        fun newInstance() = PresentFragment()
    }
}