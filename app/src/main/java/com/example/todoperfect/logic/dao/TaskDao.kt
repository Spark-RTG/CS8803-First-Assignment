package com.example.todoperfect.logic.dao


import androidx.room.*
import com.example.todoperfect.TodoPerfectApplication
import com.example.todoperfect.logic.model.Task
import java.sql.Timestamp
import java.util.*

@Dao
interface TaskDao {

    @Insert
    fun insertTask(task: Task): Long

    @Update
    fun updateTask(newTask: Task)

    @Query("select * from Task where user = :user order by due")
    fun loadAllTasks(user: String): List<Task>

    @Query("select * from Task where (user = :user and due < :due and stared = 0) order by due")
    fun loadTasksEarlierThan(user: String, due: Timestamp): List<Task>

    @Query("select * from Task where (user = :user and due < :end and due >= :start and stared == 0) order by due")
    fun loadTasksBetween(user: String, start: Timestamp, end: Timestamp): List<Task>

    @Query("select * from Task where (user = :user and due >= :start and stared == 0) order by due")
    fun loadTasksAfter(user: String, start: Timestamp): List<Task>

    @Query("select * from Task where (user = :user and importance > :importance and stared == 0) order by due")
    fun loadTaskHigherImportance(user: String, importance: Int): List<Task>

    @Query("select * from Task where (user = :user and stared == 1) order by due")
    fun loadStaredTasks(user: String): List<Task>

    @Delete
    fun deleteTask(task: Task)

    @Query("delete from Task where user = :user")
    fun deleteAllTasks(user: String)
}