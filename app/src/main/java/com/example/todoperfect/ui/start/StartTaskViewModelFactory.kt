package com.example.todoperfect.ui.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoperfect.logic.model.Task

class StartTaskViewModelFactory(private val currentTask: Task)  : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StartTaskViewModel(currentTask) as T
    }
}