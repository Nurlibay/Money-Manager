package uz.nurlibaydev.moneymanager.data.pref

import android.content.Context
import android.content.SharedPreferences
import uz.nurlibaydev.moneymanager.utils.Constants
import uz.nurlibaydev.onlinemathgame.utils.BooleanPreference
import uz.nurlibaydev.onlinemathgame.utils.StringPreference

class SharedPref(context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)

    var isSigned: Boolean by BooleanPreference(pref, false)

    var language: String by StringPreference(pref, "ru")
}