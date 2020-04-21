package com.aks.notpress.ui.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.utils.*

interface DialogViewModel: FragmentViewModel, ActivityStartViewModel, FinishViewModel{
    var callBack: CustomDialogFragment.CallBack?
    val text: LiveData<String>

    fun initText(text: String)
    fun onOk()
    fun onCancel()
}

class DialogViewModelImpl: ViewModelBase(),DialogViewModel{
    override var callBack: CustomDialogFragment.CallBack? = null
    override val text = MutableLiveData<String>("")

    override fun initText(text: String) = this.text.postValue(text)
    override fun onOk() {
        callBack?.okDialog()
        finish(FinishEvent(FinishType.DISMISS))
    }

    override fun onCancel() {
        callBack?.cancelDialog()
    }
}