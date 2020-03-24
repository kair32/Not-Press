package com.aks.notpress.ui.pay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aks.notpress.databinding.FragmentPayBinding

class PayFragment: Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPayBinding.inflate(inflater, container, false)

        return binding.root
    }
    companion object {
        fun newInstance() = PayFragment()
    }
}