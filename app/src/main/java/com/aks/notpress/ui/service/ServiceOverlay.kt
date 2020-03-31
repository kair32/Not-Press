package com.aks.notpress.ui.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.view.*
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.databinding.OverlayBinding

const val DOUBLE_CLICK_INTERVAL: Long = 750
const val TEXT_VISIBLE_INTERVAL: Long = 1500

class ServiceOverlay: LifecycleService() {
    private lateinit var binding: OverlayBinding
    private lateinit var manager: WindowManager

    val isTextVisible = MutableLiveData<Boolean>(false)
    val isPasswordVisible = MutableLiveData<Boolean>(false)

    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
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
        when (i) {
            1,2,3 -> {
                Handler().postDelayed({ i = 0 }, DOUBLE_CLICK_INTERVAL)
                Handler().postDelayed({ isTextVisible.value = false }, TEXT_VISIBLE_INTERVAL)
                isTextVisible.value = true
            }
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