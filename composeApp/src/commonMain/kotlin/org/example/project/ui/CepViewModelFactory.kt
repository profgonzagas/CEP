//package br.edu.utfpr.consultacep.ui
//package org.example.project.data
package org.example.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.example.project.data.repository.CepRepository
import org.example.project.data.validator.CepValidator

class CepViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CepViewModel::class.java)) {
            return CepViewModel(
                cepRepository = CepRepository(),
                cepValidator = CepValidator()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}