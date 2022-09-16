package com.example.finito.features.labels.presentation

import androidx.lifecycle.ViewModel
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@HiltViewModel
class SharedLabelViewModel @Inject constructor(
    private val labelUseCases: LabelUseCases
) : ViewModel() {

    var dialogType by mutableStateOf<SharedLabelEvent.DialogType?>(null)
        private set

    fun onEvent(event: SharedLabelEvent) {
        when (event) {
            is SharedLabelEvent.ShowDialog -> TODO()
            is SharedLabelEvent.ChangeName -> TODO()
            SharedLabelEvent.CreateLabel -> TODO()
            is SharedLabelEvent.DeleteLabel -> TODO()
        }
    }
}