package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScheduledTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM scheduled_tasks ORDER BY dateEpochDay ASC, startTime ASC")
    fun getAllTasks(): Flow<List<ScheduledTask>>

    @Query("SELECT * FROM scheduled_tasks WHERE dateEpochDay = :dateEpochDay ORDER BY startTime ASC")
    fun getTasksForDate(dateEpochDay: Long): Flow<List<ScheduledTask>>

    @Query("SELECT * FROM scheduled_tasks WHERE planId = :planId ORDER BY dateEpochDay ASC")
    fun getTasksForPlan(planId: Long): Flow<List<ScheduledTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ScheduledTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<ScheduledTask>)

    @Update
    suspend fun updateTask(task: ScheduledTask)

    @Delete
    suspend fun deleteTask(task: ScheduledTask)

    @Query("DELETE FROM scheduled_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("SELECT COUNT(*) FROM scheduled_tasks")
    suspend fun getTaskCount(): Int
}
