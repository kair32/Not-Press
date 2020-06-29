package com.aks.notpress.ui.purchase

import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("textGreenPrice")
fun setTextGreenPrice(tv: TextView, price: String){
    val priceNew = price.substringBeforeLast("/")
    Log.d("tag", "Price = $price, $priceNew")
    tv.text = priceNew
}