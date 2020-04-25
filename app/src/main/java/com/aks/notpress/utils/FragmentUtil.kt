package com.aks.notpress.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.aks.notpress.R
import com.aks.notpress.ui.dialog.CustomDialogFragment
import com.aks.notpress.ui.home.FragmentHome
import com.aks.notpress.ui.password.PasswordFragment
import com.aks.notpress.ui.pay.PayFragment
import com.aks.notpress.utils.FragmentType.*


class FragmentUtil {
    fun observe(owner: LifecycleOwner, viewModel: FragmentViewModel, manager: FragmentManager?,
                consumer: (FragmentEvent) -> Unit = {}) {
        viewModel.fragmentLiveData.observe(owner, Observer {
            manager ?: return@Observer
            if (it.isRemove) removeFragment(manager, it)
            else replaceFragment(manager, it)
        })
    }
    private fun replaceFragment(manager: FragmentManager, event: FragmentEvent) {
        val type = event.type
        val fragment = createFragment(event) ?: return

        if (fragment is DialogFragment) return fragment.show(manager, type.name)
        val transaction = manager.beginTransaction()
        when(event.type.animation){
            AnimationType.BOTTOM_TO_TOP-> {  if (event.isBack)transaction.setCustomAnimations(R.anim.slide_in_bottom,R.anim.slide_out_bottom, R.anim.slide_in_bottom,R.anim.slide_out_bottom)
            else  transaction.setCustomAnimations(R.anim.slide_in_bottom,R.anim.slide_out_bottom)}
            AnimationType.NONE -> {}
        }

        if (event.isAdd) transaction.add(type.id, fragment, event.type.name)
        else transaction.replace(type.id, fragment, event.type.name)

        if (type.addToStack) {
            transaction.addToBackStack(null)
        } else {
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        transaction.commit()
    }
    //creat
    private fun createFragment(event: FragmentEvent) = when (event) {
        is DialogFragmentEvent -> CustomDialogFragment.newInstance(event.resText, event.text, event.callBack)
        else -> when(event.type) {
            HOME        -> FragmentHome.newInstance()
            PAY         -> PayFragment.newInstance()
            PASSWORD    -> PasswordFragment.newInstance()
            else                    -> null
        }
    }

    private fun removeFragment(manager: FragmentManager, event: FragmentEvent) {
        val fragment = manager.findFragmentByTag(event.type.name) ?: return
        manager.beginTransaction().remove(fragment).commit()
    }
}

open class FragmentEvent(
    val type: FragmentType,
    val isRemove: Boolean = false,
    val isAdd: Boolean = false,
    val isRecreate: Boolean? = true,
    val isBack: Boolean = true
)

class DialogFragmentEvent(val resText: Int? = null, val text: String? = null, val callBack: CustomDialogFragment.CallBack? = null) :
    FragmentEvent(DIALOG)

enum class FragmentType(val id: Int = R.id.container,
                        val addToStack: Boolean = true,
                        val animation: AnimationType = AnimationType.BOTTOM_TO_TOP
) {
    HOME(addToStack = false),
    PAY,
    PASSWORD,
    DIALOG,

    DEFAULT
}
enum class AnimationType{
    NONE,
    BOTTOM_TO_TOP
}