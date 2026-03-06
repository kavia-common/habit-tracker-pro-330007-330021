package org.example.app.ui.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.app.domain.HabitTrackerRepository
import org.example.app.domain.model.Habit
import org.example.app.domain.model.HabitId

data class HabitForm(
    val habitId: HabitId?,
    val name: String,
    val description: String
)

class EditHabitViewModel(
    private val repository: HabitTrackerRepository
) : ViewModel() {

    private val _form = MutableLiveData(HabitForm(null, "", ""))
    val form: LiveData<HabitForm> = _form

    private val _finishEvent = MutableLiveData<Boolean?>(null)
    val finishEvent: LiveData<Boolean?> = _finishEvent

    private val _oneShotMessage = MutableLiveData<String?>(null)
    val oneShotMessage: LiveData<String?> = _oneShotMessage

    // PUBLIC_INTERFACE
    fun loadHabit(habitId: HabitId) {
        /** Loads an existing habit into the form for editing. */
        viewModelScope.launch {
            val habit = repository.getHabitLocal(habitId)
            if (habit == null) {
                _oneShotMessage.value = "Habit not found."
                return@launch
            }
            _form.value = HabitForm(habit.id, habit.name, habit.description.orEmpty())
        }
    }

    // PUBLIC_INTERFACE
    fun save(name: String, description: String?) {
        /**
         * Saves the habit. Creates new if form has no habitId, otherwise updates existing.
         * Validates name is not blank and returns errors via oneShotMessage.
         */
        viewModelScope.launch {
            if (name.isBlank()) {
                _oneShotMessage.value = "Name is required."
                return@launch
            }

            val current = _form.value ?: HabitForm(null, "", "")
            val result = if (current.habitId == null) {
                repository.createHabit(
                    name = name,
                    description = description
                )
            } else {
                repository.updateHabit(
                    habit = Habit(
                        id = current.habitId,
                        name = name,
                        description = description,
                        doneToday = false,
                        currentStreakDays = 0
                    )
                )
            }

            if (result.isFailure) {
                _oneShotMessage.value = result.exceptionOrNull()?.message ?: "Save failed."
                return@launch
            }

            _finishEvent.value = true
        }
    }

    // PUBLIC_INTERFACE
    fun consumeOneShotMessage() {
        /** Clears one-shot message. */
        _oneShotMessage.value = null
    }
}
