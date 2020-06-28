package com.aks.notpress.ui.everyday

import com.aks.notpress.utils.*

interface EverydayViewModel: FragmentViewModel, ActivityStartViewModel, PermissionViewModel{

}

class EverydayViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), EverydayViewModel{

}