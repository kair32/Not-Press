package com.aks.notpress.ui.password

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.databinding.FragmentPasswordBinding
import com.aks.notpress.utils.FragmentUtil
import com.aks.notpress.utils.PasswordView
import com.aks.notpress.utils.PreferencesBasket

class PasswordFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private lateinit var factory: PasswordFactory

    private lateinit var viewModel: PasswordViewModel
    private lateinit var binding: FragmentPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        factory = PasswordFactory(PreferencesBasket(context?:return))
        viewModel = ViewModelProvider(this, factory).get(PasswordViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPasswordBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        binding.itemsPassword.drawerView.setViewModel(viewModel)
        binding.setLifecycleOwner(this)
        PasswordView.getPosition(binding.itemsPassword)
        return binding.root
    }

    companion object {
        fun newInstance() = PasswordFragment()
    }
}