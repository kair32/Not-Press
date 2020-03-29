package com.aks.notpress.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class ViewModelBase(): ViewModel(),
    FragmentViewModel, ActivityStartViewModel {
    override val fragmentLiveData: MutableLiveData<FragmentEvent> by lazy { MutableLiveData<FragmentEvent>() }
    override val activityStartLiveData: MutableLiveData<ActivityStartEvent> by lazy { MutableLiveData<ActivityStartEvent>() }

    protected fun startActivity(event: ActivityStartEvent) = activityStartLiveData.postValue(event)
    protected fun replaceFragment(event: FragmentEvent) = fragmentLiveData.postValue(event)
}