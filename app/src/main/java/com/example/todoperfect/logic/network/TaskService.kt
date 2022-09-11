package com.example.todoperfect.logic.network

import com.example.todoperfect.logic.model.*
import retrofit2.Call
import retrofit2.http.*

interface TaskService {

    @POST("get_tasks")
    fun pullAllTasks(@Body taskPullRequest: TaskPullRequest): Call<TaskResponse>

    @POST("add_task")
    fun insertTasks(@Body taskRequest: TaskRequest): Call<TaskNoDataResponse>

    @PATCH("update_task")
    fun updateTasks(@Body taskRequest: TaskRequest): Call<TaskNoDataResponse>

    @POST("delete_task")
    fun deleteTasks(@Body taskRequest: TaskRequest): Call<TaskNoDataResponse>
}