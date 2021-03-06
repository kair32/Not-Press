package com.aks.notpress.ui.book

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentPayBookBinding
import com.aks.notpress.setStyle
import com.aks.notpress.ui.dialog.CustomDialogFragment
import com.aks.notpress.ui.main.MainActivity
import com.aks.notpress.utils.*

class PayBookFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private val activityUtil = ActivityUtil()
    private lateinit var factory: ViewModelFactory
    private val permissionUtil = PermissionUtil()
    private val finishUtil = FinishUtil()

    private lateinit var viewModel: PayBookViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory((activity as? MainActivity)?.preference?:return)
        viewModel = ViewModelProvider(this, factory).get(PayBookViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
        activityUtil.observe(this, viewModel, activity)
        permissionUtil.observe(this, viewModel, activity)
        finishUtil.observe(this, viewModel, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPayBookBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        viewModel.isSale = arguments?.getBoolean(ARG_IS_SALE, false)?:false

        activity?.setStyle(R.color.colorBlue, R.color.colorLilac)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    companion object {
        private const val ARG_IS_SALE = "ARG_IS_SALE"
        fun newInstance(isSale: Boolean) = PayBookFragment()
            .apply { arguments = Bundle()
                .apply { putBoolean(ARG_IS_SALE, isSale)}}
    }
}