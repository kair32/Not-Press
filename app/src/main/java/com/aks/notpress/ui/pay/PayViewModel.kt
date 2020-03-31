package com.aks.notpress.ui.pay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.utils.FragmentViewModel
import com.aks.notpress.utils.PreferencesBasket
import com.aks.notpress.utils.ViewModelBase

interface PayViewModel: FragmentViewModel{
    val daySubscription: LiveData<Int>
    val isFreeDayVisible: LiveData<Boolean>

    fun onBaySubscription()
    fun onFreeDays()
}

class PayViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(),PayViewModel{

    override val daySubscription = MutableLiveData(preferencesBasket.getSubscriptionDay())
    override val isFreeDayVisible = MutableLiveData<Boolean>(preferencesBasket.getIsSubscription())

    override fun onBaySubscription() {
    }

    override fun onFreeDays() {
        preferencesBasket.setIsSubscription()
        isFreeDayVisible.value = false
    }

}