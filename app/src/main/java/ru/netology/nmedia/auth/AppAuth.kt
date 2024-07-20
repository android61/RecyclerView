package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.error.ApiException
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val TOKEN_KEY = "TOKEN_KEY"
    private val ID_KEY = "ID_KEY"
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state: MutableStateFlow<AuthState?>


    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)

        _state = if (token == null || !prefs.contains(ID_KEY)) {
            prefs.edit { clear() }
            MutableStateFlow(null)
        } else {
            MutableStateFlow(AuthState(id, token))
        }
        sendPushToken()
    }

    val state = _state.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        prefs.edit {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
        _state.value = AuthState(id, token)
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        prefs.edit { clear() }
        _state.value = null
        sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getApiService(): ApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val entryPoint =
                    EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().sendPushToken(
                    PushToken(
                        token ?: FirebaseMessaging.getInstance().token.await()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun update(login: String, password: String) {
        try {
            val entryPoint =
                EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response = entryPoint.getApiService().updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiException(response.code(), response.message())
            setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            println(e)
            throw UnknownException
        }
    }

    suspend fun register(login: String, password: String, name: String) {
        try {
            val entryPoint =
                EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response = entryPoint.getApiService().registerUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val newAuth =
                response.body() ?: throw ApiException(response.code(), response.message())
            setAuth(newAuth.id, newAuth.token)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    suspend fun registerWithPhoto(login: String, password: String, name: String, file: File) {
        try {
            val fileData = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
            val entryPoint =
                EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response = entryPoint.getApiService().registerWithPhoto(
                login.toRequestBody(),
                password.toRequestBody(),
                name.toRequestBody(),
                fileData
            )

            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiException(response.code(), response.message())
            setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}