package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AiMemory
import com.example.data.model.ChatMessage
import com.example.data.model.DailyLesson
import com.example.data.model.LearningPlan
import com.example.data.model.ScheduledTask
import com.example.data.model.UserProfile

@Database(
    entities = [
        LearningPlan::class,
        ScheduledTask::class,
        UserProfile::class,
        ChatMessage::class,
        DailyLesson::class,
        AiMemory::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun planDao(): PlanDao
    abstract fun taskDao(): TaskDao
    abstract fun profileDao(): ProfileDao
    abstract fun chatDao(): ChatDao
    abstract fun lessonDao(): DailyLessonDao
    abstract fun aiMemoryDao(): AiMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daypilot_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
