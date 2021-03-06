package com.aks.notpress.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.aks.notpress.ui.dialog.CustomDialog
import com.aks.notpress.ui.dialog.CustomDialog.*
import com.aks.notpress.ui.dialog.CustomDialogFragment
import com.aks.notpress.utils.*
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

interface HomeViewModel: FragmentViewModel, ActivityStartViewModel, TeddyViewModel, PermissionViewModel,
    CustomDialogFragment.CallBack {
    val isGrantedPermission: LiveData<Boolean>
    val stateSubscription: LiveData<StateSubscription>
    val daySubscription: LiveData<Int>
    val isCheckPermissionOverlay: LiveData<Boolean>
    val isChecked: LiveData<Boolean>
    val isClicked: LiveData<Boolean>
    val isHaveBook: LiveData<Boolean>
    val isNotVIP: LiveData<Boolean>
    var textFreeDay: String

    fun initChecked(isCheck: Boolean)
    fun onUpdate()
    fun onUpdateCheck()
    fun onFreeDays()
    fun onVarenikClick()
    fun onListenBook()
    fun onButterflyClick()
    fun onCheckedChanged(checked: Boolean)
    fun checkPermissionDialog()
    fun checkGrantedPermissionDialog()
    fun onOpenPromoCode()
}

class HomeViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), HomeViewModel, MultiplePermissionsListener {

    override var textFreeDay: String =""
    override val isHaveBook = preferencesBasket.isHaveBook
    override val stateSubscription = preferencesBasket.stateSubscription
    override val daySubscription = preferencesBasket.freeDay
    override val isNotVIP = preferencesBasket.isNotVIP
    override val isCheckPermissionOverlay = MutableLiveData<Boolean>(false)
    override val isGrantedPermission = MutableLiveData<Boolean>(false)
    override val isChecked = MutableLiveData<Boolean>(false)
    override val isClicked = MutableLiveData<Boolean>(true)

    private var state = DIALOG_ONE
    init {
        preferencesBasket.billing()
        onUpdate()
        listeners = this
    }

    override fun initChecked(isCheck: Boolean) {
        Log.d("BindingAdapter","initChecked = $isCheck")
        isChecked.value = isCheck

        if (stateSubscription.value == StateSubscription.FREE_DAY ||
            stateSubscription.value == StateSubscription.FREE_DAY_LARGE ||
            stateSubscription.value == StateSubscription.FREE_MINUTE)
            replaceFragment(DialogFragmentEvent(null, textFreeDay))
    }
    override fun onVarenikClick() = startActivity(ActivityStartEvent(ActivityType.OPEN_INSTAGRAM))
    override fun onListenBook() = startActivity(ActivityStartEvent(ActivityType.OPEN_BOOK))

    override fun onButterflyClick() = startActivity(ActivityStartEvent(ActivityType.VIDEO))

    override fun onOpenPromoCode() = startActivity(ActivityStartEvent(ActivityType.OPEN_PROMO_CODE))

    override fun onCheckedChanged(checked: Boolean){
        Log.d("BindingAdapter","onCheckedChanged = $checked")
        isChecked.value = checked
    }

    override fun onFreeDays(){
        preferencesBasket.startFreeDay()
        onUpdate()
    }

    override fun onClickTeddy() = replaceFragment(if (preferencesBasket.getHotOffer()) OfferEvent(false) else PurchaseEvent(false))

    override fun onUpdateCheck() {
        isClicked.value = true
        if (stateSubscription.value == StateSubscription.ENDED) {
            isClicked.value = false
            replaceFragment(DialogFragmentEvent(R.string.error_subscription))
        }
        if (stateSubscription.value == StateSubscription.FREE_MINUTE && preferencesBasket.getFreeMinute() < 15000L){
            isClicked.value = false
            isChecked.value = false
            Log.d("BindingAdapter","onUpdateCheck = false")
            replaceFragment(DialogFragmentEvent(R.string.error_free_time))
        }
    }
    override fun onUpdate() {
        preferencesBasket.update()
        onUpdateCheck()
    }

    override fun checkPermissionDialog() = replaceFragment(DialogFragmentEvent(R.string.warning_permission, callBack = this, state = DIALOG_TWO))

    override fun checkGrantedPermissionDialog() = replaceFragment(DialogFragmentEvent(R.string.warning_permission, callBack = this, state = state))
    override fun cancelDialog(state: CustomDialog?) {
        when(state){
            DIALOG_ONE -> isGrantedPermission.postValue(!isGrantedPermission.value!!)
            DIALOG_TWO -> isCheckPermissionOverlay.postValue(!isCheckPermissionOverlay.value!!)
            DIALOG_THREE -> checkGrantedPermissionDialog()
            null -> return
        }
    }
    override fun okDialog(state: CustomDialog?){
        when(state){
            DIALOG_ONE      -> checkPermission(PermissionEvent(listOf(PermissionType.READ_STORAGE, PermissionType.WRITE_EXTERNAL_STORAGE)))
            DIALOG_TWO      -> startActivity(ActivityStartEvent(ActivityType.OVERLAY_PERMISSION))
            DIALOG_THREE    -> startActivity(ActivityStartEvent(ActivityType.OPEN_SETTING))
            null -> return
        }
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        if (report?.isAnyPermissionPermanentlyDenied == true) state = DIALOG_THREE
        report?.deniedPermissionResponses?.let { isGrantedPermission.postValue(!isGrantedPermission.value!!) }
    }
    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) = token?.continuePermissionRequest() ?: Unit
}