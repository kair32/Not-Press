package com.aks.notpress.ui.offer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentOfferBinding
import com.aks.notpress.databinding.FragmentPayBookBinding
import com.aks.notpress.setStyle
import com.aks.notpress.ui.book.PayBookFragment
import com.aks.notpress.ui.book.PayBookViewModel
import com.aks.notpress.ui.book.PayBookViewModelImpl
import com.aks.notpress.utils.*
import jp.wasabeef.blurry.Blurry

class OfferFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private val activityUtil = ActivityUtil()
    private lateinit var factory: ViewModelFactory
    private val permissionUtil = PermissionUtil()
    private val finishUtil = FinishUtil()

    private lateinit var viewModel: OfferViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory(PreferencesBasket(activity?: return))
        viewModel = ViewModelProvider(this, factory).get(OfferViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
        activityUtil.observe(this, viewModel, activity)
        permissionUtil.observe(this, viewModel, activity)
        finishUtil.observe(this, viewModel, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentOfferBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        binding.ivVarenik.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                Blurry.with(context)
                    .capture(view)
                    .into(binding.ivVarenik)
                binding.ivVarenik.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }})

        activity?.setStyle(R.color.colorRed, R.color.colorDeepRed)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    companion object {
        fun newInstance() = OfferFragment()
    }
}