package uz.nurlibaydev.moneymanager.presentation.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.auth.FirebaseAuth
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.databinding.ScreenForgetPasswordBinding
import uz.nurlibaydev.moneymanager.utils.extenions.onClick
import uz.nurlibaydev.moneymanager.utils.extenions.showMessage

/**
 *  Created by Nurlibay Koshkinbaev on 29/11/2022 13:27
 */

class ForgetPasswordScreen : Fragment(R.layout.screen_forget_password) {

    private val binding: ScreenForgetPasswordBinding by viewBinding()
    private val navController: NavController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val submitButton: Button = binding.forgotPassBtn
        val etEmail: EditText = binding.emailForgotPass

        submitButton.onClick {
            val email: String = etEmail.text.toString().trim { it <= ' ' }
            if (email.isEmpty()) {
                showMessage("Please enter email address!")
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showMessage("Check your email! (Including Spam)")
                            navController.popBackStack()
                        } else {
                            showMessage(task.exception!!.message.toString())
                        }
                    }
            }
        }
    }
}