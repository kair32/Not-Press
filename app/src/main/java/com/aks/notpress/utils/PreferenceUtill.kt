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
import java.lang.Math.abs


interface Preference{
    fun getPassword():List<Boolean>
    fun setPassword(list: List<Boolean>)

    val stateSubscription: LiveData<StateSubscription>
    val freeDay: LiveData<Int>
    fun getFreeDay():Int
    fun startFreeDay()
    fun clearPreference()
    fun update()

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
    override val isHaveBook = MutableLiveData<Boolean>(false)
    override val textSubMonth = MutableLiveData<String>(activity.getString(R.string.month_subscription))
    override val textSubYear = MutableLiveData<String>(activity.getString(R.string.year_subscription))
    override val textBook = MutableLiveData<String>("")
    override val textBookVIP = MutableLiveData<String>("")
    override val textSaleSubMonth = MutableLiveData<String>("")
    override val textSaleSubYear = MutableLiveData<String>("")
    override val textSaleBookVIP = MutableLiveData<String>("")
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

    private fun setStateSubscription(state: StateSubscription) = preferences.edit().putString(KEY_STATE_SUBSCRIPTION, state.name).apply()
    private fun setEndedSubByBilling(){
        if (getStateSubscription() == StateSubscription.HAVE_SUB )
            setStateSubscription(StateSubscription.ENDED)
    }
    private fun setEndedSubByFreeDay(){
        if (getStateSubscription() == StateSubscription.FREE_DAY )
            setStateSubscription(StateSubscription.ENDED)
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
        val startSubscriptionDate: Long = readFile()?:preferences.getLong(KEY_SUBSCRIPTION, 0)
        return if (startSubscriptionDate > 0)
             startAndGetSubscriptionDay()
        else FREE_DAY
    }
    override fun startFreeDay(){
        setStateSubscription(StateSubscription.FREE_DAY)
        startAndGetSubscriptionDay()
    }

    private fun startAndGetSubscriptionDay(): Int{
        val startSubscriptionDate = getStartSubscriptionDate()
        val sizeSubscription: Int = FREE_DAY
        val realTime = System.currentTimeMillis() / DAY

        val day = ((startSubscriptionDate + sizeSubscription - realTime)).toInt()
        val s = if (sizeSubscription - day >= 0 && day >= 0) day else 0

        Log.d(tag, " day = $day, s = $s, sizeSubscription = $sizeSubscription, startSubscriptionDate = $startSubscriptionDate, realTime = $realTime")
        if (s <= 0)  setEndedSubByFreeDay()
        startSubscriptionDate(startSubscriptionDate)
        setSizeSubscription(s)
        return  s
    }
    private fun getStartSubscriptionDate(): Long = readFile()?:preferences.getLong(KEY_SUBSCRIPTION, System.currentTimeMillis() / DAY)
    private fun startSubscriptionDate(date: Long){
        writeToFile(date)
        preferences.edit().putLong(KEY_SUBSCRIPTION, date).apply()
    }
    private fun setSizeSubscription(size: Int) = preferences.edit().putInt(KEY_SIZE_SUBSCRIPTION, size).apply()

    //region file
    private fun writeToFile(date: Long){
        if (ContextCompat.checkSelfPermission(activity, PermissionType.READ_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED  ||
            ContextCompat.checkSelfPermission(activity, PermissionType.WRITE_EXTERNAL_STORAGE.permission) ==  PackageManager.PERMISSION_DENIED )
            return
        if (readFile() == null)
        try {
            Log.d(tag, "file pre write")
            val outputStream = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, FILENAME)
                FileOutputStream(file)
            } else {
                val resolver = activity.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "testing_phone.txt")
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
                        if (audioCursor.getString(s) == "testing_phone.txt") {
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
    override fun launchBillingBook() = launch(BILLING_BOOK)
    override fun launchBillingBookVIP() = launch(BILLING_BOOK_VIP)
    override fun launchSaleBillingBookVIP() = launch(BILLING_SALE_BOOK_VIP)
    override fun launchSaleBillingMonth() = launch(BILLING_SALE_MONTH)
    override fun launchSaleBillingYear() = launch(BILLING_SALE_YEAR)

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
                    val purchasesListSubs = queryPurchasesSUBS() //запрос о покупках
                    val purchasesListInApp = queryPurchasesINAPP()//запрос о покупках

                    when {
                        purchasesListInApp?.any{ it?.sku == BILLING_BOOK_VIP || it?.sku == BILLING_SALE_BOOK_VIP} == true -> {
                            isHaveBook.value = true
                            setStateSubscription(StateSubscription.HAVE_SUB)
                        }
                        purchasesListSubs?.isNotEmpty() == true -> setStateSubscription(StateSubscription.HAVE_SUB)
                        else -> setEndedSubByBilling()
                    }

                    if (purchasesListInApp?.any{ it?.sku == BILLING_BOOK} == true){ isHaveBook.value = true}

                    Log.d(tag, "success Subs $purchasesListSubs")
                    Log.d(tag, "success InApp $purchasesListInApp")
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
        const val HOT_OFFER_TIME = 500000L// 30 минут в миллисекундах 1800000L
        const val FILENAME = "testing_phone.txt"
        const val BILLING_MONTH = "month"
        const val BILLING_YEAR = "year"
        const val BILLING_SALE_MONTH = "sale_month"
        const val BILLING_SALE_YEAR = "sale_year"
        const val BILLING_SALE_BOOK_VIP = "sale_book_vip"
        const val BILLING_BOOK_VIP = "book_vip"
        const val BILLING_BOOK = "book"

        const val KEY_PASSWORD = "password"
        const val KEY_STATE_SUBSCRIPTION = "KEY_STATE_SUBSCRIPTION"
        const val KEY_SUBSCRIPTION = "KEY_SUBSCRIPTION"
        const val KEY_SIZE_SUBSCRIPTION = "KEY_SIZE_SUBSCRIPTION"
        const val KEY_IS_SUBSCRIPTION = "KEY_IS_SUBSCRIPTION"
        const val KEY_HAVE_SUBSCRIPTION = "KEY_HAVE_SUBSCRIPTION"
        const val KEY_IS_HOT_OFFER = "KEY_IS_HOT_OFFER"
        const val KEY_HOT_OFFER_TIME = "KEY_HOT_OFFER_TIME"
    }
}