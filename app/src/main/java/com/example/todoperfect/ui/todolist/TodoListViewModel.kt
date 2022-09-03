package com.example.todoperfect.ui.todolist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.todoperfect.LogUtil
import com.example.todoperfect.TodoPerfectApplication
import com.example.todoperfect.logic.Cloud
import com.example.todoperfect.logic.Repository
import com.example.todoperfect.logic.model.Task
import com.example.todoperfect.logic.model.TaskPullRequest
import com.example.todoperfect.logic.model.TaskPullRequestBodyJSON
import java.util.*


class TodoListViewModel : ViewModel() {
    private val current = MutableLiveData<Calendar>()
    private val lastRefreshDate = MutableLiveData<Calendar>()
    val taskLiveData = Transformations.switchMap(current) {
        Repository.refreshTaskList(it)
    }

    val backendLiveData = Transformations.switchMap(lastRefreshDate) {
        Cloud.pullAllTasks(TaskPullRequest(
            TaskPullRequestBodyJSON(TodoPerfectApplication.user!!.email)))
    }

    fun refreshFromCloud() {
        lastRefreshDate.value = GregorianCalendar()
    }
    fun refresh() {
        current.value = GregorianCalendar()
    }

    fun insertTask(task: Task) {
        Repository.insertTask(task)
        refresh()
    }

    fun updateTask(task: Task) {
        Repository.updateTask(task)
        refresh()
    }

    fun deleteTask(task: Task) {
        Repository.deleteTask(task)
        refresh()
    }

    fun tryUploadBackend() {
        LogUtil.i("Uploaded")
        Repository.push()
    }

    fun sync(tasks: List<Task>) {
        Repository.sync(tasks)
        refresh()
    }
}