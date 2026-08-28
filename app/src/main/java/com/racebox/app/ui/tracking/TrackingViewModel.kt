package com.racebox.app.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.racebox.app.domain.race.TrackingState
import com.racebox.app.repository.RaceRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackingViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    val state: StateFlow<TrackingState?> = repository.trackingState

    fun beginLap() {
        viewModelScope.launch { repository.beginLap() }
    }

    fun endLap() {
        viewModelScope.launch { repository.endLap() }
    }
}