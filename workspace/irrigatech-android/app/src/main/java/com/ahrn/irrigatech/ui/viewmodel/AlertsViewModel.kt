package com.ahrn.irrigatech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahrn.irrigatech.data.model.AlertEvent
import com.ahrn.irrigatech.data.repository.AlertRepository
import com.ahrn.irrigatech.data.model.AlertKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlertsUiState(
    val filter: AlertKind? = null,
    val alerts: List<AlertEvent> = emptyList(),
)

class AlertsViewModel(private val repository: AlertRepository) : ViewModel() {

    private val _filter = MutableStateFlow<AlertKind?>(null)

    val alerts: StateFlow<List<AlertEvent>> = repository.alerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val filtered: StateFlow<AlertsUiState> = combine(repository.alerts, _filter) { list, filter ->
        AlertsUiState(
            filter = filter,
            alerts = filter?.let { kind -> list.filter { it.kind == kind } } ?: list,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlertsUiState(),
    )

    fun setFilter(kind: AlertKind?) {
        _filter.value = kind
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clear() }
    }
}
