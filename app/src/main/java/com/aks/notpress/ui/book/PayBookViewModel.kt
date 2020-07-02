package com.aks.notpress.ui.book

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.utils.*

interface PayBookViewModel: FragmentViewModel, ActivityStartViewModel, PermissionViewModel, FinishViewModel {
    val priceBook: LiveData<String>
    val priceBookVIP: LiveData<String>
    val priceSaleBookVIP: LiveData<String>
    val isHaveBook: LiveData<Boolean>
    var isSale: Boolean

    fun onPayBook(price: String)
}

class PayBookViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), PayBookViewModel{
    override val priceBook = preferencesBasket.textBook
    override val priceBookVIP = preferencesBasket.textBookVIP
    override val priceSaleBookVIP = preferencesBasket.textSaleBookVIP
    override val isHaveBook = preferencesBasket.isHaveBook
    override var isSale: Boolean = false
    init { preferencesBasket.billing() }

    override fun onPayBook(price: String) {
        when(price){
            priceBook.value -> preferencesBasket.launchBillingBook()
            priceBookVIP.value -> preferencesBasket.launchBillingBookVIP()
            priceSaleBookVIP.value -> preferencesBasket.launchSaleBillingBookVIP()
        }
    }
}
