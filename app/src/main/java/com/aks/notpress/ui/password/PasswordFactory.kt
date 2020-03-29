package com.aks.notpress.ui.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.utils.PreferencesBasket

class PasswordFactory(
    private val preferencesBasket: PreferencesBasket) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordViewModelImpl::class.java)) {
            return PasswordViewModelImpl(preferencesBasket) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}