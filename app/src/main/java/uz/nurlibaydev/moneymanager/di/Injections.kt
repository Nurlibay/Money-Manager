package uz.nurlibaydev.moneymanager.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uz.nurlibaydev.moneymanager.data.pref.SharedPref

val sharedPrefModule = module {
    single { SharedPref(androidApplication().applicationContext) }
}