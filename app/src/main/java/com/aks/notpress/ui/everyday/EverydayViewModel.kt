package com.aks.notpress.ui.everyday

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.utils.*

interface EverydayViewModel: FragmentViewModel, ActivityStartViewModel, PermissionViewModel{
    val checked: LiveData<Int>

    fun onNext()
}

class EverydayViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), EverydayViewModel{
    override val checked = MutableLiveData<Int>(preferencesBasket.getEveryDayCount())

    override fun onNext() {
        preferencesBasket.setEveryDayCount(checked.value?:0)
        replaceFragment(FragmentEvent(FragmentType.HOME))
    }
}