package org.example.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.example.app.domain.HabitTrackerRepository

class EditHabitViewModelFactory(
    private val repository: HabitTrackerRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditHabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditHabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
