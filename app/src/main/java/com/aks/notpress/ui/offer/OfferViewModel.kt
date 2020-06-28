package com.aks.notpress.ui.offer

import com.aks.notpress.utils.*

interface OfferViewModel: FragmentViewModel, ActivityStartViewModel, PermissionViewModel,
    FinishViewModel

class OfferViewModelImpl(
    private val preferencesBasket: PreferencesBasket
): ViewModelBase(), OfferViewModel{}