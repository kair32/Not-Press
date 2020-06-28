package com.aks.notpress.ui.book

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.utils.*

interface PayBookViewModel: FragmentViewModel, ActivityStartViewModel, PermissionViewModel {
    val priceBook: LiveData<String>
    val priceVIP: LiveData<String>
}

class PayBookViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), PayBookViewModel{
    override val priceBook = MutableLiveData<String>("")
    override val priceVIP = MutableLiveData<String>("")
}