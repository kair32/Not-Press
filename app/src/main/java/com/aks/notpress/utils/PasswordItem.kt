package com.aks.notpress.utils

import androidx.lifecycle.LiveData
import com.aks.notpress.ui.password.Password


interface PasswordEnterViewModel{
    val passwordItems: List<Password>
    val color: LiveData<Int>

    fun initPasswordList(passwordItems: List<Password>)
    fun onCheck(password: List<Boolean>)
    fun onNext()
}