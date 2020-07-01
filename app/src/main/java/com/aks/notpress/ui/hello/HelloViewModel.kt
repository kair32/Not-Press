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

    override fun onNext() {
        replaceFragment(FragmentEvent(if (isHaveHotOffer) FragmentType.OFFER else FragmentType.PURCHASE))
    }

    override fun updateHaveOffer() {
        isHaveHotOffer = preferencesBasket.getHotOffer()
    }
}