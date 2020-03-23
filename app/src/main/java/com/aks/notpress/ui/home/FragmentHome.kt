package com.aks.notpress.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aks.notpress.databinding.FragmentHomeBinding

class FragmentHome: Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }
    companion object {
        fun newInstance() = FragmentHome()
    }
}