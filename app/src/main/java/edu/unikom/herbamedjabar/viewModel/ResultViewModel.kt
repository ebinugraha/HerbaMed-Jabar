package edu.unikom.herbamedjabar.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unikom.herbamedjabar.repository.PostRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _postResult = MutableLiveData<Result<Unit>>()
    val postResult: LiveData<Result<Unit>> = _postResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun createPostFromScan(imageUri: Uri, plantName: String, description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUser
                if (user == null) {
                    _postResult.value = Result.failure(Exception("User not logged in"))
                    _isLoading.value = false
                    return@launch
                }

                postRepository.createPost(
                    userId = user.uid,
                    username = user.displayName ?: "Anonymous",
                    userProfilePictureUrl = user.photoUrl?.toString(),
                    imageUri = imageUri,
                    plantName = plantName,
                    description = description
                )
                _postResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _postResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
