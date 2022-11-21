package uz.nurlibaydev.moneymanager.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import uz.nurlibaydev.moneymanager.BuildConfig
import uz.nurlibaydev.moneymanager.di.dataModule
import uz.nurlibaydev.moneymanager.di.repositoryModule
import uz.nurlibaydev.moneymanager.di.sharedPrefModule
import uz.nurlibaydev.moneymanager.di.viewModelModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        //Koin
        val modules = listOf(
            dataModule, sharedPrefModule, repositoryModule, viewModelModule
        )
        startKoin {
            androidLogger()
            androidContext(this@App)
            androidFileProperties()
            koin.loadModules(modules)
        }
    }
}