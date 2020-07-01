package com.aks.notpress.ui.purchase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.aks.notpress.ui.offer.SaleOffer
import com.aks.notpress.utils.*

interface PurchaseViewModel: FragmentViewModel, ActivityStartViewModel, PermissionViewModel, FinishViewModel {
    val offers: List<SaleOffer>
    val isHaveBook: LiveData<Boolean>
    val isNextVisible: LiveData<Boolean>

    fun onNext()
    fun setNextVisible(isNextVisible: Boolean)
    fun onUpdate()
    fun onAudionBook()
    fun onPayOffer(text: Int)
}

class PurchaseViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), PurchaseViewModel{
    override val isHaveBook = preferencesBasket.isHaveBook
    override val isNextVisible = MutableLiveData<Boolean>(true)
    override val offers: List<SaleOffer> = listOf(SaleOffer(R.string.for_month, preferencesBasket.textSaleSubMonth, preferencesBasket.textSubMonth),
        SaleOffer(R.string.for_year, preferencesBasket.textSaleSubYear, preferencesBasket.textSubYear),
        SaleOffer(R.string.vip_forever, preferencesBasket.textSaleBookVIP, preferencesBasket.textBookVIP))
    private val isFirstStart: Boolean = preferencesBasket.isFirstStart()
    private val stateSubscription = preferencesBasket.stateSubscription

    init {
        preferencesBasket.billing()
    }

    override fun onAudionBook() = replaceFragment(FragmentEvent(FragmentType.BOOK))

    override fun onUpdate() {
        preferencesBasket.update()
    }

    override fun setNextVisible(isNextVisible: Boolean) { this.isNextVisible.value = isNextVisible }

    override fun onNext() =
        replaceFragment(FragmentEvent(
            when {
                isFirstStart -> FragmentType.PRESENT
                stateSubscription.value == StateSubscription.FREE_MINUTE -> FragmentType.EVERYDAY
                else -> FragmentType.HOME
            }))

    override fun onPayOffer(text: Int) {
        when(text){
            R.string.for_month -> preferencesBasket.launchSaleBillingMonth()
            R.string.for_year -> preferencesBasket.launchSaleBillingYear()
            R.string.vip_forever -> preferencesBasket.launchSaleBillingBookVIP()
        }
    }
}