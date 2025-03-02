package com.tstool.trackexpenses

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.multidex.MultiDexApplication
import com.orhanobut.hawk.Hawk
import com.tstool.trackexpenses.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : MultiDexApplication(), DefaultLifecycleObserver, ViewModelStoreOwner {

    private val appViewModelStore: ViewModelStore by lazy { ViewModelStore() }

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        instance = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initHawk()
        initSharedPreferences()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }

    private fun initHawk() {
        Hawk.init(applicationContext).build()
    }

    private fun initSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isFirstApp", true).apply()
        registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())
    }

    class AdjustLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}

    }
}
//https://grok.com/share/bGVnYWN5_a714b89f-e6f9-4f7e-a37b-6dce0407b20c