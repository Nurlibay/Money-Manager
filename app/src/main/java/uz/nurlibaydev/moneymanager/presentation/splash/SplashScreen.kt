package uz.nurlibaydev.moneymanager.presentation.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.data.source.pref.SharedPref

@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment(R.layout.screen_splash) {

    private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = SharedPref(requireContext())
        lifecycleScope.launch {
            delay(1500)
            if (sharedPref.isSigned) {
                navController.navigate(SplashScreenDirections.actionSplashScreenToMainContainer())
            } else {
                navController.navigate(SplashScreenDirections.actionSplashScreenToSignInScreen())
            }
        }
    }
}