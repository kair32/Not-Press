package com.aks.notpress.ui.password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.aks.notpress.utils.FragmentViewModel
import com.aks.notpress.utils.PasswordEnterViewModel
import com.aks.notpress.utils.PreferencesBasket
import com.aks.notpress.utils.ViewModelBase

interface PasswordViewModel: FragmentViewModel, PasswordEnterViewModel{
    val textTitle: LiveData<Int>
    val isSaveVisible: LiveData<Boolean>

    fun onSave()
    fun onDelete()
}

class PasswordViewModelImpl(val preferencesBasket: PreferencesBasket): ViewModelBase(), PasswordViewModel{
    override val textTitle = MutableLiveData<Int>(R.string.enter_password)
    override val isSaveVisible = MutableLiveData<Boolean>(false)
    override var passwordItems: List<Password> = (0..9).flatMap { listOf(Password())}

    override val color = MutableLiveData<Int>(android.R.color.white)

    private var password:List<Boolean> = preferencesBasket.getPassword()

    override fun onCheck(password: List<Boolean>) {
        preferencesBasket.setPassword(password)
    }

    override fun onNext() {
        textTitle.postValue(R.string.repeat_image)
        isSaveVisible.postValue(true)
    }

    override fun onSave() {}

    override fun onDelete() {
        textTitle.postValue(R.string.enter_password)
        isSaveVisible.postValue(false)
    }

    override fun initPasswordList(passwordItems: List<Password>) { this.passwordItems = passwordItems }
}