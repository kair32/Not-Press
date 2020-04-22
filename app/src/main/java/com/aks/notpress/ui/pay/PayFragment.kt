package com.aks.notpress.ui.pay

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.databinding.FragmentPayBinding
import com.aks.notpress.utils.FragmentUtil
import com.aks.notpress.utils.PreferencesBasket
import com.aks.notpress.utils.ViewModelFactory

class PayFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private lateinit var factory: ViewModelFactory

    private lateinit var viewModel:PayViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory(PreferencesBasket(activity?: return))
        viewModel = ViewModelProvider(this, factory).get(PayViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPayBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        binding.tv.movementMethod = ScrollingMovementMethod()
        binding.setLifecycleOwner(this)

        viewModel.isHaveSubscription.observe(viewLifecycleOwner, Observer {
            if (it) activity?.onBackPressed()
        })
        return binding.root
    }

    companion object {
        fun newInstance() = PayFragment()
    }
}