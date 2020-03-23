package com.aks.notpress.utils

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.aks.notpress.R
import com.aks.notpress.ui.home.FragmentHome


class FragmentUtil() {
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

        //if (fragment is DialogFragment) return fragment.show(manager, type.name)
        val transaction = manager.beginTransaction()
        when(event.type.animation){
            AnimationType.BOTTOM_TO_TOP -> {  if (event.isBack)transaction.setCustomAnimations(
                R.anim.slide_out_bottom,
                R.anim.slide_out_bottom
            )
            else  transaction.setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.slide_in_bottom
            )}
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

        else -> when(event.type) {
            FragmentType.HOME -> FragmentHome.newInstance()
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

enum class FragmentType(val title: Int = -1,
                        val id: Int = R.id.container,
                        val addToStack: Boolean = true,
                        val animation: AnimationType = AnimationType.NONE
) {
    HOME,

    DEFAULT
}
enum class AnimationType{
    NONE,
    BOTTOM_TO_TOP
}