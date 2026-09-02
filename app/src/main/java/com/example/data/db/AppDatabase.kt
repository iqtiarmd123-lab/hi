package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.dao.SnapshotDao
import com.example.data.dao.VirtualMachineDao
import com.example.data.model.BootDevice
import com.example.data.model.OsPreset
import com.example.data.model.VirtualMachine
import com.example.data.model.VmSnapshot
import com.example.data.model.VmStatus

class Converters {
    @TypeConverter
    fun fromOsPreset(preset: OsPreset): String = preset.name

    @TypeConverter
    fun toOsPreset(value: String): OsPreset = runCatching { OsPreset.valueOf(value) }.getOrDefault(OsPreset.LINUX_GENERIC)

    @TypeConverter
    fun fromBootDevice(device: BootDevice): String = device.name

    @TypeConverter
    fun toBootDevice(value: String): BootDevice = runCatching { BootDevice.valueOf(value) }.getOrDefault(BootDevice.CD_ROM_ISO)

    @TypeConverter
    fun fromVmStatus(status: VmStatus): String = status.name

    @TypeConverter
    fun toVmStatus(value: String): VmStatus = runCatching { VmStatus.valueOf(value) }.getOrDefault(VmStatus.STOPPED)
}

@Database(
    entities = [VirtualMachine::class, VmSnapshot::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun virtualMachineDao(): VirtualMachineDao
    abstract fun snapshotDao(): SnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mobile_virtual_os.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
