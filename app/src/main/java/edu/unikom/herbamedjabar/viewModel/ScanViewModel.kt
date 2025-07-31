package edu.unikom.herbamedjabar.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unikom.herbamedjabar.useCase.AnalyzePlantUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

// Definisikan state untuk UI
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val data: String) : UiState()
    data class Error(val message: String) : UiState()
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val analyzePlantUseCase: AnalyzePlantUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    fun analyzeImage(bitmap: Bitmap) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = analyzePlantUseCase(bitmap)
            result.onSuccess { data ->
                _uiState.postValue(UiState.Success(data))
            }.onFailure { error ->
                _uiState.postValue(UiState.Error(error.message ?: "Terjadi kesalahan tidak diketahui"))
            }
        }
    }
}
