package com.aks.notpress.ui.pay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.utils.FragmentViewModel
import com.aks.notpress.utils.PreferencesBasket
import com.aks.notpress.utils.StateSubscription
import com.aks.notpress.utils.ViewModelBase

interface PayViewModel: FragmentViewModel{
    val stateSubscription: LiveData<StateSubscription>
    val textSubMonth: LiveData<String>
    val textSubYear: LiveData<String>
    val daySubscription: LiveData<Int>

    fun onBaySubMonth()
    fun onBaySubYear()
    fun onFreeDays()
}

class PayViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(),PayViewModel{
    override val stateSubscription = preferencesBasket.stateSubscription
    override val daySubscription = preferencesBasket.freeDay

    override val textSubMonth = preferencesBasket.textSubMonth
    override val textSubYear = preferencesBasket.textSubYear
    init {
        preferencesBasket.billing()
    }

    override fun onBaySubMonth() = preferencesBasket.launchBillingMonth()
    override fun onBaySubYear() = preferencesBasket.launchBillingYear()

    override fun onFreeDays() = preferencesBasket.startFreeDay()

}