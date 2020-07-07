package com.aks.notpress.ui.offer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.aks.notpress.utils.*

class SaleOffer(val text: Int,
           val price: LiveData<String>,
           val oldPrice: LiveData<String>
)

interface OfferViewModel: FragmentViewModel, ActivityStartViewModel, PermissionViewModel, FinishViewModel{
    val offers: List<SaleOffer>
    val offerTime: Long
    val isHaveBook: LiveData<Boolean>
    val isNextVisible: LiveData<Boolean>

    fun onNext()
    fun onOpenPromoCode()
    fun setNextVisible(isNextVisible: Boolean)
    fun onAudionBook()
    fun onPayOffer(text: Int)
}

class OfferViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), OfferViewModel{
    override val offerTime = preferencesBasket.getHotOfferTime()
    override val isHaveBook = preferencesBasket.isHaveBook
    override val isNextVisible = MutableLiveData<Boolean>(true)
    override val offers: List<SaleOffer> = listOf(SaleOffer(R.string.for_month, preferencesBasket.textSaleSubMonth, preferencesBasket.textSubMonth),
        SaleOffer(R.string.for_year, preferencesBasket.textSaleSubYear, preferencesBasket.textSubYear),
        SaleOffer(R.string.vip_forever, preferencesBasket.textSaleBookVIP, preferencesBasket.textBookVIP))
    private val isFirstStart: Boolean = preferencesBasket.isFirstStart()
    private val stateSubscription = preferencesBasket.stateSubscription
    init {
        preferencesBasket.getFreeDay()
        preferencesBasket.billing()
    }

    override fun onAudionBook() = replaceFragment(PayBookEvent(true))

    override fun onNext() =
        replaceFragment(FragmentEvent(
            when {
                isFirstStart -> FragmentType.PRESENT
                stateSubscription.value == StateSubscription.FREE_MINUTE -> FragmentType.EVERYDAY
                else -> FragmentType.HOME
            }))

    override fun setNextVisible(isNextVisible: Boolean) { this.isNextVisible.value = isNextVisible }

    override fun onOpenPromoCode() = startActivity(ActivityStartEvent(ActivityType.OPEN_PROMO_CODE))

    override fun onPayOffer(text: Int) {
        when(text){
            R.string.for_month -> preferencesBasket.launchSaleBillingMonth()
            R.string.for_year -> preferencesBasket.launchSaleBillingYear()
            R.string.vip_forever -> preferencesBasket.launchSaleBillingBookVIP()
        }
    }
}