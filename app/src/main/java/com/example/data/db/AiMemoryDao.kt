package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AiMemory
import kotlinx.coroutines.flow.Flow

@Dao
interface AiMemoryDao {
    @Query("SELECT * FROM ai_memory WHERE id = 1 LIMIT 1")
    fun getMemory(): Flow<AiMemory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(memory: AiMemory)

    @Update
    suspend fun updateMemory(memory: AiMemory)

    @Query("SELECT COUNT(*) FROM ai_memory")
    suspend fun getCount(): Int

    @Query("DELETE FROM ai_memory")
    suspend fun clearMemory()
}
