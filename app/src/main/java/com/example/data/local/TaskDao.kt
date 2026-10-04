package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

  @Query("SELECT * FROM tasks ORDER BY dateIso ASC, id ASC")
  fun getAllTasks(): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE dateIso = :dateIso ORDER BY isCompleted ASC, id ASC")
  fun getTasksForDate(dateIso: String): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
  suspend fun getTaskById(id: Long): TaskEntity?

  @Query("SELECT * FROM tasks WHERE routineGroup = :group ORDER BY routineDayIndex ASC")
  fun getTasksByRoutineGroup(group: String): Flow<List<TaskEntity>>

  @Query("SELECT DISTINCT dateIso FROM tasks")
  fun getAllTaskDates(): Flow<List<String>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTask(task: TaskEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTasks(tasks: List<TaskEntity>): List<Long>

  @Update
  suspend fun updateTask(task: TaskEntity)

  @Delete
  suspend fun deleteTask(task: TaskEntity)

  @Query("DELETE FROM tasks WHERE id = :id")
  suspend fun deleteTaskById(id: Long)

  @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt, syncedWithSheet = :synced, syncStatusMessage = :syncMessage WHERE id = :id")
  suspend fun setTaskCompletion(
    id: Long,
    isCompleted: Boolean,
    completedAt: Long?,
    synced: Boolean,
    syncMessage: String?
  )

  @Query("UPDATE tasks SET syncedWithSheet = :synced, syncStatusMessage = :syncMessage WHERE id = :id")
  suspend fun updateSyncStatus(id: Long, synced: Boolean, syncMessage: String?)
}
