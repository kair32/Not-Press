package com.aks.notpress.ui.home

import com.aks.notpress.utils.*

interface HomeViewModel: FragmentViewModel, TeddyViewModel {
}

class HomeViewModelImpl: ViewModelBase(),
    HomeViewModel {

    override fun onClickTeddy() = replaceFragment(FragmentEvent(FragmentType.PAY))
}