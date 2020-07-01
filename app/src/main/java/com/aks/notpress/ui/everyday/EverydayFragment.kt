package com.aks.notpress.ui.everyday

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentEverydayBinding
import com.aks.notpress.setStyle
import com.aks.notpress.ui.main.MainActivity
import com.aks.notpress.utils.*
import jp.wasabeef.blurry.Blurry

class EverydayFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private val activityUtil = ActivityUtil()
    private lateinit var factory: ViewModelFactory
    private val permissionUtil = PermissionUtil()

    private lateinit var viewModel: EverydayViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory((activity as? MainActivity)?.preference?:return)
        viewModel = ViewModelProvider(this, factory).get(EverydayViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
        activityUtil.observe(this, viewModel, activity)
        permissionUtil.observe(this, viewModel, activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentEverydayBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        binding.ivVarenik.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                Blurry.with(context)
                    .capture(view)
                    .into(binding.ivVarenik)
                binding.ivVarenik.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }})

        activity?.setStyle(R.color.colorViolet, R.color.colorRed)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    companion object {
        fun newInstance() = EverydayFragment()
    }
}