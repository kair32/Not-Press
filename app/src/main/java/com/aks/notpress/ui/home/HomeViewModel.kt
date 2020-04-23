package com.aks.notpress.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.aks.notpress.ui.dialog.CustomDialogFragment
import com.aks.notpress.utils.*

interface HomeViewModel: FragmentViewModel, ActivityStartViewModel, TeddyViewModel,
    CustomDialogFragment.CallBack {
    val daySubscription: LiveData<Int>
    val isFreeDayVisible: LiveData<Boolean>
    val isHaveSubscription: LiveData<Boolean>
    val isCheckPermissionOverlay: LiveData<Boolean>
    val isChecked: LiveData<Boolean>
    val isClicked: LiveData<Boolean>

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

    override val daySubscription = MutableLiveData(preferencesBasket.getSubscriptionDay())
    override val isFreeDayVisible = preferencesBasket.isSubscription
    override val isHaveSubscription = preferencesBasket.isHaveSubscription
    override val isCheckPermissionOverlay = MutableLiveData<Boolean>(false)
    override val isChecked = MutableLiveData<Boolean>(false)
    override val isClicked = MutableLiveData<Boolean>(true)

    init {
        preferencesBasket.billing()
        onUpdate()
    }

    override fun initChecked(isCheck: Boolean) = isChecked.postValue(isCheck)
    override fun onVarenikClick() = startActivity(ActivityStartEvent(ActivityType.OPEN_INSTAGRAM))
    override fun onButterflyClick() = startActivity(ActivityStartEvent(ActivityType.OPEN_YOUTUBE))

    override fun onCheckedChanged(checked: Boolean) {
        isChecked.postValue(checked)
    }

    override fun onFreeDays() {
        preferencesBasket.setIsSubscription()
        isFreeDayVisible.value = false
    }

    override fun onClickTeddy() {
        replaceFragment(FragmentEvent(FragmentType.PAY))
    }

    override fun onUpdateCheck() {
        if (isHaveSubscription.value != true) {
            isClicked.value = daySubscription.value ?: 0 > 0
            if (daySubscription.value?: 0 <= 0) {
                replaceFragment(DialogFragmentEvent(R.string.error_subscription))
            }
        }
    }
    override fun onUpdate() {
        preferencesBasket.update()
        onUpdateCheck()
    }

    override fun checkPermissionDialog() = replaceFragment(DialogFragmentEvent(R.string.warning_permission, this))
    override fun cancelDialog() = isCheckPermissionOverlay.postValue(!isCheckPermissionOverlay.value!!)
    override fun okDialog() = startActivity(ActivityStartEvent(ActivityType.OVERLAY_PERMISSION))
}