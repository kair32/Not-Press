package com.aks.notpress.utils

import androidx.lifecycle.LiveData

interface FragmentViewModel {
    val fragmentLiveData: LiveData<FragmentEvent>
}

interface TeddyViewModel{
    fun onClickTeddy()
}

interface ActivityStartViewModel{
    val activityStartLiveData: LiveData<ActivityStartEvent>
}