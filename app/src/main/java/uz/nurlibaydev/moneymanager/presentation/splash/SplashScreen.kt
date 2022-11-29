package uz.nurlibaydev.moneymanager.presentation.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.data.pref.SharedPref

@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment(R.layout.screen_splash) {

    private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }
    private val pref: SharedPref by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenResumed {
            delay(1500)
            if (pref.isSigned) {
                navController.navigate(SplashScreenDirections.actionSplashScreenToMainContainer())
            } else {
                navController.navigate(SplashScreenDirections.actionSplashScreenToSignInScreen())
            }
        }
    }
}