package uz.nurlibaydev.moneymanager.presentation.auth.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uz.nurlibaydev.moneymanager.domain.MainRepository
import uz.nurlibaydev.moneymanager.utils.Resource

class SignUpViewModel(
    private val mainRepository: MainRepository
) : ViewModel() {
    private var _signUp: MutableLiveData<Resource<Any?>> = MutableLiveData()
    val signUpStatus: LiveData<Resource<Any?>> get() = _signUp

    fun signUp(fullName: String, email: String, password: String) {
        _signUp.value = Resource.loading()
        mainRepository.signUp(fullName, email, password,
            {
                _signUp.value = Resource.success(null)
            }, {
                _signUp.value = Resource.error(it)
            }
        )
    }
}