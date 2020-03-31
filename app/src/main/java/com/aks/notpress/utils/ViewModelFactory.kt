package com.aks.notpress.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aks.notpress.ui.password.PasswordViewModelImpl
import java.security.Provider

class ViewModelFactory(private val clazz: Any)
    : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(clazz::class.java).newInstance(clazz)
    }
}