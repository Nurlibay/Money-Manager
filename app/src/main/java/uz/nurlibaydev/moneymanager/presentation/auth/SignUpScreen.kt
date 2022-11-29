package uz.nurlibaydev.moneymanager.presentation.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.auth.FirebaseAuth
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.databinding.ScreenSignupBinding
import uz.nurlibaydev.moneymanager.utils.extenions.onClick
import uz.nurlibaydev.moneymanager.utils.extenions.showMessage

class SignUpScreen : Fragment(R.layout.screen_signup) {

    private val binding: ScreenSignupBinding by viewBinding()
    private val navController: NavController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            firebaseAuth = FirebaseAuth.getInstance()
            signupBtn.setOnClickListener {
                val email = email.text.toString()
                val pass = password.text.toString()
                val confirmPass = passwordRetype.text.toString()

                if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                        if (pass == confirmPass) {
                            binding.progressBar.visibility = View.VISIBLE
                            firebaseAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        navController.navigate(SignUpScreenDirections.actionSignUpScreenToMainContainer())
                                        showMessage("Sign Up Successful")
                                        binding.progressBar.visibility = View.GONE
                                    } else {
                                        binding.progressBar.visibility = View.GONE
                                        showMessage(it.exception.toString())
                                    }
                                }
                        } else {
                            showMessage("Password id not matching!")
                        }
                    } else {
                        showMessage("Empty fields are no allowed!")
                    }
                } else {
                    showMessage("Invalid or Empty Email")
                }
            }
            haveAccount.onClick {
                navController.popBackStack()
            }
        }
    }
}