package com.example.todoperfect.logic.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todoperfect.logic.model.Converters
import com.example.todoperfect.logic.model.Task

@Database(version = 3, entities = [Task::class])
@TypeConverters(Converters::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("alter table Task add column stared integer not null default 0")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("alter table Task add column user varchar")
            }
        }
        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let{
                return it
            }
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
                "app_database")
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build().apply {
                    instance = this
                }
        }
    }
}