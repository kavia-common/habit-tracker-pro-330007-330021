package org.example.app.ui.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.app.domain.HabitTrackerRepository
import org.example.app.domain.model.HabitStat
import org.example.app.domain.model.OverallStats

class StatsViewModel(
    private val repository: HabitTrackerRepository
) : ViewModel() {

    private val _overall = MutableLiveData(OverallStats(0, 0, 0.0))
    val overall: LiveData<OverallStats> = _overall

    private val _perHabit = MutableLiveData<List<HabitStat>>(emptyList())
    val perHabit: LiveData<List<HabitStat>> = _perHabit

    // PUBLIC_INTERFACE
    fun load() {
        /** Loads statistics from local DB (fast, offline-capable). */
        viewModelScope.launch {
            _overall.value = repository.getOverallStatsLocal()
            _perHabit.value = repository.getPerHabitStatsLocal()
        }
    }
}
