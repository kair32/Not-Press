package com.aks.notpress.ui.home

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

    fun onFreeDays()
    fun onVarenikClick()
    fun onCheckedChanged(checked: Boolean)
    fun checkPermissionDialog()
}

class HomeViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), HomeViewModel {

    override val daySubscription = MutableLiveData(preferencesBasket.getSubscriptionDay())
    override val isFreeDayVisible = MutableLiveData<Boolean>(preferencesBasket.getIsSubscription())
    override val isHaveSubscription = MutableLiveData<Boolean>(preferencesBasket.getHaveSubscription())
    override val isCheckPermissionOverlay = MutableLiveData<Boolean>(false)
    override val isChecked = MutableLiveData<Boolean>(false)
    override val isClicked = MutableLiveData<Boolean>(false)

    init {
        preferencesBasket.clearPreference()
    }

    override fun onVarenikClick() {
        preferencesBasket.setHaveSubscription(true)
        isHaveSubscription.value = true
    }

    override fun onCheckedChanged(checked: Boolean) {
        isChecked.postValue(checked)
    }

    override fun onFreeDays() {
        preferencesBasket.setIsSubscription()
        isFreeDayVisible.value = false
    }

    override fun onClickTeddy() = replaceFragment(FragmentEvent(FragmentType.PAY))

    override fun checkPermissionDialog() = replaceFragment(DialogFragmentEvent(R.string.warning_permission, this))
    override fun cancelDialog() = isCheckPermissionOverlay.postValue(!isCheckPermissionOverlay.value!!)
}