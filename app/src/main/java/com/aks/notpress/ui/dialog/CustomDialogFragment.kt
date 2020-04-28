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

enum class CustomDialog{
    DIALOG_ONE,
    DIALOG_TWO,
    DIALOG_THREE;
    companion object{
        fun getStateDialog(state: String?) = values().find { it.name.equals(state, ignoreCase = true) }
    }
}
class CustomDialogFragment(private val callBack: CallBack?): DialogFragment(){
    private val fragmentUtil = FragmentUtil()
    private val finishUtil = FinishUtil()

    private lateinit var viewModel: DialogViewModel

    interface CallBack{
        fun cancelDialog(state: CustomDialog?)
        fun okDialog(state: CustomDialog?)
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

        viewModel.initState(CustomDialog.getStateDialog(arguments?.getString(ARG_STATE_DIALOG)))

        val intRes = arguments?.getInt(ARG_TEXT_INT)?:0
        val text =  if (intRes!=0)  context?.getString(intRes)?:""
                    else            arguments?.getString(ARG_TEXT)?:""
        viewModel.initText(text)
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
        private const val ARG_TEXT_INT = "ARG_TEXT_INT"
        private const val ARG_TEXT = "ARG_TEXT"
        private const val ARG_STATE_DIALOG = "ARG_STATE_DIALOG"
        fun newInstance(resText: Int?, text: String?, state: CustomDialog? = null, callBack: CallBack?) = CustomDialogFragment(callBack)
            .apply { arguments = Bundle().apply {
                    putInt(ARG_TEXT_INT, resText?:0)
                    putString(ARG_TEXT, text)
                    putString(ARG_STATE_DIALOG, state?.name)
            } }
    }
}