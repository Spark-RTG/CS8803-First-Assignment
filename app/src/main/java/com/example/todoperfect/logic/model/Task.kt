package com.example.todoperfect.logic.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.todoperfect.R
import com.example.todoperfect.TodoPerfectApplication
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.sql.Timestamp

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Timestamp?): Long? {
        return date?.time
    }
}

val monthMap = mapOf(
    0 to "JAN",
    1 to "FEB",
    2 to "MAR",
    3 to "APR",
    4 to "MAY",
    5 to "JUN",
    6 to "JUL",
    7 to "AUG",
    8 to "SEP",
    9 to "OCT",
    10 to "NOV",
    11 to "DEC"
)

val TaskImportanceColor = mapOf(
    1 to TodoPerfectApplication.context.resources.getColor(R.color.grayBlue),
    2 to TodoPerfectApplication.context.resources.getColor(R.color.mediumBlue),
    3 to TodoPerfectApplication.context.resources.getColor(R.color.yellow),
    4 to TodoPerfectApplication.context.resources.getColor(R.color.red),
    5 to TodoPerfectApplication.context.resources.getColor(R.color.magenta)
)

data class TaskRequest(@SerializedName("body-json") val bodyJSON: TaskRequestBodyJSON)
data class TaskRequestBodyJSON(val tasks: List<Task>)
data class TaskPullRequest(@SerializedName("body-json") val bodyJSON: TaskPullRequestBodyJSON)
data class TaskPullRequestBodyJSON(val email: String)

data class TaskNoDataResponse(@SerializedName("body-json") val body: TaskNoDataResponseBody)
data class TaskNoDataResponseBody(val statusCode: Int, val body: String)

data class TaskResponse(@SerializedName("body-json") val body: TaskResponseBody)
data class TaskResponseBody(val statusCode: Int, @SerializedName("body") val tasks: List<Task>)

@Entity
data class Task (
    var subject: String, var description: String,
    @SerializedName("hours") var hour: Int,
    @SerializedName("minutes")var min: Int,
    @SerializedName("due_date")var due: Timestamp,
    var importance: Int, var stared: Boolean,
    @SerializedName("email") var user: String? = TodoPerfectApplication.user?.email) : Serializable {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("task_id")
    var id: Long = 0

    companion object {
        const val TRIVIAL = 1
        const val NORMAL = 2
        const val IMPORTANT = 3
        const val MILESTONE = 4
        const val MEMORIAL = 5
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (subject != other.subject) return false
        if (description != other.description) return false
        if (hour != other.hour) return false
        if (min != other.min) return false
        if (due != other.due) return false
        if (importance != other.importance) return false
        if (stared != other.stared) return false
        if (user != other.user) return false
        if (id != other.id) return false

        return true
    }
}
