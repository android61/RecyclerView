package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PhotoModel
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth
): ViewModel() {
    val state = appAuth.state
        .asLiveData()
    val authorized: Boolean
        get() = state.value != null


    private val _error = SingleLiveEvent<Throwable>()
    val error: LiveData<Throwable>
        get() = _error

    private val noPhoto = PhotoModel(null, null)

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo


    fun updateUser(login: String, password: String) =
        viewModelScope.launch {
            try {
                appAuth.update(login, password)
            } catch (e: Exception) {
                _error.value = e
            }
        }

    fun registerUser(login: String, password: String, name: String) = viewModelScope.launch {
        try {
            appAuth.register(login, password, name)
        } catch (e: Exception) {
            _error.value = e
        }
    }

    fun registerWithPhoto(login: String, password: String, name: String, file: File) =
        viewModelScope.launch {
            try {
                appAuth.registerWithPhoto(login, password, name, file)
            } catch (e: Exception) {
                println(e)
                throw AppError.from(e)
            }
        }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }
}