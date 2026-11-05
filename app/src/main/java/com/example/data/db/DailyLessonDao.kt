package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyLesson
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLessonDao {
    @Query("SELECT * FROM daily_lessons WHERE planId = :planId ORDER BY dayNumber ASC")
    fun getLessonsForPlan(planId: Long): Flow<List<DailyLesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: DailyLesson): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<DailyLesson>)

    @Update
    suspend fun updateLesson(lesson: DailyLesson)

    @Delete
    suspend fun deleteLesson(lesson: DailyLesson)

    @Query("DELETE FROM daily_lessons WHERE planId = :planId")
    suspend fun deleteLessonsForPlan(planId: Long)
}
