
package com.aks.notpress.ui.main

import com.aks.notpress.utils.*

interface MainViewModel: FragmentViewModel

class MainViewModelImpl(
    preferencesBasket: PreferencesBasket
): ViewModelBase(), MainViewModel {
    private val isFirstStart: Boolean = preferencesBasket.isFirstStart()
    private var isHaveHotOffer = preferencesBasket.getHotOffer()

    init {
        replaceFragment(
            when {
                isFirstStart -> FragmentEvent(FragmentType.HELLO)
                isHaveHotOffer -> OfferEvent()
                else -> PurchaseEvent()
            }
        )
    }
}