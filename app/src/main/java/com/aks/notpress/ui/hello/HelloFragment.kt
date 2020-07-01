package com.aks.notpress.ui.hello

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentHelloBinding
import com.aks.notpress.setStyle
import com.aks.notpress.ui.main.MainActivity
import com.aks.notpress.utils.*
import com.bumptech.glide.Glide

class HelloFragment: Fragment(){
    private val fragmentUtil = FragmentUtil()
    private lateinit var factory: ViewModelFactory

    private lateinit var viewModel: HelloViewModel

    override fun onResume() {
        super.onResume()
        viewModel.updateHaveOffer()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        factory = ViewModelFactory((activity as? MainActivity)?.preference?:return)
        viewModel = ViewModelProvider(this, factory).get(HelloViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHelloBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        activity?.setStyle(R.color.colorLightBlue, R.color.colorPrimary)
        context?.let {
            Glide.with(it)
                .load(R.drawable.phone_animation)
                .into(binding.ivAnimationPhone)
        }
        return binding.root
    }

    companion object {
        fun newInstance() = HelloFragment()
    }
}