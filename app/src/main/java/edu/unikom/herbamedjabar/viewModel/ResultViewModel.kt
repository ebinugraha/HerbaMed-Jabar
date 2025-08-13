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

    private fun parsePlantData(text: String): Map<String, String> {
        val dataMap = mutableMapOf<String, String>()
        val originalText = text

        val namePattern = Regex("🌿(.*?)\\*Nama Ilmiah", setOf(RegexOption.DOT_MATCHES_ALL))
        dataMap["plantName"] = namePattern.find(originalText)?.destructured?.let { (name) ->
            name.trim()
        } ?: originalText.lines().firstOrNull()?.replace(Regex("[#*🌿]"), "")?.trim() ?: "Nama tidak ditemukan" // Fallback jika pola gagal

        val descriptionPattern = Regex("### 📝 Deskripsi(.*?)(?=### 🩺 Potensi Manfaat & Kegunaan|### ⚠️ Peringatan & Efek Samping|$)", setOf(RegexOption.DOT_MATCHES_ALL))
        val benefitPattern = Regex("### 🩺 Potensi Manfaat & Kegunaan(.*?)(?=### 📝 Deskripsi|### ⚠️ Peringatan & Efek Samping|$)", setOf(RegexOption.DOT_MATCHES_ALL))
        val warningPattern = Regex("### ⚠️ Peringatan & Efek Samping(.*?)(?=### 📝 Deskripsi|### 🩺 Potensi Manfaat & Kegunaan|$)", setOf(RegexOption.DOT_MATCHES_ALL))

        descriptionPattern.find(originalText)?.destructured?.let { (desc) ->
            dataMap["description"] = desc.replace("---", "").trim()
        }
        benefitPattern.find(originalText)?.destructured?.let { (benefit) ->
            dataMap["benefit"] = benefit.replace("---", "").trim()
        }
        warningPattern.find(originalText)?.destructured?.let { (warning) ->
            dataMap["warning"] = warning.replace("---", "").trim()
        }

        dataMap.putIfAbsent("description", "")
        dataMap.putIfAbsent("benefit", "")
        dataMap.putIfAbsent("warning", "")

        return dataMap
    }

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

                val parsedData = parsePlantData(description)

                postRepository.createPost(
                    userId = user.uid,
                    username = user.displayName ?: "Anonymous",
                    userProfilePictureUrl = user.photoUrl?.toString(),
                    imageUri = imageUri,
                    plantName = plantName,
                    description = description,
                    parsedData = parsedData
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
