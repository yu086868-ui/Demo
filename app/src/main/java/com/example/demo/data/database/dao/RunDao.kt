package com.example.demo.data.database.dao

import androidx.room.*
import com.example.demo.data.database.entities.DatabaseRun
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {

    @Query("SELECT * FROM runs ORDER BY startTime DESC")
    fun getAllRuns(): Flow<List<DatabaseRun>>

    @Query("SELECT * FROM runs WHERE id = :runId")
    suspend fun getRunById(runId: Long): DatabaseRun?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: DatabaseRun)

    @Delete
    suspend fun deleteRun(run: DatabaseRun)

    @Query("DELETE FROM runs WHERE id = :runId")
    suspend fun deleteRunById(runId: Long)

    @Query("SELECT COUNT(*) FROM runs")
    suspend fun getRunCount(): Int

    @Query("SELECT SUM(distance) FROM runs")
    suspend fun getTotalDistance(): Float?

    @Query("SELECT SUM(duration) FROM runs")
    suspend fun getTotalDuration(): Long?

    @Query("SELECT SUM(calories) FROM runs")
    suspend fun getTotalCalories(): Float?

    @Query("SELECT * FROM runs ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentRuns(limit: Int): List<DatabaseRun>
}