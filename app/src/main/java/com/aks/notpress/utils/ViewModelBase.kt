package com.aks.notpress.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class ViewModelBase(): ViewModel(),
    FragmentViewModel, ActivityStartViewModel, FinishViewModel {
    override val fragmentLiveData: MutableLiveData<FragmentEvent> by lazy { MutableLiveData<FragmentEvent>() }
    override val activityStartLiveData: MutableLiveData<ActivityStartEvent> by lazy { MutableLiveData<ActivityStartEvent>() }
    override val finishLiveData: MutableLiveData<FinishEvent> by lazy { MutableLiveData<FinishEvent>() }

    protected fun startActivity(event: ActivityStartEvent) = activityStartLiveData.postValue(event)
    protected fun replaceFragment(event: FragmentEvent) = fragmentLiveData.postValue(event)
    protected fun finish(event: FinishEvent) = finishLiveData.postValue(event)
    override fun onBackPressed() = finishLiveData.postValue(FinishEvent(FinishType.BACK))
}