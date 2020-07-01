package com.aks.notpress.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.aks.notpress.service.service.ServiceOverlay
import com.aks.notpress.ui.video.VideoActivity
import com.aks.notpress.utils.ActivityType.*

class ActivityUtil {
    fun observe(owner: LifecycleOwner, viewModel: ActivityStartViewModel, activity: Activity?) =
        viewModel.activityStartLiveData.observe(owner, Observer {
            startActivity(activity ?: return@Observer, it)
        })

    private fun startActivity(activity: Activity, event: ActivityStartEvent) {
        val intent = when (event.type) {
            OVERLAY_PERMISSION  -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.packageName)) else null
            OPEN_INSTAGRAM      -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/varenik_vpuze/"))
            OPEN_BOOK           -> Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/F7JN1T4C2Vg"))
            OPEN_SETTING        -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null))
            VIDEO               -> VideoActivity.newIntent(activity)
            OVERLAY_ACTIVITY    -> ServiceOverlay.newIntent(activity)
            else -> null
        }?: return
        if(intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivityForResult(intent, event.type.code)
        }
        else return
    }
}
open class ActivityStartEvent(
    val type: ActivityType)

enum class ActivityType(val code: Int) {
    DEFAULT(0),OVERLAY_PERMISSION(101), OVERLAY_ACTIVITY(102), OPEN_INSTAGRAM(103),
    OPEN_YOUTUBE(104), VIDEO(105), OPEN_SETTING(106), OPEN_BOOK(107)
}