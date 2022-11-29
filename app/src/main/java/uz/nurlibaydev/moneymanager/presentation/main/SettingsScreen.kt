package uz.nurlibaydev.moneymanager.presentation.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.databinding.ScreenSettingsBinding
import uz.nurlibaydev.moneymanager.utils.extenions.onClick

class SettingsScreen: Fragment(R.layout.screen_settings) {

    private val binding: ScreenSettingsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            tvLang.text = resources.getString(R.string.lang_text)
            containerSelectLanguage.onClick {
                val dialog = LanguageDialog()
                dialog.show(requireActivity().supportFragmentManager, "LanguageDialog")
            }
            iconBack.onClick {
                findNavController().popBackStack()
            }
        }
    }
}