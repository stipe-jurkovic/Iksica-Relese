package com.tstudioz.iksica.SignInScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tstudioz.iksica.Data.Models.PaperUser
import com.tstudioz.iksica.Data.Repository
import com.tstudioz.iksica.Utils.Exceptions.EmptyInputFields
import com.tstudioz.iksica.Utils.Exceptions.NoNetworkException
import com.tstudioz.iksica.Utils.Exceptions.WrongCredsException
import com.tstudioz.iksica.Utils.LiveEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

/**
 * Created by etino7 on 12/8/2019.
 */
class SignInViewModel : ViewModel() {

    private val repository: Repository by inject(Repository::class.java)

    private val userLogged: MutableLiveData<Boolean>
    private val userData: MutableLiveData<PaperUser>
    private val errorMessage: LiveEvent<String> = LiveEvent()

    private val handler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is WrongCredsException -> errorMessage.postValue(throwable.message)
            is NoNetworkException -> errorMessage.postValue(throwable.message)
            is EmptyInputFields -> errorMessage.value = throwable.message
            else -> Timber.e(throwable)
        }
    }

    companion object {
        private const val USER_LOGGED_KEY = "user_logged"
    }

    init {
        userData = repository.getUserData()
        userLogged = repository.getUserAlreadyLogged()
    }

    fun authenticateCredentials(data: Pair<String, String>) {

        viewModelScope.launch(handler) {
            if (!checkInputs(data)) return@launch

            withContext(context = Dispatchers.IO) {
                val success = repository.verifyUserInput(data.first, data.second)
                if (success) {
                    repository.insertUser(PaperUser(1, data.first, data.second))
                    repository.setUserLogged(USER_LOGGED_KEY, true)
                }
            }

            repository.checkIfUserIsAlreadyLogged()
        }
    }

    private fun checkInputs(data: Pair<String, String>): Boolean {
        if (data.first.isEmpty() || data.second.isEmpty()) {
            throw EmptyInputFields()
        }

        return true
    }

    fun isUserLoggedAlready(): LiveData<Boolean> {
        return userLogged
    }

    fun getErrors(): LiveData<String> {
        return errorMessage
    }

}