package com.aks.notpress.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.aks.notpress.ui.dialog.CustomDialogFragment
import com.aks.notpress.utils.*

interface HomeViewModel: FragmentViewModel, ActivityStartViewModel, TeddyViewModel,
    CustomDialogFragment.CallBack {
    val stateSubscription: LiveData<StateSubscription>
    val daySubscription: LiveData<Int>
    val isCheckPermissionOverlay: LiveData<Boolean>
    val isChecked: LiveData<Boolean>
    val isClicked: LiveData<Boolean>
    var textFreeDay: String

    fun initChecked(isCheck: Boolean)
    fun onUpdate()
    fun onUpdateCheck()
    fun onFreeDays()
    fun onVarenikClick()
    fun onButterflyClick()
    fun onCheckedChanged(checked: Boolean)
    fun checkPermissionDialog()
}

class HomeViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), HomeViewModel {

    override var textFreeDay: String =""
    override val stateSubscription = preferencesBasket.stateSubscription
    override val daySubscription = MutableLiveData(preferencesBasket.getFreeDay())
    override val isCheckPermissionOverlay = MutableLiveData<Boolean>(false)
    override val isChecked = MutableLiveData<Boolean>(false)
    override val isClicked = MutableLiveData<Boolean>(true)

    init {
        preferencesBasket.billing()
        onUpdate()
    }

    override fun initChecked(isCheck: Boolean) {
        isChecked.postValue(isCheck)

        if (stateSubscription.value == StateSubscription.FREE_DAY)
            replaceFragment(DialogFragmentEvent(null, textFreeDay))
    }
    override fun onVarenikClick() = startActivity(ActivityStartEvent(ActivityType.OPEN_INSTAGRAM))
    override fun onButterflyClick() = startActivity(ActivityStartEvent(ActivityType.VIDEO))

    override fun onCheckedChanged(checked: Boolean) {
        isChecked.postValue(checked)
    }

    override fun onFreeDays() = preferencesBasket.startFreeDay()

    override fun onClickTeddy() {
        replaceFragment(FragmentEvent(FragmentType.PAY))
    }

    override fun onUpdateCheck() {
        isClicked.value = true
        if (stateSubscription.value == StateSubscription.ENDED) {
            isClicked.value = false
            replaceFragment(DialogFragmentEvent(R.string.error_subscription))
        }
    }
    override fun onUpdate() {
        preferencesBasket.update()
        onUpdateCheck()
    }

    override fun checkPermissionDialog() = replaceFragment(DialogFragmentEvent(R.string.warning_permission, callBack = this))
    override fun cancelDialog() = isCheckPermissionOverlay.postValue(!isCheckPermissionOverlay.value!!)
    override fun okDialog() = startActivity(ActivityStartEvent(ActivityType.OVERLAY_PERMISSION))
}