package com.aks.notpress.ui.present

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.aks.notpress.ui.dialog.CustomDialog
import com.aks.notpress.ui.dialog.CustomDialogFragment
import com.aks.notpress.utils.*
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

interface PresentViewModel: FragmentViewModel, PermissionViewModel, ActivityStartViewModel,
    CustomDialogFragment.CallBack{
    val isGrantedPermission: LiveData<Boolean>

    fun checkGrantedPermissionDialog()
    fun onNext()
}

class PresentViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), PresentViewModel, MultiplePermissionsListener {
    override val isGrantedPermission = MutableLiveData<Boolean>(false)

    override var listeners: MultiplePermissionsListener? = this
    private var state = CustomDialog.DIALOG_ONE

    override fun onNext() {
        preferencesBasket.setFirst()
        preferencesBasket.startFreeDay()
        replaceFragment(FragmentEvent(FragmentType.HOME))
    }
    override fun cancelDialog(state: CustomDialog?) {
        when(state){
            CustomDialog.DIALOG_ONE -> isGrantedPermission.postValue(!isGrantedPermission.value!!)
            CustomDialog.DIALOG_THREE -> checkGrantedPermissionDialog()
            null -> return
        }
    }
    override fun okDialog(state: CustomDialog?){
        when(state){
            CustomDialog.DIALOG_ONE -> checkPermission(PermissionEvent(listOf(PermissionType.READ_STORAGE, PermissionType.WRITE_EXTERNAL_STORAGE)))
            CustomDialog.DIALOG_THREE -> startActivity(ActivityStartEvent(ActivityType.OPEN_SETTING))
            null -> return
        }
    }
    override fun checkGrantedPermissionDialog() = replaceFragment(DialogFragmentEvent(R.string.warning_permission, callBack = this, state = state))
    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        if (report?.isAnyPermissionPermanentlyDenied == true) state = CustomDialog.DIALOG_THREE
        report?.deniedPermissionResponses?.let { isGrantedPermission.postValue(!isGrantedPermission.value!!) }
    }
    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) = token?.continuePermissionRequest() ?: Unit
}