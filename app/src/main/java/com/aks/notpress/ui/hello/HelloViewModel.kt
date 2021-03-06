package com.aks.notpress.ui.hello

import com.aks.notpress.utils.*

interface HelloViewModel: FragmentViewModel{
    fun onNext()
    fun updateHaveOffer()
}

class HelloViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), HelloViewModel{
    private var isHaveHotOffer = preferencesBasket.getHotOffer()

    override fun onNext() = replaceFragment(if (isHaveHotOffer) OfferEvent() else PurchaseEvent())

    override fun updateHaveOffer() {
        isHaveHotOffer = preferencesBasket.getHotOffer()
    }
}