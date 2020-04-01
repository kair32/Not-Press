package com.aks.notpress.utils

import android.app.Activity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

class FinishUtil{
    fun observe(owner: LifecycleOwner, viewModel: FinishViewModel, activity: Activity?) {
        viewModel.finishLiveData.observe(owner, Observer {
            finish(activity ?: return@Observer, it)
        })
    }
    fun observe(owner: LifecycleOwner, viewModel: FinishViewModel, fragment: DialogFragment?) {
        viewModel.finishLiveData.observe(owner, Observer {
            dismiss(fragment ?: return@Observer, it)
        })
    }
    fun observe(owner: LifecycleOwner, viewModel: FinishViewModel, fragment: Fragment?) {
        viewModel.finishLiveData.observe(owner, Observer {
            finish(fragment?.activity ?: return@Observer, it)
        })
    }

    private fun finish(activity: Activity, event: FinishEvent) {
        when(event.type) {
            FinishType.FINISH -> activity.finish()
            FinishType.BACK -> activity.onBackPressed()
            else -> throw IllegalArgumentException("Use this event (${event.type}) with dialog.")
        }
    }

    private fun dismiss(fragment: DialogFragment, event: FinishEvent) {
        if (event.type != FinishType.DISMISS) throw IllegalArgumentException("Use this event (${event.type}) with activity.")
        fragment.dismiss()
    }
}

class FinishEvent(
    val type: FinishType
)

enum class FinishType {
    FINISH, DISMISS, BACK
}