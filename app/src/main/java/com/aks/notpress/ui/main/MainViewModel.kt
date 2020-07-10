
package com.aks.notpress.ui.main

import com.aks.notpress.utils.*

interface MainViewModel: FragmentViewModel

class MainViewModelImpl(
    preferencesBasket: PreferencesBasket
): ViewModelBase(), MainViewModel {
    private val isFirstStart: Boolean = preferencesBasket.isFirstStart()
    private var isHaveHotOffer = preferencesBasket.getHotOffer()
    private val stateSubscription = preferencesBasket.stateSubscription
    private val freeDay = preferencesBasket.freeDay

    init {
        replaceFragment(
            when {
                //Акция за сутки до окончания 30 дней
                stateSubscription.value == StateSubscription.FREE_DAY_LARGE && freeDay.value?:0 <= 1 -> {
                    if (!preferencesBasket.isWasLastDayInHotOffer()){
                        preferencesBasket.setHotOffer(true)
                        preferencesBasket.startHotOffer()
                        OfferEvent()
                    }
                    else PurchaseEvent()
                }
                stateSubscription.value == StateSubscription.HAVE_SUB -> FragmentEvent(FragmentType.HOME)
                isFirstStart -> FragmentEvent(FragmentType.HELLO)
                isHaveHotOffer -> OfferEvent()
                else -> PurchaseEvent()
            }
        )
    }
}