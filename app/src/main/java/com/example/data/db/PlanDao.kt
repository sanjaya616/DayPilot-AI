package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LearningPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM learning_plans ORDER BY id DESC")
    fun getAllPlans(): Flow<List<LearningPlan>>

    @Query("SELECT * FROM learning_plans WHERE id = :id LIMIT 1")
    fun getPlanById(id: Long): Flow<LearningPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: LearningPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<LearningPlan>)

    @Update
    suspend fun updatePlan(plan: LearningPlan)

    @Delete
    suspend fun deletePlan(plan: LearningPlan)

    @Query("DELETE FROM learning_plans WHERE id = :id")
    suspend fun deletePlanById(id: Long)

    @Query("SELECT COUNT(*) FROM learning_plans")
    suspend fun getPlanCount(): Int
}
