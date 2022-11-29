package uz.nurlibaydev.moneymanager.presentation.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import org.koin.android.ext.android.inject
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.data.pref.SharedPref
import uz.nurlibaydev.moneymanager.databinding.ContainerMainBinding
import uz.nurlibaydev.moneymanager.utils.extenions.onClick

class MainContainer : Fragment(R.layout.container_main) {

    private val binding: ContainerMainBinding by viewBinding()
    private lateinit var navController: NavController
    private val sharedPref: SharedPref by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(requireActivity(), R.id.fragment_container_view)
        binding.bottomNavMenu.setupWithNavController(navController)
        sharedPref.isSigned = true


//        binding.chipAppBar.setItemSelected(R.id.transactionScreen, true)
//        binding.chipAppBar.setOnItemSelectedListener {
//            when (it) {
//                R.id.transactionScreen -> {
//                    navController.navigate(R.id.transactionScreen)
//                }
//                R.id.accountScreen -> {
//                    navController.navigate(R.id.accountScreen)
//                }
//            }
//        }
//        binding.fab.onClick {
//            navController.navigate(Gloaal)
//        }
    }
}