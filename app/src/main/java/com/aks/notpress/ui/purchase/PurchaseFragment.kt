package com.aks.notpress.ui.purchase

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
import com.aks.notpress.databinding.FragmentPurchaseBinding
import com.aks.notpress.setStyle
import com.aks.notpress.ui.hello.HelloFragment
import com.aks.notpress.ui.main.MainActivity
import com.aks.notpress.ui.offer.OfferFragment
import com.aks.notpress.ui.offer.OfferViewModel
import com.aks.notpress.ui.offer.OfferViewModelImpl
import com.aks.notpress.utils.*
import jp.wasabeef.blurry.Blurry

class PurchaseFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private val activityUtil = ActivityUtil()
    private lateinit var factory: ViewModelFactory
    private val permissionUtil = PermissionUtil()
    private val finishUtil = FinishUtil()

    private lateinit var viewModel: PurchaseViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory((activity as? MainActivity)?.preference?:return)
        viewModel = ViewModelProvider(this, factory).get(PurchaseViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
        activityUtil.observe(this, viewModel, activity)
        permissionUtil.observe(this, viewModel, activity)
        finishUtil.observe(this, viewModel, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPurchaseBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        viewModel.setNextVisible(arguments?.getBoolean(ARG_IS_NEXT_VISIBLE_PURCHASE)?:true)
        binding.ivVarenik.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                Blurry.with(context)
                    .capture(view)
                    .into(binding.ivVarenik)
                binding.ivVarenik.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }})

        activity?.setStyle(R.color.colorGreen, R.color.colorDarkGreen)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    companion object {
        private const val ARG_IS_NEXT_VISIBLE_PURCHASE = "ARG_IS_NEXT_VISIBLE_PURCHASE"
        fun newInstance(isNextVisible: Boolean) = PurchaseFragment()
            .apply { arguments = Bundle()
                .apply { putBoolean(ARG_IS_NEXT_VISIBLE_PURCHASE, isNextVisible) } }
    }
}