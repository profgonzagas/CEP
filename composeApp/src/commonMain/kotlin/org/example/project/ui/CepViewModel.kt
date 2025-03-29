//package br.edu.utfpr.consultacep.ui
package org.example.project.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
//import org.example.project.data.model.CepResult
import org.example.project.data.repository.CepRepository
import org.example.project.data.validator.CepValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.example.project.Endereco

class CepViewModel(
    private val cepRepository: CepRepository,
    private val cepValidator: CepValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(CepFormState())
    val uiState: StateFlow<CepFormState> = _uiState

    fun buscarCep(cep: String) {
        if (!_uiState.value.isDataValid || _uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, hasErrorLoading = false) }

        viewModelScope.launch {
            try {
                val endereco = cepRepository.buscarCep(cep)
                _uiState.update {
                    it.copy(isLoading = false, endereco = endereco)
                }
            } catch (ex: Exception) {
               // Log.e("CepViewModel", "Erro ao consultar o CEP", ex)
                _uiState.update { it.copy(isLoading = false, hasErrorLoading = true) }
            }
        }
    }

    fun onCepChanged(cep: String) {
        _uiState.update { it.copy(isDataValid = cepValidator.verificarCep(cep)) }
    }
}
