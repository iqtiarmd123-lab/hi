package com.example.engine

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.data.model.VirtualMachine
import com.example.data.model.VmStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VmTelemetry(
    val cpuUsagePercent: Int = 18,
    val ramUsedMb: Int = 0,
    val diskIoReadKb: Long = 0,
    val diskIoWriteKb: Long = 0,
    val uptimeSeconds: Long = 0,
    val fps: Int = 60,
    val isAudioMuted: Boolean = false,
    val mouseMode: MouseInputMode = MouseInputMode.TRACKPAD
)

enum class MouseInputMode {
    TRACKPAD,
    DIRECT_TOUCH
}

class VirtualMachineEngine(
    private val context: Context,
    val vm: VirtualMachine
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var telemetryJob: Job? = null

    private val _status = MutableStateFlow(VmStatus.STOPPED)
    val status: StateFlow<VmStatus> = _status.asStateFlow()

    private val _telemetry = MutableStateFlow(VmTelemetry(ramUsedMb = (vm.ramMb * 0.35).toInt()))
    val telemetry: StateFlow<VmTelemetry> = _telemetry.asStateFlow()

    private var webView: WebView? = null
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Exception) {
            toneGen = null
        }
    }

    fun attachWebView(view: WebView) {
        this.webView = view
        try {
            // Software rendering layer prevents MESA DRM rendernode search failure on virtualized/emulated environments
            view.setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
        } catch (e: Exception) {
            // Fallback
        }
        view.setBackgroundColor(android.graphics.Color.BLACK)
        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = false
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
        }
    }

    fun start() {
        _status.value = VmStatus.BOOTING
        startTelemetryLoop()

        val html = LocalEmulatorAssets.getHtmlPage(
            vmName = vm.name,
            osPreset = vm.osPreset.displayName,
            cpuCores = vm.cpuCores,
            ramMb = vm.ramMb,
            diskMb = vm.diskSizeMb,
            isoName = vm.isoName,
            bootDevice = vm.bootDevice.label,
            vgaResolution = vm.vgaResolution,
            networkEnabled = vm.networkEnabled,
            audioEnabled = vm.audioEnabled && !_telemetry.value.isAudioMuted
        )

        webView?.loadDataWithBaseURL("https://mobilevirtualos.local/", html, "text/html", "UTF-8", null)

        scope.launch {
            delay(1500)
            if (_status.value == VmStatus.BOOTING) {
                _status.value = VmStatus.RUNNING
            }
        }
    }

    fun pause() {
        if (_status.value == VmStatus.RUNNING) {
            _status.value = VmStatus.PAUSED
            webView?.evaluateJavascript("window.AndroidBridge && window.AndroidBridge.pauseVm();", null)
        }
    }

    fun resume() {
        if (_status.value == VmStatus.PAUSED) {
            _status.value = VmStatus.RUNNING
            webView?.evaluateJavascript("window.AndroidBridge && window.AndroidBridge.resumeVm();", null)
        }
    }

    fun restart() {
        _status.value = VmStatus.BOOTING
        webView?.evaluateJavascript("window.AndroidBridge && window.AndroidBridge.restartVm();", null)
        scope.launch {
            delay(1200)
            _status.value = VmStatus.RUNNING
        }
    }

    fun shutdown() {
        _status.value = VmStatus.STOPPED
        webView?.evaluateJavascript("window.AndroidBridge && window.AndroidBridge.shutdownVm();", null)
        telemetryJob?.cancel()
    }

    fun forceStop() {
        _status.value = VmStatus.STOPPED
        telemetryJob?.cancel()
        webView?.loadUrl("about:blank")
    }

    fun sendKey(key: String) {
        val safeKey = key.replace("\\", "\\\\").replace("'", "\\'")
        webView?.evaluateJavascript("window.AndroidBridge && window.AndroidBridge.sendKey('$safeKey');", null)
    }

    fun sendSpecialKey(keyName: String) {
        val safeKey = keyName.replace("\\", "\\\\").replace("'", "\\'")
        webView?.evaluateJavascript("window.AndroidBridge && window.AndroidBridge.sendSpecialKey('$safeKey');", null)
    }

    fun sendMouseDelta(deltaX: Float, deltaY: Float) {
        val currX = _telemetry.value.diskIoReadKb // reuse or calculate relative
        // Evaluate mouse movement
        val script = """
            (function() {
                if (window.AndroidBridge) {
                    window.AndroidBridge.setMousePosition(
                        Math.max(0, Math.min(1, ($deltaX / 400.0) + 0.5)),
                        Math.max(0, Math.min(1, ($deltaY / 300.0) + 0.5))
                    );
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script, null)
    }

    fun sendMousePosition(xPercent: Float, yPercent: Float) {
        val script = "window.AndroidBridge && window.AndroidBridge.setMousePosition($xPercent, $yPercent);"
        webView?.evaluateJavascript(script, null)
    }

    fun sendMouseClick(button: String = "left") {
        playBeepTone()
        val script = "window.AndroidBridge && window.AndroidBridge.mouseClick('$button');"
        webView?.evaluateJavascript(script, null)
    }

    fun toggleAudio() {
        val newMuted = !_telemetry.value.isAudioMuted
        _telemetry.value = _telemetry.value.copy(isAudioMuted = newMuted)
    }

    fun setMouseMode(mode: MouseInputMode) {
        _telemetry.value = _telemetry.value.copy(mouseMode = mode)
    }

    fun captureSnapshotState(callback: (String) -> Unit) {
        webView?.evaluateJavascript("window.AndroidBridge ? window.AndroidBridge.captureState() : '{}';") { result ->
            val unquoted = if (result != null && result.startsWith("\"") && result.endsWith("\"")) {
                result.substring(1, result.length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
            } else {
                result ?: "{}"
            }
            callback(unquoted)
        }
    }

    fun restoreSnapshotState(stateJson: String) {
        val escaped = stateJson.replace("\\", "\\\\").replace("'", "\\'")
        webView?.evaluateJavascript("window.AndroidBridge && window.AndroidBridge.restoreState('$escaped');", null)
        _status.value = VmStatus.RUNNING
    }

    private fun playBeepTone() {
        if (!_telemetry.value.isAudioMuted && vm.audioEnabled) {
            try {
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
            } catch (e: Exception) {}
        }
    }

    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            var uptime = 0L
            var readKb = 120L
            var writeKb = 40L
            while (isActive) {
                delay(1000)
                if (_status.value == VmStatus.RUNNING) {
                    uptime++
                    val cpuFluctuation = (15..45).random()
                    val ramBase = (vm.ramMb * 0.4).toInt()
                    val ramFluctuation = (-5..15).random()
                    readKb += (0..80).random()
                    writeKb += (0..20).random()

                    _telemetry.value = _telemetry.value.copy(
                        cpuUsagePercent = cpuFluctuation,
                        ramUsedMb = (ramBase + ramFluctuation).coerceIn(16, vm.ramMb),
                        diskIoReadKb = readKb,
                        diskIoWriteKb = writeKb,
                        uptimeSeconds = uptime,
                        fps = (58..60).random()
                    )
                }
            }
        }
    }

    fun cleanup() {
        telemetryJob?.cancel()
        toneGen?.release()
        toneGen = null
        webView = null
    }
}
