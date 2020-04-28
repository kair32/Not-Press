package com.aks.notpress.utils

import androidx.lifecycle.LiveData
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

interface FragmentViewModel {
    val fragmentLiveData: LiveData<FragmentEvent>
}

interface TeddyViewModel{
    fun onClickTeddy()
}

interface ActivityStartViewModel{
    val activityStartLiveData: LiveData<ActivityStartEvent>
}

interface FinishViewModel{
    val finishLiveData: LiveData<FinishEvent>
    fun onBackPressed()
}

interface PermissionViewModel{
    var listener: PermissionListener?
    var listeners: MultiplePermissionsListener?
    val permissionLiveData: LiveData<PermissionEvent>
}