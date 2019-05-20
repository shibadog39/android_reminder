package com.asahina.test.preference

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.asahina.test.BaseApplication
import com.asahina.test.item.ReminderItemList
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.`$Gson$Types`


class Preferences {

    private val KEY_REMINDER_LIST = "KEY_REMINDER_LIST"

    fun saveReminderList(list: List<ReminderItemList>){
        setObject(KEY_REMINDER_LIST, list)
    }

    fun loadReminderList(): MutableList<ReminderItemList> {
        return getObjectList(KEY_REMINDER_LIST, ReminderItemList::class.java)
    }

    //region ラッパーメソッドなどの定義
    private val SHARED_PREFERENCES_NAME = "PreferencesUtils"

    private fun getPreference(): SharedPreferences {
        return BaseApplication.context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
    }

    private fun setObject(key: String, value: Any) {
        val editor = getPreference().edit()
        val json = createGson().toJson(value)
        editor.putString(key, json)
        editor.apply()
    }

    private fun <T> getObject(key: String, type: Class<T>, default: T?): T? {
        val json = getPreference().getString(key, null)
        return json?.let {
            createGson().fromJson(it, type)
        } ?: run {
            default
        }
    }

    private fun <T> getObjectList(key: String, type: Class<T>): MutableList<T> {
        val json = getPreference().getString(key, null)
        return json?.let {
            val typeClass = `$Gson$Types`.newParameterizedTypeWithOwner(null, MutableList::class.java, type)
            createGson().fromJson(it, typeClass) as MutableList<T>
        } ?: run {
            ArrayList<T>()
        }
    }

    private fun createGson(): Gson {
        val builder = GsonBuilder()
        builder.enableComplexMapKeySerialization()
        return builder.create()
    }
    //endregion
}