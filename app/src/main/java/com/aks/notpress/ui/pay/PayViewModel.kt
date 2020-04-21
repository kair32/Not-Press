package com.aks.notpress.ui.pay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.utils.FragmentViewModel
import com.aks.notpress.utils.PreferencesBasket
import com.aks.notpress.utils.ViewModelBase

interface PayViewModel: FragmentViewModel{
    val daySubscription: LiveData<Int>
    val isFreeDayVisible: LiveData<Boolean>
    val isHaveSubscription: LiveData<Boolean>

    fun onBaySubscription()
    fun onFreeDays()
}

class PayViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(),PayViewModel{

    override val isHaveSubscription = preferencesBasket.isHaveSubscription
    override val daySubscription = MutableLiveData(preferencesBasket.getSubscriptionDay())
    override val isFreeDayVisible = preferencesBasket.isSubscription

    init {
        preferencesBasket.billing()
    }

    override fun onBaySubscription() {
        preferencesBasket.launchBillingMonth()
    }

    override fun onFreeDays() {
        preferencesBasket.setIsSubscription()
        isFreeDayVisible.value = false
    }

}