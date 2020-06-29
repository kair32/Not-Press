package com.aks.notpress.ui.offer

import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("textPrice")
fun setTextPrice(tv: TextView, price: String){
    val priceNew = price.substringBeforeLast("₽")
    Log.d("tag", "Price = $price, $priceNew")
    tv.text = priceNew
}