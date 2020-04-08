package com.aks.notpress.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.*

interface Preference{
    fun getPassword():List<Boolean>
    fun setPassword(list: List<Boolean>)

    fun getSubscriptionDay(): Int
    fun getIsSubscription(): Boolean
    fun setIsSubscription()

    fun getHaveSubscription(): Boolean
    fun setHaveSubscription(isHave: Boolean)

    fun clearPreference()

    //billing
    fun billing()
}

class PreferencesBasket(context: Context): Preference{
    private val preferences: SharedPreferences = context.getSharedPreferences(javaClass.simpleName, Context.MODE_PRIVATE)
    private val billingClient: BillingClient = BillingClient
        .newBuilder(context)
        .enablePendingPurchases()
        .setListener(::onPurchasesUpdated)
        .build()

    private val tag = "PreferencesBasket"


    @Suppress("UNUSED until better times") override fun getPassword(): List<Boolean> {
        val password: String = preferences.getString(KEY_PASSWORD, "000000000")?:"000000000"
        return password.flatMap { listOf<Boolean>(it=='0') }
    }
    @Suppress("UNUSED until better times") override fun setPassword(list: List<Boolean>) {
        val s = list.map { if (it) 1 else 0 }.joinToString()
        preferences.edit().putString(KEY_PASSWORD, s).apply()
    }

    override fun setIsSubscription() = preferences.edit().putBoolean(KEY_IS_SUBSCRIPTION, false).apply()
    override fun getIsSubscription(): Boolean = preferences.getBoolean(KEY_IS_SUBSCRIPTION, true)

    override fun getHaveSubscription(): Boolean = preferences.getBoolean(KEY_HAVE_SUBSCRIPTION, false)
    override fun setHaveSubscription(isHave: Boolean) = preferences.edit().putBoolean(KEY_HAVE_SUBSCRIPTION, isHave).apply()

    override fun getSubscriptionDay(): Int{
        val startSubscriptionDate: Long = preferences.getLong(KEY_SUBSCRIPTION, System.currentTimeMillis())
        val sizeSubscription: Int = preferences.getInt(KEY_SIZE_SUBSCRIPTION, 7)

        val day = ((startSubscriptionDate + sizeSubscription * 86400000 - System.currentTimeMillis())/ 86400000).toInt() + 1
        val s = if (sizeSubscription - day >= 0 && day >= 0) day else 0

        Log.d(tag, " day = $day, s = $s, sizeSubscription = $sizeSubscription, startSubscriptionDate = $startSubscriptionDate, realTime = ${ System.currentTimeMillis()}")
        startSubscriptionDate(System.currentTimeMillis())
        setSizeSubscription(s)
        return  s
    }
    private fun startSubscriptionDate(date: Long){
        preferences.edit().putLong(KEY_SUBSCRIPTION, date).apply()
    }
    private fun setSizeSubscription(size: Int){
        preferences.edit().putInt(KEY_SIZE_SUBSCRIPTION, size).apply()
    }

    @Suppress("ONLY_DEBUG") override fun clearPreference() {
        preferences.edit().putBoolean(KEY_IS_SUBSCRIPTION, true).apply()
        setSizeSubscription(7)
        startSubscriptionDate(System.currentTimeMillis())
        setHaveSubscription(false)
    }

    //region billing
    override fun billing() {
        //https://support.google.com/googleplay/android-developer/answer/140504
        //https://developer.android.com/google/play/billing/billing_library_overview
        //https://habr.com/ru/post/444072/

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    ///здесь мы можем запросить информацию о товарах и покупках
                    querySkuDetails()                    //запрос о товарах
                    val purchasesList = queryPurchases() //запрос о покупках
                    Log.d("tag", "success")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.d("tag", "error")
                //сюда мы попадем если что-то пойдет не так
            }
        })
    }
    private fun querySkuDetails() {
        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
        val skuList: MutableList<String> = ArrayList()
        skuList.add("sku_id_1")// здесь мы добавили id товара из Play Consol
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { responseCode, skuDetailsList ->

        }
    }
    private fun queryPurchases(): List<Purchase?>? = billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList
    private fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            //сюда мы попадем когда будет осуществлена покупка
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            //отменил покупку
        } else {
            //прочие ошибки
        }
    }
    //endregion
    companion object {
        const val KEY_PASSWORD = "password"
        const val KEY_SUBSCRIPTION = "KEY_SUBSCRIPTION"
        const val KEY_SIZE_SUBSCRIPTION = "KEY_SIZE_SUBSCRIPTION"
        const val KEY_IS_SUBSCRIPTION = "KEY_IS_SUBSCRIPTION"
        const val KEY_HAVE_SUBSCRIPTION = "KEY_HAVE_SUBSCRIPTION"
    }
}