package edu.unikom.herbamedjabar.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan password tidak boleh kosong.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                _authState.postValue(AuthState.Authenticated)
            } catch (e: Exception) {
                _authState.postValue(AuthState.Error(e.message ?: "Login gagal"))
            }
        }
    }

    fun registerUser(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _authState.value = AuthState.Error("Semua kolom harus diisi.")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Password dan konfirmasi password tidak cocok.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password minimal harus 6 karakter.")
            return
        }


        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                _authState.postValue(AuthState.Authenticated)

                val user = firebaseAuth.currentUser

                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }

                user!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "User profile updated.")
                        }
                    }
            } catch (e: Exception) {
                _authState.postValue(AuthState.Error(e.message ?: "Registrasi gagal"))
            }
        }
    }

//    fun logoutUser() {
//        _authState.value = AuthState.Loading
//        viewModelScope.launch {
//            try {
//                firebaseAuth.signOut()
//                _authState.postValue(AuthState.Idle)
//            } catch (e: Exception) {
//                _authState.postValue(AuthState.Error(e.message ?: "Signout gagal"))
//
//            }
//        }
//    }
}

// Extension function untuk menggunakan coroutines dengan Firebase Auth
suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
    return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result, null)
            } else {
                cont.resumeWithException(task.exception!!)
            }
        }
    }
}
