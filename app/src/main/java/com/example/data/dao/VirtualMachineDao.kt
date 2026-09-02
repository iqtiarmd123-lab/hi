package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VirtualMachine
import com.example.data.model.VmStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface VirtualMachineDao {
    @Query("SELECT * FROM virtual_machines ORDER BY createdAt DESC")
    fun getAllVms(): Flow<List<VirtualMachine>>

    @Query("SELECT * FROM virtual_machines WHERE id = :id")
    suspend fun getVmById(id: Long): VirtualMachine?

    @Query("SELECT * FROM virtual_machines WHERE id = :id")
    fun getVmByIdFlow(id: Long): Flow<VirtualMachine?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVm(vm: VirtualMachine): Long

    @Update
    suspend fun updateVm(vm: VirtualMachine)

    @Delete
    suspend fun deleteVm(vm: VirtualMachine)

    @Query("UPDATE virtual_machines SET status = :status WHERE id = :id")
    suspend fun updateVmStatus(id: Long, status: VmStatus)

    @Query("UPDATE virtual_machines SET lastBootedAt = :timestamp, status = :status WHERE id = :id")
    suspend fun markVmBooted(id: Long, timestamp: Long, status: VmStatus)

    @Query("UPDATE virtual_machines SET status = 'STOPPED'")
    suspend fun resetAllVmStatuses()
}
