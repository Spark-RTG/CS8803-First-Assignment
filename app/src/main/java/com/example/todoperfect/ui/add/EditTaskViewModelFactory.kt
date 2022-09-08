package com.example.todoperfect.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoperfect.logic.model.Task

class EditTaskViewModelFactory(private val currentTask: Task?)  : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EditTaskViewModel(currentTask) as T
    }
}