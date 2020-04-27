package com.aks.notpress.utils

import android.Manifest
import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class PermissionUtil{
    fun observe(owner: LifecycleOwner, viewModel: PermissionViewModel, activity: Activity?) =
        viewModel.permissionLiveData.observe(owner, Observer {
            if (it.listType == null)    checkPermission(activity ?: return@Observer, viewModel.listener?: return@Observer, it)
            else                        checkPermissions(activity ?: return@Observer, viewModel.listeners?: return@Observer, it)
        })

    private fun checkPermission(activity: Activity, listener: PermissionListener, event: PermissionEvent) =
        Dexter.withActivity(activity)
            .withPermission(event.type!!.permission)
            .withListener(listener)
            .check()

    private fun checkPermissions(activity: Activity, listeners: MultiplePermissionsListener, event: PermissionEvent){
        Dexter.withActivity(activity)
            .withPermissions(event.listType!!.map { it.permission })
            .withListener(listeners)
            .check()
    }

}
class PermissionEvent{
    constructor(type: PermissionType): super(){
        this.type = type }
    constructor(listType: List<PermissionType>): super(){
        this.listType = listType }

    var type: PermissionType?=null
    var listType: List<PermissionType>?=null
}

enum class PermissionType(val permission: String) {
    READ_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE),
    WRITE_EXTERNAL_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE)
}