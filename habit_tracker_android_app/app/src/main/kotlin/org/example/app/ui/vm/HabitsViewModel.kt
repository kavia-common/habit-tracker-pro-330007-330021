package org.example.app.ui.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.app.domain.HabitTrackerRepository
import org.example.app.domain.model.Habit
import org.example.app.domain.model.HabitId
import org.example.app.domain.model.TodaySummary

class HabitsViewModel(
    private val repository: HabitTrackerRepository
) : ViewModel() {

    private val _habits = MutableLiveData<List<Habit>>(emptyList())
    val habits: LiveData<List<Habit>> = _habits

    private val _todaySummary = MutableLiveData(TodaySummary(0, 0, 0.0))
    val todaySummary: LiveData<TodaySummary> = _todaySummary

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _oneShotMessage = MutableLiveData<String?>(null)
    val oneShotMessage: LiveData<String?> = _oneShotMessage

    // PUBLIC_INTERFACE
    fun load() {
        /** Loads local data first and then attempts a backend sync (best-effort). */
        viewModelScope.launch {
            loadLocalOnly()
            syncFromBackend()
        }
    }

    // PUBLIC_INTERFACE
    fun loadLocalOnly() {
        /** Loads from local persistence only. */
        viewModelScope.launch {
            val list = repository.getHabitsLocal()
            _habits.value = list
            _todaySummary.value = repository.getTodaySummaryLocal()
        }
    }

    // PUBLIC_INTERFACE
    fun syncFromBackend() {
        /**
         * Syncs from backend (best effort). If backend is unavailable, keeps local data.
         * Errors are surfaced via oneShotMessage for debuggability.
         */
        viewModelScope.launch {
            _loading.value = true
            val result = repository.syncFromBackend()
            _loading.value = false
            if (result.isFailure) {
                _oneShotMessage.value = result.exceptionOrNull()?.message ?: "Offline: showing saved data."
            }
            loadLocalOnly()
        }
    }

    // PUBLIC_INTERFACE
    fun setDoneToday(habitId: HabitId, done: Boolean) {
        /** Marks the habit done/undone for today; persists locally and attempts backend push. */
        viewModelScope.launch {
            val result = repository.setDoneToday(habitId, done)
            if (result.isFailure) {
                _oneShotMessage.value = result.exceptionOrNull()?.message ?: "Failed to update."
            }
            loadLocalOnly()
        }
    }

    // PUBLIC_INTERFACE
    fun deleteHabit(habitId: HabitId) {
        /** Deletes a habit locally and attempts backend deletion. */
        viewModelScope.launch {
            val result = repository.deleteHabit(habitId)
            if (result.isFailure) {
                _oneShotMessage.value = result.exceptionOrNull()?.message ?: "Failed to delete."
            }
            loadLocalOnly()
        }
    }

    // PUBLIC_INTERFACE
    fun consumeOneShotMessage() {
        /** Clears the one-shot message after UI has shown it. */
        _oneShotMessage.value = null
    }
}
