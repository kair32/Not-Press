package com.aks.notpress.ui.offer

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.aks.notpress.R
import com.aks.notpress.utils.PreferencesBasket


@BindingAdapter("textPrice")
fun setTextPrice(tv: TextView, price: String){
    val priceNew = price.substringBeforeLast("₽")
    Log.d("tag", "Price = $price, $priceNew")
    tv.text = priceNew
}

@BindingAdapter("textNewPrice")
fun setTextNewPrice(tv: TextView, price: String){
    val priceNew = price.filter { !it.isWhitespace() }
    Log.d("textNewPrice", "Price = $price, $priceNew")
    tv.text = priceNew
}

@BindingAdapter("numberPickerTime")
fun setNumberPickerTime(ll: LinearLayout, viewModel: OfferViewModel){
    val time = if (viewModel.offerTime == -1L) PreferencesBasket.HOT_OFFER_TIME else viewModel.offerTime + 1000
    var mTime = 0L
    val mHandler = Handler()
    val hour1 = ll.findViewById<TextView>(R.id.np_hour1)
    val hour2 = ll.findViewById<TextView>(R.id.np_hour2)
    val min1 = ll.findViewById<TextView>(R.id.np_min1)
    val min2 = ll.findViewById<TextView>(R.id.np_min2)

    val timeUpdaterRunnable: Runnable = object : Runnable {
        override fun run() {
            val start: Long = mTime
            val millis = SystemClock.uptimeMillis() - start
            var second = (time - millis) / 1000
            val min = second / 60
            second %= 60
            hour1.text = (min / 10).toString()
            hour2.text = (min % 10).toString()
            min1.text = (second / 10).toString()
            min2.text = (second % 10).toString()
            if ((time - millis) / 1000 <= 0) {
                mHandler.removeCallbacks(this)
                viewModel.onNext()
            }else {
                Log.d("numberPickerTime","${(time - millis) / 1000}")
                Handler().postDelayed(this, 200)
            }
        }
    }


    if (mTime == 0L) {
        mTime = SystemClock.uptimeMillis()
        mHandler.removeCallbacks(timeUpdaterRunnable)
        mHandler.postDelayed(timeUpdaterRunnable, 100)
    }

}