
package com.aks.notpress.ui.main

import com.aks.notpress.utils.FragmentEvent
import com.aks.notpress.utils.FragmentType
import com.aks.notpress.utils.FragmentViewModel
import com.aks.notpress.utils.ViewModelBase

interface MainViewModel: FragmentViewModel

class MainViewModelImpl: ViewModelBase(),
    MainViewModel {
    init {
        replaceFragment(FragmentEvent(FragmentType.HOME))
    }
}