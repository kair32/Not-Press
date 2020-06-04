package com.aks.notpress.service.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.view.*
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.aks.notpress.databinding.OverlayBinding

const val DOUBLE_CLICK_INTERVAL: Long = 1250
const val TEXT_VISIBLE_INTERVAL: Long = 1500

class ServiceOverlay: LifecycleService() {
    private lateinit var binding: OverlayBinding
    private lateinit var manager: WindowManager

    val isTextVisible = MutableLiveData<Boolean>(false)
    val textClick = MutableLiveData<String>("3")

    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(WINDOW_SERVICE) as WindowManager
        val type  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(type,
            (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED),
            PixelFormat.TRANSLUCENT)

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0

        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = OverlayBinding.inflate(layoutInflater, null, false)
        binding.viewModel = this
        binding.root.setOnTouchListener(::onTouch)
        binding.iv.setOnClickListener(::onClick)
        binding.setLifecycleOwner(this)

        manager.addView(binding.root, params)
        hideSystemUI()
    }

    private var i = 0
    private fun onClick(v: View) {
        i++
        textClick.postValue((4-i).toString())
        when (i) {
            1 -> {
                Handler().postDelayed({ i = 0 }, DOUBLE_CLICK_INTERVAL)
                Handler().postDelayed({ isTextVisible.value = false }, TEXT_VISIBLE_INTERVAL)
                isTextVisible.value = true
            }
            3 -> textClick.postValue(baseContext.getString(R.string.just_little))
            4 -> {
                i = 0
                isTextVisible.value = false
                destroy()
            }
        }
    }

    private fun destroy(){
        manager.removeView(binding.root)
        showSystemUI()
        stopSelf()
    }

    private fun onTouch(v: View, event: MotionEvent): Boolean{
        hideSystemUI()
        return false
    }

    private fun hideSystemUI() {
        binding.root.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        Handler().postDelayed({
            hideSystemUI()
            binding.root.context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        }, TEXT_VISIBLE_INTERVAL)
    }

    private fun showSystemUI() {
        binding.root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    companion object {
        fun newIntent(context: Context): Intent? {
            val intent = Intent(context, ServiceOverlay::class.java)
            context.startService(intent)
            return null
        }
    }
}