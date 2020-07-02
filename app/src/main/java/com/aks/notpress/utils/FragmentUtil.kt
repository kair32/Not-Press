package com.aks.notpress.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.aks.notpress.R
import com.aks.notpress.ui.book.PayBookFragment
import com.aks.notpress.ui.dialog.CustomDialog
import com.aks.notpress.ui.dialog.CustomDialogFragment
import com.aks.notpress.ui.everyday.EverydayFragment
import com.aks.notpress.ui.hello.HelloFragment
import com.aks.notpress.ui.home.FragmentHome
import com.aks.notpress.ui.offer.OfferFragment
import com.aks.notpress.ui.password.PasswordFragment
import com.aks.notpress.ui.pay.PayFragment
import com.aks.notpress.ui.present.PresentFragment
import com.aks.notpress.ui.purchase.PurchaseFragment
import com.aks.notpress.utils.FragmentType.*


class FragmentUtil {
    fun observe(owner: LifecycleOwner, viewModel: FragmentViewModel, manager: FragmentManager?,
                consumer: (FragmentEvent) -> Unit = {}) {
        viewModel.fragmentLiveData.observe(owner, Observer {
            manager ?: return@Observer
            findFragment(manager, it).takeIf { it }?.run {return@Observer }
            if (it.isRemove) removeFragment(manager, it)
            else replaceFragment(manager, it)
        })
    }

    private fun findFragment(manager: FragmentManager, event: FragmentEvent) : Boolean = manager.findFragmentByTag(event.type.name)?.run{ true }?:false

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
        is DialogFragmentEvent  -> CustomDialogFragment.newInstance(event.resText, event.text, event.state, event.callBack)
        is PurchaseEvent        -> PurchaseFragment.newInstance(event.isNextVisible)
        is OfferEvent           -> OfferFragment.newInstance(event.isNextVisible)
        is PayBookEvent         -> PayBookFragment.newInstance(event.isSale)
        else -> when(event.type) {
            HOME        -> FragmentHome.newInstance()
            HELLO       -> HelloFragment.newInstance()
            EVERYDAY    -> EverydayFragment.newInstance()
            PRESENT     -> PresentFragment.newInstance()
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

class DialogFragmentEvent(val resText: Int? = null, val text: String? = null, val state: CustomDialog? = null,
                          val callBack: CustomDialogFragment.CallBack? = null) :
    FragmentEvent(DIALOG)

class PurchaseEvent(val isNextVisible: Boolean = true):
    FragmentEvent(PURCHASE)

class OfferEvent(val isNextVisible: Boolean = true):
    FragmentEvent(OFFER)

class PayBookEvent(val isSale: Boolean = false):
    FragmentEvent(BOOK)

enum class FragmentType(val id: Int = R.id.container,
                        val addToStack: Boolean = true,
                        val animation: AnimationType = AnimationType.BOTTOM_TO_TOP
) {
    HOME(addToStack = false),
    HELLO,
    PAY,
    BOOK,
    EVERYDAY,
    OFFER,
    PRESENT,
    PURCHASE,
    PASSWORD,
    DIALOG,

    DEFAULT
}
enum class AnimationType{
    NONE,
    BOTTOM_TO_TOP
}