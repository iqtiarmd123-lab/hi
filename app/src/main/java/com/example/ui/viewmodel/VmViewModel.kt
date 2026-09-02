package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BootDevice
import com.example.data.model.IsoMetadata
import com.example.data.model.OsArchitecture
import com.example.data.model.OsPreset
import com.example.data.model.VirtualMachine
import com.example.data.model.VmSnapshot
import com.example.data.model.VmStatus
import com.example.engine.VirtualMachineEngine
import com.example.storage.IsoManager
import com.example.storage.VirtualDiskManager
import com.example.system.DeviceHardwareInfo
import com.example.system.HardwareDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VmViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val vmDao = db.virtualMachineDao()
    private val snapshotDao = db.snapshotDao()

    val vms: StateFlow<List<VirtualMachine>> = vmDao.getAllVms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hardwareInfo = MutableStateFlow(HardwareDetector.detectHardware(application))
    val hardwareInfo: StateFlow<DeviceHardwareInfo> = _hardwareInfo.asStateFlow()

    private val _selectedIso = MutableStateFlow<IsoMetadata?>(null)
    val selectedIso: StateFlow<IsoMetadata?> = _selectedIso.asStateFlow()

    private val _activeEngine = MutableStateFlow<VirtualMachineEngine?>(null)
    val activeEngine: StateFlow<VirtualMachineEngine?> = _activeEngine.asStateFlow()

    private val _currentSnapshots = MutableStateFlow<List<VmSnapshot>>(emptyList())
    val currentSnapshots: StateFlow<List<VmSnapshot>> = _currentSnapshots.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            vmDao.resetAllVmStatuses()
            createDefaultSampleVmIfNeeded()
        }
    }

    private suspend fun createDefaultSampleVmIfNeeded() {
        val count = vmDao.getVmById(1)
        if (count == null) {
            val sampleIso = IsoManager.BUILT_IN_SAMPLE_ISOS[0]
            val disk = VirtualDiskManager.createOrEnsureDisk(getApplication(), "alpine_root.img", 1024)
            val defaultVm = VirtualMachine(
                name = "Alpine Linux Demo (x86)",
                osPreset = OsPreset.ALPINE_LINUX,
                cpuCores = 1,
                ramMb = 256,
                diskSizeMb = 1024,
                diskFileName = disk.fileName,
                isoUri = sampleIso.uri,
                isoName = sampleIso.fileName,
                isoSizeBytes = sampleIso.fileSizeBytes,
                bootDevice = BootDevice.CD_ROM_ISO,
                networkEnabled = true,
                audioEnabled = true,
                description = "Pre-configured ultra-lightweight Linux test environment ready to boot immediately."
            )
            vmDao.insertVm(defaultVm)
        }
    }

    fun refreshHardware() {
        _hardwareInfo.value = HardwareDetector.detectHardware(getApplication())
    }

    fun selectIsoUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val metadata = IsoManager.inspectIsoUri(getApplication(), uri)
                _selectedIso.value = metadata
                _userMessage.value = "Selected ISO: ${metadata.fileName} (${metadata.formattedSize})"
            } catch (e: Exception) {
                _userMessage.value = "Failed to parse ISO: ${e.message}"
            }
        }
    }

    fun selectBuiltInSampleIso(sample: IsoMetadata) {
        _selectedIso.value = sample
        _userMessage.value = "Selected Built-in OS: ${sample.fileName}"
    }

    fun clearSelectedIso() {
        _selectedIso.value = null
    }

    fun createVirtualMachine(
        name: String,
        preset: OsPreset,
        cpuCores: Int,
        ramMb: Int,
        diskMb: Int,
        bootDevice: BootDevice,
        isoMeta: IsoMetadata?,
        networkEnabled: Boolean,
        audioEnabled: Boolean,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val diskName = "vdisk_${System.currentTimeMillis() % 100000}.img"
                VirtualDiskManager.createOrEnsureDisk(getApplication(), diskName, diskMb)

                val newVm = VirtualMachine(
                    name = name.ifBlank { "Virtual Machine ${System.currentTimeMillis() % 1000}" },
                    osPreset = preset,
                    cpuCores = cpuCores,
                    ramMb = ramMb,
                    diskSizeMb = diskMb,
                    diskFileName = diskName,
                    isoUri = isoMeta?.uri,
                    isoName = isoMeta?.fileName,
                    isoSizeBytes = isoMeta?.fileSizeBytes ?: 0L,
                    bootDevice = bootDevice,
                    networkEnabled = networkEnabled,
                    audioEnabled = audioEnabled,
                    description = "${preset.displayName} with ${cpuCores}vCPU, ${ramMb}MB RAM, ${diskMb}MB Disk"
                )

                val id = vmDao.insertVm(newVm)
                _userMessage.value = "Virtual Machine '${newVm.name}' created!"
                onCreated(id)
            } catch (e: Exception) {
                _userMessage.value = "Error creating VM: ${e.message}"
            }
        }
    }

    fun deleteVirtualMachine(vm: VirtualMachine) {
        viewModelScope.launch {
            try {
                if (_activeEngine.value?.vm?.id == vm.id) {
                    stopActiveVm()
                }
                VirtualDiskManager.deleteDisk(getApplication(), vm.diskFileName)
                vmDao.deleteVm(vm)
                _userMessage.value = "Deleted '${vm.name}'"
            } catch (e: Exception) {
                _userMessage.value = "Error deleting VM: ${e.message}"
            }
        }
    }

    fun startVirtualMachine(vm: VirtualMachine) {
        viewModelScope.launch {
            _activeEngine.value?.cleanup()

            vmDao.markVmBooted(vm.id, System.currentTimeMillis(), VmStatus.RUNNING)
            val engine = VirtualMachineEngine(getApplication(), vm)
            _activeEngine.value = engine
            loadSnapshotsForVm(vm.id)
        }
    }

    fun stopActiveVm() {
        viewModelScope.launch {
            val engine = _activeEngine.value
            if (engine != null) {
                vmDao.updateVmStatus(engine.vm.id, VmStatus.STOPPED)
                engine.shutdown()
                engine.cleanup()
                _activeEngine.value = null
            }
        }
    }

    fun forceStopActiveVm() {
        viewModelScope.launch {
            val engine = _activeEngine.value
            if (engine != null) {
                vmDao.updateVmStatus(engine.vm.id, VmStatus.STOPPED)
                engine.forceStop()
                engine.cleanup()
                _activeEngine.value = null
                _userMessage.value = "VM Force Stopped"
            }
        }
    }

    fun loadSnapshotsForVm(vmId: Long) {
        viewModelScope.launch {
            snapshotDao.getSnapshotsForVm(vmId).collect { list ->
                _currentSnapshots.value = list
            }
        }
    }

    fun takeSnapshot(title: String) {
        val engine = _activeEngine.value ?: return
        engine.captureSnapshotState { stateJson ->
            viewModelScope.launch {
                val snapshot = VmSnapshot(
                    vmId = engine.vm.id,
                    title = title.ifBlank { "Snapshot @ ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}" },
                    description = "${engine.vm.osPreset.displayName} state capture",
                    timestamp = System.currentTimeMillis(),
                    ramStateBlob = stateJson,
                    snapshotSizeMb = (engine.vm.ramMb * 0.25)
                )
                snapshotDao.insertSnapshot(snapshot)
                _userMessage.value = "Snapshot '${snapshot.title}' saved!"
            }
        }
    }

    fun restoreSnapshot(snapshot: VmSnapshot) {
        val engine = _activeEngine.value ?: return
        val state = snapshot.ramStateBlob
        if (!state.isNullOrBlank()) {
            engine.restoreSnapshotState(state)
            _userMessage.value = "Restored snapshot '${snapshot.title}'"
        }
    }

    fun deleteSnapshot(snapshot: VmSnapshot) {
        viewModelScope.launch {
            snapshotDao.deleteSnapshot(snapshot)
            _userMessage.value = "Deleted snapshot"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        _activeEngine.value?.cleanup()
    }
}
