package com.asahina.test

import android.content.Context
import android.support.multidex.MultiDexApplication


/**
 * アプリ用にカスタムした Application。
 */
open class BaseApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        context = applicationContext


    }


    companion object {

        lateinit var context: Context

    }
}
