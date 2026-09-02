package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VmSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface SnapshotDao {
    @Query("SELECT * FROM vm_snapshots WHERE vmId = :vmId ORDER BY timestamp DESC")
    fun getSnapshotsForVm(vmId: Long): Flow<List<VmSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: VmSnapshot): Long

    @Delete
    suspend fun deleteSnapshot(snapshot: VmSnapshot)

    @Query("DELETE FROM vm_snapshots WHERE vmId = :vmId")
    suspend fun deleteAllSnapshotsForVm(vmId: Long)
}
