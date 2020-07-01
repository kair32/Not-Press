package com.aks.notpress.ui.book

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.utils.*

interface PayBookViewModel: FragmentViewModel, ActivityStartViewModel, PermissionViewModel, FinishViewModel {
    val priceBook: LiveData<String>
    val priceBookVIP: LiveData<String>
    val isHaveBook: LiveData<Boolean>

    fun onPayBook(price: String)
}

class PayBookViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), PayBookViewModel{
    override val priceBook = preferencesBasket.textBook
    override val priceBookVIP = preferencesBasket.textBookVIP
    override val isHaveBook = preferencesBasket.isHaveBook
    init { preferencesBasket.billing() }

    override fun onPayBook(price: String) {
        when(price){
            priceBook.value -> preferencesBasket.launchBillingBook()
            priceBookVIP.value -> preferencesBasket.launchBillingBookVIP()
        }
    }
}
