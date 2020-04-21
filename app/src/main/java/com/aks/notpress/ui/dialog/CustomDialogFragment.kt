package com.aks.notpress.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.R
import com.aks.notpress.databinding.FragmentDialogBinding
import com.aks.notpress.utils.ActivityUtil
import com.aks.notpress.utils.FinishUtil
import com.aks.notpress.utils.FragmentUtil

class CustomDialogFragment(private val callBack: CallBack?): DialogFragment(){
    private val fragmentUtil = FragmentUtil()
    private val finishUtil = FinishUtil()

    private lateinit var viewModel: DialogViewModel

    interface CallBack{
        fun cancelDialog()
        fun okDialog()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(DialogViewModelImpl::class.java)
        fragmentUtil.observe(this, viewModel, activity?.supportFragmentManager)
        finishUtil.observe(this, viewModel, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentDialogBinding.inflate(LayoutInflater.from(activity))
        binding.viewModel = viewModel
        viewModel.callBack = callBack

        viewModel.initText(context?.getString(arguments?.getInt(ARG_TEXT) ?: 0)?:"")
        binding.setLifecycleOwner(this)
        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.onCancel()
        super.onCancel(dialog)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?)
            = Dialog(activity!!, R.style.CompositeDialog).apply {
        setCanceledOnTouchOutside(false)
    }

    companion object {
        private const val ARG_TEXT = "ARG_TEXT"
        fun newInstance(resText: Int, callBack: CallBack?) = CustomDialogFragment(callBack)
            .apply { arguments = Bundle().apply {
                    putInt(ARG_TEXT, resText)
            } }
    }
}