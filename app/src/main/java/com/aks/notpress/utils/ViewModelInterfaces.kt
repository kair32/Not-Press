package com.aks.notpress.utils

import androidx.lifecycle.LiveData
import com.aks.notpress.utils.FragmentEvent

interface FragmentViewModel {
    val fragmentLiveData: LiveData<FragmentEvent>
}
interface TeddyViewModel{
    fun onClickTeddy()
}