package es.mirumi.es.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.mirumi.es.data.repository.repositories.RepositoryLogin

class LoginViewModelFactory(
    private val repository: RepositoryLogin,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
