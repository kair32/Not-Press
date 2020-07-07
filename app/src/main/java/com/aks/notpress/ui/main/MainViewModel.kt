
package com.aks.notpress.ui.main

import com.aks.notpress.utils.*

interface MainViewModel: FragmentViewModel

class MainViewModelImpl(
    preferencesBasket: PreferencesBasket
): ViewModelBase(), MainViewModel {
    private val isFirstStart: Boolean = preferencesBasket.isFirstStart()
    private var isHaveHotOffer = preferencesBasket.getHotOffer()
    private val stateSubscription = preferencesBasket.stateSubscription

    init {
        replaceFragment(
            when {
                stateSubscription.value == StateSubscription.HAVE_SUB -> FragmentEvent(FragmentType.HOME)
                isFirstStart -> FragmentEvent(FragmentType.HELLO)
                isHaveHotOffer -> OfferEvent()
                else -> PurchaseEvent()
            }
        )
    }
}