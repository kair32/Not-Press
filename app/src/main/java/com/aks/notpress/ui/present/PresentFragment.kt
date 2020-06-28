package com.aks.notpress.ui.present

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentPresentBinding
import com.aks.notpress.setStyle
import com.aks.notpress.utils.*

class PresentFragment: Fragment(){
    private val permissionUtil = PermissionUtil()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPresentBinding.inflate(inflater, container, false)

        activity?.setStyle(R.color.colorLightBlue, R.color.colorPrimary)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    companion object {
        fun newInstance() = PresentFragment()
    }
}