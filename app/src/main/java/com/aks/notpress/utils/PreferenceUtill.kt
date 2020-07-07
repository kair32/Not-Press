package com.aks.notpress.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aks.notpress.R
import com.android.billingclient.api.*
import java.io.*
import java.lang.Math.abs

interface Preference{
    fun getPassword():List<Boolean>
    fun setPassword(list: List<Boolean>)

    val stateSubscription: LiveData<StateSubscription>
    val freeDay: LiveData<Int>
    fun getFreeDay():Int
    fun startFreeDay()
    fun update()

    fun isFirstStart(): Boolean
    fun setFirst()

    fun setFreeMinute(mils: Long)
    fun getFreeMinute(): Long

    fun getEveryDayCount():Int
    fun setEveryDayCount(count: Int)

    //billing
    val isHaveBook: LiveData<Boolean>
    val textSubMonth: LiveData<String>
    val textSubYear: LiveData<String>
    val textBook: LiveData<String>
    val textBookVIP: LiveData<String>
    val textSaleSubMonth: LiveData<String>
    val textSaleSubYear: LiveData<String>
    val textSaleBookVIP: LiveData<String>
    fun billing()
    fun launchBillingBook()
    fun launchBillingBookVIP()
    fun launchBillingMonth()
    fun launchBillingYear()
    fun launchSaleBillingBookVIP()
    fun launchSaleBillingMonth()
    fun launchSaleBillingYear()

    fun setHotOffer(isHave: Boolean)
    fun getHotOffer(): Boolean
    fun getHotOfferTime(): Long
}

enum class StateSubscription(val codeName: String){
    HAVE_SUB("Y1E3S"),
    ENDED("O4U1C54H"),
    NOT_ACTIVE("0SCS"),
    FREE_DAY("13dEf"),
    FREE_MINUTE("1EFR7"),
    FREE_DAY_LARGE("F23EAR");

    companion object{
        fun getStateByCode(codeName: String?): StateSubscription? = values().find { it.codeName.equals(codeName, ignoreCase = true) } ?: null
        fun getState(state: String?) = values().find { it.name.equals(state, ignoreCase = true) } ?: NOT_ACTIVE
        fun getStateOrNull(state: String?): StateSubscription? = values().find { it.name.equals(state, ignoreCase = true) } ?: null
    }
}

class PreferencesBasket(private val activity: Activity): Preference{
    private val preferences: SharedPreferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val billingClient: BillingClient = BillingClient
        .newBuilder(activity)
        .enablePendingPurchases()
        .setListener(::onPurchasesUpdated)
        .build()
    override val isHaveBook = MutableLiveData<Boolean>(false)
    override val textSubMonth = MutableLiveData<String>("-.--")
    override val textSubYear = MutableLiveData<String>("-.--")
    override val textBook = MutableLiveData<String>("-.--")
    override val textBookVIP = MutableLiveData<String>("-.--")
    override val textSaleSubMonth = MutableLiveData<String>("-.--")
    override val textSaleSubYear = MutableLiveData<String>("-.--")
    override val textSaleBookVIP = MutableLiveData<String>("-.--")
    override val stateSubscription = MutableLiveData<StateSubscription>(getStateSubscription())
    override val freeDay = MutableLiveData<Int>(getFreeDay())

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

    override fun setEveryDayCount(count: Int){
        val realDay = System.currentTimeMillis() / DAY
        val resDay = getEveryDaySumDay()?:(System.currentTimeMillis() / DAY -1)
        val differenceDay = realDay - resDay
        if (count == 4) setEndedSubByFreeMinute()
        if (differenceDay == 0L) return
        if (differenceDay == 1L){
            preferences.edit().putString(KEY_EVERYDAY_COUNT_SUM_DAY, "$count,$realDay").apply()
            setFreeMinute(when (count){
                1 -> FREE_MINUTE_10
                2 -> FREE_MINUTE_20
                3 -> FREE_MINUTE_HOUR
                else -> 0
            })
        }
        if (differenceDay < 0L)  preferences.edit().putString(KEY_EVERYDAY_COUNT_SUM_DAY, "4,$realDay").apply()
        if (differenceDay > 1L)  preferences.edit().putString(KEY_EVERYDAY_COUNT_SUM_DAY, "0,$realDay").apply()
    }
    private fun getEveryDaySumDay(): Long?{
        val str = preferences.getString(KEY_EVERYDAY_COUNT_SUM_DAY, null)
        return str?.substringAfter(",")?.toLongOrNull()
    }
    override fun getEveryDayCount():Int {
        val str = preferences.getString(KEY_EVERYDAY_COUNT_SUM_DAY, "")
        val result = str?.substringBefore(",")?.toIntOrNull()?: 0
        val realDay = System.currentTimeMillis() / DAY
        val resDay = getEveryDaySumDay()?:(System.currentTimeMillis() / DAY -1)
        val differenceDay = realDay - resDay
        if (differenceDay == 0L) return result
        if (differenceDay == 1L) return result + 1
        if (differenceDay > 1L)  return 1
        return result
    }

    override fun setFreeMinute(mils: Long) = preferences.edit().putLong(KEY_FREE_MINUTE, mils).apply()
    override fun getFreeMinute(): Long = preferences.getLong(KEY_FREE_MINUTE, FREE_MINUTE_10)

    override fun setFirst() = preferences.edit().putBoolean(KEY_IS_FIRST_START, false).apply()
    override fun isFirstStart() = preferences.getBoolean(KEY_IS_FIRST_START, true)

    override fun setHotOffer(isHave: Boolean) {preferences.edit().putBoolean(KEY_IS_HOT_OFFER, isHave).apply()}
    override fun getHotOffer(): Boolean {
        val time = HOT_OFFER_TIME // 30 минут в миллисекундах 1800000L
        val offerTime = getSettingHotOfferTime()
        if (offerTime == -1L) setHotOfferTime()
        else {
            val s = System.currentTimeMillis() - offerTime
            if (s > time){
                setHotOffer(false)
                return false
            }
        }
        return preferences.getBoolean(KEY_IS_HOT_OFFER, true)
    }
    private fun setHotOfferTime() {preferences.edit().putLong(KEY_HOT_OFFER_TIME, System.currentTimeMillis()).apply()}
    private fun getSettingHotOfferTime(): Long = preferences.getLong(KEY_HOT_OFFER_TIME, -1)
    override fun getHotOfferTime(): Long = abs(System.currentTimeMillis() - preferences.getLong(KEY_HOT_OFFER_TIME, -1) - HOT_OFFER_TIME)

    private fun setStateSubscription(state: StateSubscription) {
        stateSubscription?.value = state
        preferences.edit().putString(KEY_STATE_SUBSCRIPTION, state.name).apply()
    }
    private fun setEndedSubByBilling(){
        if (getStateSubscription() == StateSubscription.HAVE_SUB )
            setStateSubscription(StateSubscription.ENDED)
    }
    private fun setEndedSubByFreeDay(){
        if (getStateSubscription() == StateSubscription.FREE_DAY ) {
            setStateSubscription(StateSubscription.FREE_MINUTE)
            setEveryDayCount(1)
        }
        if (getStateSubscription() == StateSubscription.FREE_DAY_LARGE )
            setStateSubscription(StateSubscription.ENDED)
    }
    private fun setEndedSubByFreeMinute(){
        if (getStateSubscription() == StateSubscription.FREE_MINUTE ) {
            setStateSubscription(StateSubscription.FREE_DAY_LARGE)
            startAndGetSubscriptionDay(true)
        }
    }
    private fun getStateSubscription(): StateSubscription{
        val state = preferences.getString(KEY_STATE_SUBSCRIPTION,null)
        val startSubscriptionDate = getStartSubscriptionDate()
        if (startSubscriptionDate < System.currentTimeMillis() / DAY && state == null ) startFreeDay()
        return StateSubscription.getState(state)
    }
    override fun update(){
        stateSubscription.value = getStateSubscription()
        freeDay.value = getFreeDay()
    }

    override fun getFreeDay(): Int{
        if (getStateSubscription() == StateSubscription.FREE_MINUTE) {
            val freeMils = getFreeMinute()
            return (freeMils / MILS_TO_MINUTE).toInt()
        }
        val startSubscriptionDate: Long = readDate()?:preferences.getLong(KEY_SUBSCRIPTION, 0)
        return if (startSubscriptionDate > 0)
             startAndGetSubscriptionDay()
        else FREE_DAY
    }
    override fun startFreeDay(){
        setStateSubscription(StateSubscription.FREE_DAY)
        startAndGetSubscriptionDay()
    }

    private fun startAndGetSubscriptionDay(rewrite: Boolean = false): Int{
        val startSubscriptionDate = getStartSubscriptionDate()
        val sizeSubscription =
            when {
                getStartSubscriptionStatus() == StateSubscription.FREE_DAY          -> FREE_DAY
                getStartSubscriptionStatus() == StateSubscription.FREE_DAY_LARGE    -> LONG_FREE_DAY
                else -> 999
            }
        val realTime = System.currentTimeMillis() / DAY

        val day = ((startSubscriptionDate + sizeSubscription - realTime)).toInt()
        val s = if (sizeSubscription - day >= 0 && day >= 0) day else 0

        Log.d(tag, " day = $day, s = $s, sizeSubscription = $sizeSubscription, startSubscriptionDate = $startSubscriptionDate, realTime = $realTime")
        if (s <= 0) setEndedSubByFreeDay()
        startSubscriptionDate(if (rewrite) realTime else startSubscriptionDate, rewrite)
        setSizeSubscription(s)
        return  s
    }
    private fun getStartSubscriptionDate(): Long = readDate()?:preferences.getLong(KEY_SUBSCRIPTION, System.currentTimeMillis() / DAY)
    private fun getStartSubscriptionStatus(): StateSubscription = readState()
    private fun startSubscriptionDate(date: Long, rewrite: Boolean){
        writeToFile(date, rewrite)
        preferences.edit().putLong(KEY_SUBSCRIPTION, date).apply()
    }
    private fun setSizeSubscription(size: Int) = preferences.edit().putInt(KEY_SIZE_SUBSCRIPTION, size).apply()

    //region file
    private fun writeToFile(date: Long, rewrite :Boolean){
        val value = getStartSubscriptionStatus().codeName + ".$date"
        if (ContextCompat.checkSelfPermission(activity, PermissionType.READ_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED  ||
            ContextCompat.checkSelfPermission(activity, PermissionType.WRITE_EXTERNAL_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED )
            return
        if (readFile() == null || rewrite)
        try {
            Log.d(tag, "file pre write")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, FILENAME)
                FileOutputStream(file).use { stream ->
                    stream.write(value.toByteArray())
                    stream.close()
                    Log.d(tag, "file Файл записан ${value.toString()}")
                }
            } else {
                if (rewriteToOldFile(value)) return
                val resolver = activity.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, FILENAME)
                    put(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, FILENAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "txt/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val url = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                url?.let {
                    resolver.openOutputStream(it)?.use { stream ->
                        stream.write(value.toByteArray())
                        stream.close()
                        Log.d(tag, "file Файл записан $value")
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun rewriteToOldFile(value: String): Boolean{
        try {
            val resolver = activity.contentResolver
            val contentValues = arrayOf(
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.DATA
            )

            var outputStream: FileOutputStream? = null
            val audioCursor = resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues, null, null)
            if (audioCursor != null) {
                if (audioCursor.moveToFirst()) {
                    do {
                        val s = audioCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                        if (audioCursor.getString(s) == "phone_testing.txt") {
                            val data = audioCursor.getString(audioCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                            outputStream = FileOutputStream(File(data))
                        }
                    } while (audioCursor.moveToNext())
                }
            }
            assert(audioCursor != null)
            audioCursor?.close()
            outputStream?.use { stream ->
                stream.write(value.toByteArray())
                stream.close()
                Log.d(tag, "file Файл перезаписан $value")
                return true
            }
        }
        catch (e: IOException) { e.printStackTrace()
        return false}
        return false
    }

    private fun readDate(): Long? = readFile()?.substringAfterLast(".")?.toLongOrNull()
    private fun readState(): StateSubscription{
        val state = getStateSubscription()
        return if (state == StateSubscription.NOT_ACTIVE) StateSubscription.getStateByCode(readFile()?.substringBefore("."))?:state
        else state
    }

    private fun readFile():String? {
        Log.d(tag, "file pre read")
        if (ContextCompat.checkSelfPermission(activity, PermissionType.READ_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED  ||
            ContextCompat.checkSelfPermission(activity, PermissionType.WRITE_EXTERNAL_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED )
            return null
        try {
        val inputStream = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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
                        if (audioCursor.getString(s) == "phone_testing.txt") {
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
        return String(byteArray)
        } catch (e: IOException) { e.printStackTrace() }
        return null
    }
    //endregion

    //region billing
    private var mSkuDetailsMap: MutableMap<String, SkuDetails> = HashMap()
    override fun launchBillingYear() = launch(BILLING_YEAR)
    override fun launchBillingMonth() = launch(BILLING_MONTH)
    override fun launchBillingBook() = launch(BILLING_BOOK)
    override fun launchBillingBookVIP() = launch(BILLING_BOOK_VIP)
    override fun launchSaleBillingBookVIP() = launch(BILLING_SALE_BOOK_VIP)
    override fun launchSaleBillingMonth() = launch(BILLING_SALE_MONTH)
    override fun launchSaleBillingYear() = launch(BILLING_SALE_YEAR)

    init {
        Log.d(tag,"INITIALIZED")
    }
    private fun launch(skuId: String){
        Log.d(tag,"launch $skuId")
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(mSkuDetailsMap[skuId]?:return)
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
                    val purchasesList = ArrayList<Purchase?>()
                    queryPurchasesSUBS()?.let {purchasesList.addAll(it)}//запрос о покупках
                    queryPurchasesINAPP()?.let {purchasesList.addAll(it)}//запрос о покупках

                    updatePurchases(purchasesList)

                    Log.d(tag, "success Subs $purchasesList")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.d(tag, "error")
                setEndedSubByBilling()
                //сюда мы попадем если что-то пойдет не так
            }
        })
    }
    private fun updatePurchases(purchasesList: List<Purchase?>?){
        when {
            purchasesList?.any{ it?.sku == BILLING_BOOK_VIP || it?.sku == BILLING_SALE_BOOK_VIP} == true -> {
                isHaveBook.value = true
                setStateSubscription(StateSubscription.HAVE_SUB)
            }
            purchasesList?.any{ it?.sku != BILLING_BOOK} == true -> setStateSubscription(StateSubscription.HAVE_SUB)
            else -> setEndedSubByBilling()
        }

        if (purchasesList?.any{ it?.sku == BILLING_BOOK} == true){ isHaveBook.value = true}
    }

    private fun querySkuDetails() {
        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
        val skuList: MutableList<String> = ArrayList()
        skuList.add(BILLING_MONTH)// здесь мы добавили id товара из Play Consol
        skuList.add(BILLING_YEAR)
        skuList.add(BILLING_BOOK)
        skuList.add(BILLING_BOOK_VIP)
        skuList.add(BILLING_SALE_BOOK_VIP)
        skuList.add(BILLING_SALE_MONTH)
        skuList.add(BILLING_SALE_YEAR)
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { responseCode, skuDetailsList ->
            Log.d(tag,"skuDetails subs $responseCode, $skuDetailsList")
            if (skuDetailsList == null) return@querySkuDetailsAsync
            for (skuDetails in skuDetailsList)
                mSkuDetailsMap[skuDetails.sku] = skuDetails

            mSkuDetailsMap[BILLING_MONTH]?.price?.let { textSubMonth.value = it.deleteKopeck() + activity.getString(R.string.month)}
            mSkuDetailsMap[BILLING_YEAR]?.price?.let { textSubYear.value  =  it.deleteKopeck() + activity.getString(R.string.year)}
            mSkuDetailsMap[BILLING_SALE_MONTH]?.price?.let { textSaleSubMonth.value = it.deleteKopeck() }
            mSkuDetailsMap[BILLING_SALE_YEAR]?.price?.let { textSaleSubYear.value = it.deleteKopeck() }
        }
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { responseCode, skuDetailsList ->
            Log.d(tag,"skuDetails inapp $responseCode, $skuDetailsList")
            if (skuDetailsList == null) return@querySkuDetailsAsync
            for (skuDetails in skuDetailsList)
                mSkuDetailsMap[skuDetails.sku] = skuDetails

            mSkuDetailsMap[BILLING_BOOK]?.price?.let { textBook.value = it.deleteKopeck() }
            mSkuDetailsMap[BILLING_BOOK_VIP]?.price?.let { textBookVIP.value = it.deleteKopeck() }
            mSkuDetailsMap[BILLING_SALE_BOOK_VIP]?.price?.let { textSaleBookVIP.value = it.deleteKopeck() }
        }
    }
    private fun String.deleteKopeck() = this.substringBefore(",") + this.substringAfterLast("0")//990,00 ₽
    private fun queryPurchasesSUBS(): List<Purchase?>? = billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList
    private fun queryPurchasesINAPP(): List<Purchase?>? = billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList

    private fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            updatePurchases(purchases)
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
        const val PREFERENCES_NAME = "PreferencesBasket"
        const val FREE_DAY = 7
        const val LONG_FREE_DAY = 30
        const val DAY = 86400000// 1 день в милисекундах
        const val HOT_OFFER_TIME = 1800000L// 30 минут в миллисекундах 1800000L
        const val MILS_TO_MINUTE = 60000L // 1 минута в миллисекундах для удобства перевода
        const val FILENAME = "phone_testing.txt"

        const val BILLING_MONTH = "month"
        const val BILLING_YEAR = "year"
        const val BILLING_SALE_MONTH = "sale_month"
        const val BILLING_SALE_YEAR = "sale_year"
        const val BILLING_SALE_BOOK_VIP = "sale_book_vip"
        const val BILLING_BOOK_VIP = "book_vip"
        const val BILLING_BOOK = "book"

        const val KEY_IS_FIRST_START = "KEY_IS_FIRST_START"
        const val KEY_EVERYDAY_COUNT_SUM_DAY = "KEY_EVERYDAY_COUNT_SUM_DAY"
        const val KEY_PASSWORD = "password"
        const val KEY_STATE_SUBSCRIPTION = "KEY_STATE_SUBSCRIPTION"
        const val KEY_SUBSCRIPTION = "KEY_SUBSCRIPTION"
        const val KEY_SIZE_SUBSCRIPTION = "KEY_SIZE_SUBSCRIPTION"
        const val KEY_IS_SUBSCRIPTION = "KEY_IS_SUBSCRIPTION"
        const val KEY_IS_HOT_OFFER = "KEY_IS_HOT_OFFER"
        const val KEY_HOT_OFFER_TIME = "KEY_HOT_OFFER_TIME"
        const val KEY_FREE_MINUTE = "KEY_FREE_MINUTE"

        const val FREE_MINUTE_10 = 600000L//600000L
        const val FREE_MINUTE_20 = 1200000L
        const val FREE_MINUTE_HOUR = 3600000L
    }
}