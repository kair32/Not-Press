package com.aks.notpress.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*

interface Preference{
    fun getPassword():List<Boolean>
    fun setPassword(list: List<Boolean>)

    fun getSubscriptionDay(): Int
    //fun getIsSubscription(): Boolean
    val isSubscription: LiveData<Boolean>
    fun setIsSubscription()

    val isHaveSubscription: LiveData<Boolean>
    fun setHaveSubscription(isHave: Boolean)

    fun update()
    fun clearPreference()

    //billing
    fun billing()
    fun launchBillingMonth()
    fun launchBillingYear()
}

class PreferencesBasket(private val activity: Activity): Preference{
    private val preferences: SharedPreferences = activity.getSharedPreferences(javaClass.simpleName, Context.MODE_PRIVATE)
    private val billingClient: BillingClient = BillingClient
        .newBuilder(activity)
        .enablePendingPurchases()
        .setListener(::onPurchasesUpdated)
        .build()

    override val isSubscription = MutableLiveData<Boolean>(getIsSubscription())
    override val isHaveSubscription = MutableLiveData<Boolean>(getHaveSubscription())

    private val tag = "PreferencesBasket"

    init {
        preferences.registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_IS_SUBSCRIPTION -> {isSubscription.value = getIsSubscription()}
                KEY_HAVE_SUBSCRIPTION -> {isHaveSubscription.value = getHaveSubscription()}
            }
        }
    }

    @Suppress("UNUSED until better times") override fun getPassword(): List<Boolean> {
        val password: String = preferences.getString(KEY_PASSWORD, "000000000")?:"000000000"
        return password.flatMap { listOf<Boolean>(it=='0') }
    }
    @Suppress("UNUSED until better times") override fun setPassword(list: List<Boolean>) {
        val s = list.map { if (it) 1 else 0 }.joinToString()
        preferences.edit().putString(KEY_PASSWORD, s).apply()
    }

    override fun setIsSubscription() = preferences.edit().putBoolean(KEY_IS_SUBSCRIPTION, false).apply()
    private fun getIsSubscription(): Boolean = preferences.getBoolean(KEY_IS_SUBSCRIPTION, true)

    private fun getHaveSubscription(): Boolean = preferences.getBoolean(KEY_HAVE_SUBSCRIPTION, false)
    override fun setHaveSubscription(isHave: Boolean) = preferences.edit().putBoolean(KEY_HAVE_SUBSCRIPTION, isHave).apply()

    override fun getSubscriptionDay(): Int{
        val startSubscriptionDate: Long = preferences.getLong(KEY_SUBSCRIPTION, System.currentTimeMillis() / DAY)
        val sizeSubscription: Int = preferences.getInt(KEY_SIZE_SUBSCRIPTION, 7)

        val day = ((startSubscriptionDate + sizeSubscription - System.currentTimeMillis() / DAY)).toInt()
        val s = if (sizeSubscription - day >= 0 && day >= 0) day else 0

        Log.d(tag, " day = $day, s = $s, sizeSubscription = $sizeSubscription, startSubscriptionDate = $startSubscriptionDate, realTime = ${ System.currentTimeMillis()/ DAY}")
        if (s <= 0) setIsSubscription()
        startSubscriptionDate(System.currentTimeMillis() / DAY)
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
        startSubscriptionDate(System.currentTimeMillis() / DAY)
        setHaveSubscription(false)
    }
    @Suppress("crutch") override fun update() {
        isSubscription.value = getIsSubscription()
        isHaveSubscription.value = getHaveSubscription()
    }

    //region billing
    private var mSkuDetailsMap: MutableMap<String, SkuDetails> = HashMap()
    override fun launchBillingYear() = launch(BILLING_YEAR)
    override fun launchBillingMonth() = launch(BILLING_MONTH)

    private fun launch(skuId: String){
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(mSkuDetailsMap[skuId])
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
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
                    setHaveSubscription(purchasesList?.isNotEmpty() == true)
                    Log.d(tag, "success $purchasesList")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.d(tag, "error")
                //сюда мы попадем если что-то пойдет не так
            }
        })
    }

    private fun querySkuDetails() {
        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
        val skuList: MutableList<String> = ArrayList()
        skuList.add(BILLING_MONTH)// здесь мы добавили id товара из Play Consol
        skuList.add(BILLING_YEAR)
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { responseCode, skuDetailsList ->
            Log.d(tag,"queryS $responseCode, $skuDetailsList")
            for (skuDetails in skuDetailsList)
                mSkuDetailsMap[skuDetails.sku] = skuDetails
        }
    }
    private fun queryPurchases(): List<Purchase?>? = billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList
    private fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            setHaveSubscription(true)
            Log.d(tag,"сюда мы попадем когда будет осуществлена покупка")
            //сюда мы попадем когда будет осуществлена покупка
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(tag,"отменил покупку")
            //отменил покупку
        } else {
            Log.d(tag,"прочие ошибки")
            //прочие ошибки
        }
    }
    //endregion

    companion object {
        const val DAY = 86400000
        const val BILLING_MONTH = "month"
        const val BILLING_YEAR = "year"

        const val KEY_PASSWORD = "password"
        const val KEY_SUBSCRIPTION = "KEY_SUBSCRIPTION"
        const val KEY_SIZE_SUBSCRIPTION = "KEY_SIZE_SUBSCRIPTION"
        const val KEY_IS_SUBSCRIPTION = "KEY_IS_SUBSCRIPTION"
        const val KEY_HAVE_SUBSCRIPTION = "KEY_HAVE_SUBSCRIPTION"
    }
}