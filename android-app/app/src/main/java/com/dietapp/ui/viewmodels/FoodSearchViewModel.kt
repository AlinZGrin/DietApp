package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.data.entities.Food
import com.dietapp.data.repository.USDARepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodSearchUiState(
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val mealType: String = ""
)

@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    private val usdaRepository: USDARepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodSearchUiState())
    val uiState: StateFlow<FoodSearchUiState> = _uiState.asStateFlow()
    
    // USDA API Key (in production this should be in BuildConfig or secure storage)
    private val usdaApiKey = "fCnWsoZ4D22bLKBaEOrmTfGpYhYCV49MWWWGxeHt"

    fun setMealType(mealType: String) {
        _uiState.value = _uiState.value.copy(mealType = mealType)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isNotBlank() && query.length >= 3) {
            searchFoods(query)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }

    private fun searchFoods(query: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val foods = usdaRepository.searchFoodsByName(query, usdaApiKey)
                
                _uiState.value = _uiState.value.copy(
                    searchResults = foods,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to search foods: ${e.message}",
                    searchResults = emptyList()
                )
            }
        }
    }
}
