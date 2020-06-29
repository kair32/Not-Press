package com.aks.notpress.ui.offer

import android.util.Log
import androidx.lifecycle.LiveData
import com.aks.notpress.R
import com.aks.notpress.utils.*

class SaleOffer(val text: Int,
           val price: LiveData<String>,
           val oldPrice: LiveData<String>
)

interface OfferViewModel: FragmentViewModel, ActivityStartViewModel, PermissionViewModel, FinishViewModel{
    val offers: List<SaleOffer>

    fun onAudionBook()
    fun onPayOffer(text: Int)
}

class OfferViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), OfferViewModel{
    override val offers: List<SaleOffer> = listOf(SaleOffer(R.string.for_month, preferencesBasket.textSaleSubMonth, preferencesBasket.textSubMonth),
        SaleOffer(R.string.for_year, preferencesBasket.textSaleSubYear, preferencesBasket.textSubYear),
        SaleOffer(R.string.vip_forever, preferencesBasket.textSaleBookVIP, preferencesBasket.textBookVIP))

    init {
        preferencesBasket.billing()
    }

    override fun onAudionBook() = replaceFragment(FragmentEvent(FragmentType.BOOK))

    override fun onPayOffer(text: Int) {
        when(text){
            R.string.for_month -> preferencesBasket.launchSaleBillingMonth()
            R.string.for_year -> preferencesBasket.launchSaleBillingYear()
            R.string.vip_forever -> preferencesBasket.launchSaleBillingBookVIP()
        }
    }
}