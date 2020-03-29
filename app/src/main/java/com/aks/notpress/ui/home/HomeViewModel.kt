package com.aks.notpress.ui.home

import com.aks.notpress.utils.*

interface HomeViewModel: FragmentViewModel, ActivityStartViewModel, TeddyViewModel {
    fun onPassword()
}

class HomeViewModelImpl: ViewModelBase(),
    HomeViewModel {
    override fun onPassword() = startActivity(ActivityStartEvent(ActivityType.OVERLAY_PERMISSION))// replaceFragment(FragmentEvent(FragmentType.PASSWORD))

    override fun onClickTeddy() = replaceFragment(FragmentEvent(FragmentType.PAY))
}