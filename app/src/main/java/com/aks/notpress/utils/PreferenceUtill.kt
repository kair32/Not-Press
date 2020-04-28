package com.aks.notpress.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.android.billingclient.api.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


interface Preference{
    fun getPassword():List<Boolean>
    fun setPassword(list: List<Boolean>)

    val stateSubscription: LiveData<StateSubscription>
    fun getFreeDay():Int
    fun startFreeDay()
    fun clearPreference()
    fun update()

    //billing
    val textSubMonth: LiveData<String>
    val textSubYear: LiveData<String>
    fun billing()
    fun launchBillingMonth()
    fun launchBillingYear()
}

enum class StateSubscription{
    HAVE_SUB(),
    ENDED(),
    NOT_ACTIVE(),
    FREE_DAY();

    companion object{
        fun getState(state: String?) = values().find { it.name.equals(state, ignoreCase = true) } ?: NOT_ACTIVE
    }
}

class PreferencesBasket(private val activity: Activity): Preference{
    private val preferences: SharedPreferences = activity.getSharedPreferences(javaClass.simpleName, Context.MODE_PRIVATE)
    private val billingClient: BillingClient = BillingClient
        .newBuilder(activity)
        .enablePendingPurchases()
        .setListener(::onPurchasesUpdated)
        .build()
    override val textSubMonth = MutableLiveData<String>(activity.getString(R.string.month_subscription))
    override val textSubYear = MutableLiveData<String>(activity.getString(R.string.year_subscription))
    override val stateSubscription = MutableLiveData<StateSubscription>(getStateSubscription())

    private val tag = "PreferencesBasket"

    init {
        preferences.registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_STATE_SUBSCRIPTION -> {
                    Log.d(tag, " KEY_STATE_SUBSCRIPTION ${stateSubscription.value}, ${getStateSubscription()}")
                    stateSubscription.postValue(getStateSubscription())}
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

    private fun setStateSubscription(state: StateSubscription) = preferences.edit().putString(KEY_STATE_SUBSCRIPTION, state.name).apply()
    private fun setEndedSubByBilling(){
        if (getStateSubscription() == StateSubscription.HAVE_SUB )
            setStateSubscription(StateSubscription.ENDED)
    }
    private fun setEndedSubByFreeDay(){
        if (getStateSubscription() == StateSubscription.FREE_DAY )
            setStateSubscription(StateSubscription.ENDED)
    }
    private fun getStateSubscription() = StateSubscription.getState(preferences.getString(KEY_STATE_SUBSCRIPTION,null))
    override fun update(){stateSubscription.value = getStateSubscription()}

    override fun getFreeDay(): Int{
        val startSubscriptionDate: Long = preferences.getLong(KEY_SUBSCRIPTION, 0)
        return if (startSubscriptionDate > 0)
             startAndGetSubscriptionDay()
        else FREE_DAY
    }
    override fun startFreeDay(){
        startAndGetSubscriptionDay()
        setStateSubscription(StateSubscription.FREE_DAY)
    }

    private fun startAndGetSubscriptionDay(): Int{
        val startSubscriptionDate = getStartSubscriptionDate()
        val sizeSubscription: Int = preferences.getInt(KEY_SIZE_SUBSCRIPTION, FREE_DAY)

        val day = ((startSubscriptionDate + sizeSubscription - System.currentTimeMillis() / DAY)).toInt()
        val s = if (sizeSubscription - day >= 0 && day >= 0) day else 0

        Log.d(tag, " day = $day, s = $s, sizeSubscription = $sizeSubscription, startSubscriptionDate = $startSubscriptionDate, realTime = ${ System.currentTimeMillis()/ DAY}")
        if (s <= 0)  setEndedSubByFreeDay()
        startSubscriptionDate(System.currentTimeMillis() / DAY)
        setSizeSubscription(s)
        return  s
    }
    private fun getStartSubscriptionDate(): Long = preferences.getLong(KEY_SUBSCRIPTION, readFile()?:System.currentTimeMillis() / DAY)
    private fun startSubscriptionDate(date: Long){
        writeToFile(date)
        preferences.edit().putLong(KEY_SUBSCRIPTION, date).apply()
    }
    private fun setSizeSubscription(size: Int)      = preferences.edit().putInt(KEY_SIZE_SUBSCRIPTION, size).apply()

    //region file
    private fun writeToFile(date: Long){
        if (ContextCompat.checkSelfPermission(activity, PermissionType.READ_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED  ||
            ContextCompat.checkSelfPermission(activity, PermissionType.WRITE_EXTERNAL_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED )
            return
        try {
            Log.d(tag, "file pre write")
            val outputStream = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, FILENAME)
                FileOutputStream(file)
            } else {
                val resolver = activity.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "test.txt")
                    put(MediaStore.MediaColumns.MIME_TYPE, "txt/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)?.let {
                    resolver.openOutputStream(it)
                }
            }
            outputStream?.use { stream ->
                stream.write(date.toString().toByteArray())
                stream.close()
            }

            Log.d(tag, "file Файл записан")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun readFile():Long? {
        Log.d(tag, "file pre read")
        if (ContextCompat.checkSelfPermission(activity, PermissionType.READ_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED  ||
            ContextCompat.checkSelfPermission(activity, PermissionType.WRITE_EXTERNAL_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED )
            return null
        try {
        val inputStream = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(directory, FILENAME)
            FileInputStream(file)
        } else {

            val resolver = activity.contentResolver
            val contentValues = arrayOf(
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.DATA
            )

            var inputStream: FileInputStream? = null
            val audioCursor = resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues, null, null)
            if (audioCursor != null) {
                if (audioCursor.moveToFirst()) {
                    do {
                        val s = audioCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                        if (audioCursor.getString(s) == "test.txt") {
                            val patch = audioCursor.getString(audioCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH))
                            val data = audioCursor.getString(audioCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                            inputStream = FileInputStream( File(data))
                        }
                    } while (audioCursor.moveToNext())
                }
            }
            assert(audioCursor != null)
            audioCursor?.close()
            inputStream
        }
        val byteArray = ByteArray(inputStream?.available()?:return null)
        inputStream.use { stream ->
            stream.read(byteArray)
        }
        Log.d(tag, "file ${String(byteArray)}")
        return String(byteArray).toLongOrNull()
        } catch (e: IOException) { e.printStackTrace() }
        return null
    }
    //endregion

    @Suppress("ONLY_DEBUG") override fun clearPreference() {
        preferences.edit().putBoolean(KEY_IS_SUBSCRIPTION, true).apply()
        setSizeSubscription(7)
        startSubscriptionDate(System.currentTimeMillis() / DAY)
        setStateSubscription(StateSubscription.NOT_ACTIVE)
    }

    //region billing
    private var mSkuDetailsMap: MutableMap<String, SkuDetails> = HashMap()
    override fun launchBillingYear() = launch(BILLING_YEAR)
    override fun launchBillingMonth() = launch(BILLING_MONTH)

    private fun launch(skuId: String){
        Log.d(tag,"launch $skuId")
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
                    if (purchasesList?.isNotEmpty() == true)
                        setStateSubscription(StateSubscription.HAVE_SUB)
                    else setEndedSubByBilling()
                    Log.d(tag, "success $purchasesList")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.d(tag, "error")
                setEndedSubByBilling()
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

            mSkuDetailsMap[BILLING_MONTH]?.price?.let {
                textSubMonth.value = it.deleteKopeck() + activity.getString(R.string.month)}
            mSkuDetailsMap[BILLING_YEAR]?.price?.let { textSubYear.value  =  it.deleteKopeck() + activity.getString(R.string.year)}
        }
    }
    private fun String.deleteKopeck() = this.substringBefore(",") + this.substringAfterLast("0")//990,00 ₽
    private fun queryPurchases(): List<Purchase?>? = billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList
    private fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            setStateSubscription(StateSubscription.HAVE_SUB)
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
        const val FREE_DAY = 7
        const val DAY = 86400000
        const val FILENAME = "detail.txt"
        const val BILLING_MONTH = "month"
        const val BILLING_YEAR = "year"

        const val KEY_PASSWORD = "password"
        const val KEY_STATE_SUBSCRIPTION = "KEY_STATE_SUBSCRIPTION"
        const val KEY_SUBSCRIPTION = "KEY_SUBSCRIPTION"
        const val KEY_SIZE_SUBSCRIPTION = "KEY_SIZE_SUBSCRIPTION"
        const val KEY_IS_SUBSCRIPTION = "KEY_IS_SUBSCRIPTION"
        const val KEY_HAVE_SUBSCRIPTION = "KEY_HAVE_SUBSCRIPTION"
    }
}