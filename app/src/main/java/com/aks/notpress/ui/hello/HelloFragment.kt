package com.aks.notpress.ui.hello

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentHelloBinding
import com.aks.notpress.setStyle

class HelloFragment: Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHelloBinding.inflate(inflater, container, false)

        activity?.setStyle(R.color.colorLightBlue, R.color.colorPrimary)
        binding.tvNext.setOnClickListener {activity?.onBackPressed() }
        return binding.root
    }

    companion object {
        fun newInstance() = HelloFragment()
    }
}