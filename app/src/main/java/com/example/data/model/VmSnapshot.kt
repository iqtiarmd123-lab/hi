package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vm_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = VirtualMachine::class,
            parentColumns = ["id"],
            childColumns = ["vmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vmId"])]
)
data class VmSnapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vmId: Long,
    val title: String,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val ramStateBlob: String? = null,
    val diskStateSnapshotName: String? = null,
    val snapshotSizeMb: Double = 0.0
)
