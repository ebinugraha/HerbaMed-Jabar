package edu.unikom.herbamedjabar.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unikom.herbamedjabar.repository.PlantRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val plantRepository: PlantRepository
) : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            // plantRepository.logout()
        }
    }
}