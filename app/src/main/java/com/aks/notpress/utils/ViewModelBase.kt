package com.aks.notpress.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class ViewModelBase(): ViewModel(),
    FragmentViewModel {
    override val fragmentLiveData: MutableLiveData<FragmentEvent> by lazy { MutableLiveData<FragmentEvent>() }

    protected fun replaceFragment(event: FragmentEvent) = fragmentLiveData.postValue(event)
}