package com.example.finito.features.labels.presentation.screen.createlabel

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateLabelViewModel @Inject constructor(
    private val labelUseCases: LabelUseCases
) : ViewModel() {

    var nameState by mutableStateOf(TextFieldState())
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: CreateLabelEvent) {
        when (event) {
            is CreateLabelEvent.ChangeName -> onChangeName(event.name)
            CreateLabelEvent.CreateLabel -> onCreateLabel()
        }
    }

    private fun onChangeName(name: String) {
        nameState = nameState.copy(value = name)
    }

    private fun onCreateLabel() = viewModelScope.launch {
        Label(name = nameState.value).let {
            labelUseCases.createLabel(it)
        }.also {
            _eventFlow.emit(Event.ShowSnackbar(message = R.string.label_created))
        }
    }

    sealed class Event {
        data class ShowSnackbar(@StringRes val message: Int) : Event()
    }
}