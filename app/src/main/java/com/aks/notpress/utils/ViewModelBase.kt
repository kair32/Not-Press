package com.aks.notpress.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

abstract class ViewModelBase(): ViewModel(),
    FragmentViewModel, ActivityStartViewModel, FinishViewModel, PermissionViewModel {
    override val fragmentLiveData: MutableLiveData<FragmentEvent> by lazy { MutableLiveData<FragmentEvent>() }
    override val activityStartLiveData: MutableLiveData<ActivityStartEvent> by lazy { MutableLiveData<ActivityStartEvent>() }
    override val finishLiveData: MutableLiveData<FinishEvent> by lazy { MutableLiveData<FinishEvent>() }
    override val permissionLiveData: MutableLiveData<PermissionEvent> by lazy { MutableLiveData<PermissionEvent>() }
    override var listener: PermissionListener? = null
    override var listeners: MultiplePermissionsListener? = null

    protected fun startActivity(event: ActivityStartEvent) = activityStartLiveData.postValue(event)
    protected fun replaceFragment(event: FragmentEvent) = fragmentLiveData.postValue(event)
    protected fun finish(event: FinishEvent) = finishLiveData.postValue(event)
    override fun onBackPressed() = finishLiveData.postValue(FinishEvent(FinishType.BACK))
    protected fun checkPermission(event: PermissionEvent) = permissionLiveData.postValue(event)
}