package com.aks.notpress.utils

import android.content.Context
import android.content.SharedPreferences

interface Preference{
    fun getPassword():List<Boolean>
    fun setPassword(list: List<Boolean>)
}

class PreferencesBasket(context: Context): Preference{
    private val preferences: SharedPreferences = context.getSharedPreferences(javaClass.simpleName, Context.MODE_PRIVATE)

    override fun getPassword(): List<Boolean> {
        val password: String = preferences.getString(KEY, "000000000")?:"000000000"
        return password.flatMap { listOf<Boolean>(it=='0') }
    }

    override fun setPassword(list: List<Boolean>) {
        val s = list.map { if (it) 1 else 0 }.joinToString()
        preferences.edit().putString(KEY, s).apply()
    }
    companion object {
        const val KEY = "password"
    }
}