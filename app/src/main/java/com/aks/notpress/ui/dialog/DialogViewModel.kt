package com.aks.notpress.ui.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.utils.*

interface DialogViewModel: FragmentViewModel, ActivityStartViewModel, FinishViewModel{
    val text: LiveData<String>

    fun initText(text: String)
    fun onOk()
}

class DialogViewModelImpl(): ViewModelBase(),DialogViewModel{
    override val text = MutableLiveData<String>("")

    override fun initText(text: String) = this.text.postValue(text)
    override fun onOk() {
        startActivity(ActivityStartEvent(ActivityType.OVERLAY_PERMISSION))
        finish(FinishEvent(FinishType.DISMISS))
    }
}