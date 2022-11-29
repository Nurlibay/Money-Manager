package uz.nurlibaydev.moneymanager.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import uz.nurlibaydev.moneymanager.R
import uz.nurlibaydev.moneymanager.databinding.ScreenSigninBinding
import uz.nurlibaydev.moneymanager.utils.extenions.onClick
import uz.nurlibaydev.moneymanager.utils.extenions.showMessage

class SignInScreen : Fragment(R.layout.screen_signin) {

    private val binding: ScreenSigninBinding by viewBinding()
    private val navController: NavController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            createAccount.onClick {
                navController.navigate(SignInScreenDirections.actionSignInScreenToSignUpScreen())
            }
            forgotPassword.onClick {
                navController.navigate(SignInScreenDirections.actionSignInScreenToForgetPasswordScreen())
            }
            emailLogin()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("857837890100-l8j2qtb9ft7mb42dnm1l342l62ero0ij.apps.googleusercontent.com")
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            googleSignInBtn.onClick {
                googleSignInClient.signOut()
                signIn()
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                e.localizedMessage?.let { showMessage(it) }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        binding.progressBar.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navController.navigate(SignInScreenDirections.actionSignInScreenToMainContainer())
                    showMessage(getString(R.string.login_successful))
                    binding.progressBar.visibility = View.GONE
                } else {
                    showMessage(task.exception?.message)
                }
            }
    }

    companion object {
        const val RC_SIGN_IN = 1001
    }

    private fun emailLogin() {
        binding.apply {
            loginBtn.onClick {
                val email = email.text.toString()
                val pass = password.text.toString()
                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    progressBar.visibility = View.VISIBLE
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            navController.navigate(SignInScreenDirections.actionSignInScreenToMainContainer())
                            showMessage(getString(R.string.login_successful))
                            progressBar.visibility = View.GONE
                        } else {
                            progressBar.visibility = View.GONE
                            showMessage("Login Failed!")
                        }
                    }
                } else {
                    showMessage("Empty fields are no allowed")
                }
            }
        }
    }

//    override fun onStart() {
//        super.onStart()
//        if (auth.currentUser != null) {
//            Intent(requireContext(), MainActivity::class.java).also {
//                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(it)
//            }
//        }
//    }
}