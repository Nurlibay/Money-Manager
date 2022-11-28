package uz.nurlibaydev.moneymanager.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import uz.nurlibaydev.moneymanager.data.source.helper.AuthHelper
import uz.nurlibaydev.moneymanager.data.source.pref.SharedPref
import uz.nurlibaydev.moneymanager.domain.MainRepository
import uz.nurlibaydev.moneymanager.domain.MainRepositoryImpl
import uz.nurlibaydev.moneymanager.presentation.auth.signin.SignInViewModel
import uz.nurlibaydev.moneymanager.presentation.auth.signup.SignUpViewModel

val dataModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    single { AuthHelper(get(), get()) }
}

val sharedPrefModule = module {
    single { SharedPref(androidApplication().applicationContext) }
}

val repositoryModule = module {
    single<MainRepository> { MainRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { SignInViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
}