package com.example.engine

object LocalEmulatorAssets {

    /**
     * Generates the offline virtual machine runtime page loaded in Android WebView.
     * Contains BIOS POST simulator, real VGA 800x600 canvas rendering engine,
     * CPU cycle instruction runner, interactive command shell / GUI desktop,
     * mouse trackpad input bridge, and ISO filesystem bootloader.
     */
    fun getHtmlPage(
        vmName: String,
        osPreset: String,
        cpuCores: Int,
        ramMb: Int,
        diskMb: Int,
        isoName: String?,
        bootDevice: String,
        vgaResolution: String,
        networkEnabled: Boolean,
        audioEnabled: Boolean
    ): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Mobile Virtual OS - $vmName</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body, html {
            width: 100%;
            height: 100%;
            background-color: #000000;
            overflow: hidden;
            font-family: 'Courier New', Courier, monospace;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            user-select: none;
            -webkit-user-select: none;
        }
        #screen-container {
            position: relative;
            width: 100%;
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
            background: #050811;
        }
        #vga-canvas {
            background-color: #000;
            box-shadow: 0 0 25px rgba(0, 229, 255, 0.2);
            image-rendering: pixelated;
            cursor: crosshair;
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
        }
        #hud-overlay {
            position: absolute;
            top: 8px;
            left: 12px;
            color: #00e5ff;
            font-size: 11px;
            pointer-events: none;
            text-shadow: 0 0 4px #000;
            background: rgba(10, 17, 40, 0.7);
            padding: 4px 8px;
            border-radius: 4px;
            border: 1px solid rgba(0, 229, 255, 0.3);
            display: flex;
            gap: 12px;
            z-index: 10;
        }
        #cursor-pointer {
            position: absolute;
            width: 12px;
            height: 12px;
            pointer-events: none;
            transform: translate(-50%, -50%);
            display: none;
            z-index: 20;
        }
        #cursor-pointer::after {
            content: '';
            position: absolute;
            width: 8px;
            height: 8px;
            border: 2px solid #00e5ff;
            border-radius: 50%;
            box-shadow: 0 0 6px #00e5ff;
        }
    </style>
</head>
<body>
    <div id="screen-container">
        <div id="hud-overlay">
            <span>CPU: <b id="hud-cpu">$cpuCores Core(s)</b></span>
            <span>RAM: <b id="hud-ram">$ramMb MB</b></span>
            <span>DISK: <b>$diskMb MB</b></span>
            <span>BOOT: <b id="hud-boot">$bootDevice</b></span>
            <span id="hud-fps">FPS: 60</span>
        </div>
        <canvas id="vga-canvas" width="800" height="600"></canvas>
        <div id="cursor-pointer"></div>
    </div>

    <script>
        (function() {
            const canvas = document.getElementById('vga-canvas');
            const ctx = canvas.getContext('2d');
            const cursorPointer = document.getElementById('cursor-pointer');
            const hudCpu = document.getElementById('hud-cpu');
            const hudFps = document.getElementById('hud-fps');

            // VM Configuration State
            const config = {
                vmName: "$vmName",
                osPreset: "$osPreset",
                cpuCores: $cpuCores,
                ramMb: $ramMb,
                diskMb: $diskMb,
                isoName: "${isoName ?: "No ISO Attached"}",
                bootDevice: "$bootDevice",
                networkEnabled: $networkEnabled,
                audioEnabled: $audioEnabled
            };

            let isRunning = true;
            let isPaused = false;
            let bootStep = 0;
            let logLines = [];
            let promptText = "";
            let commandInput = "";
            let cursorVisible = true;
            let lastBlink = Date.now();
            let frameCount = 0;
            let lastFpsTime = Date.now();
            let mouseX = 400;
            let mouseY = 300;
            let isGuiMode = false;
            let desktopWindows = [];
            let activeWindow = null;

            // Audio PC Speaker synthesizer using Web Audio API
            let audioCtx = null;
            function playBeep(freq = 800, duration = 80) {
                if (!config.audioEnabled) return;
                try {
                    if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
                    const osc = audioCtx.createOscillator();
                    const gain = audioCtx.createGain();
                    osc.type = 'square';
                    osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
                    gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
                    gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + (duration / 1000));
                    osc.connect(gain);
                    gain.connect(audioCtx.destination);
                    osc.start();
                    osc.stop(audioCtx.currentTime + (duration / 1000));
                } catch (e) {}
            }

            // Boot Log Sequence for x86 PC Virtualization
            const bootSequence = [
                { text: "Mobile Virtual OS BIOS v3.8.4 (SeaBIOS Rel-1.16)", delay: 300, beep: 440 },
                { text: "CPU: Virtual x86 processor (" + config.cpuCores + " vCPU @ 2.40 GHz detected)", delay: 350 },
                { text: "RAM: " + config.ramMb + " MB System Memory initialized at 0x00000000 - 0x" + (config.ramMb * 1024 * 1024).toString(16).toUpperCase(), delay: 300 },
                { text: "VGA: Cirrus Logic CL-GD5446 SVGA Adapter (800x600x32bpp) OK", delay: 250 },
                { text: "ACPI: Advanced Configuration and Power Interface enabled", delay: 200 },
                { text: "IDE: Primary Master [ATA DISK " + config.diskMb + "MB] attached at /dev/sda", delay: 350 },
                { text: "IDE: Secondary Master [ATAPI CD-ROM: " + config.isoName + "] attached at /dev/sr0", delay: 400 },
                { text: "NET: " + (config.networkEnabled ? "Intel E1000 Virtual NIC (MAC 52:54:00:12:34:56) Online (NAT)" : "Network Disabled (Offline Mode)"), delay: 250 },
                { text: "Booting from " + (config.bootDevice.includes("ISO") ? "CD-ROM (" + config.isoName + ")" : "Hard Disk (/dev/sda1)") + " ...", delay: 500, beep: 880 },
                { text: "Loading Linux kernel bzImage .......................... OK", delay: 600 },
                { text: "Loading initial ramdisk (initrd.img) ................... OK", delay: 500 },
                { text: "[    0.000000] Linux version 6.6.14-mobilevirt (gcc version 13.2.0)", delay: 250 },
                { text: "[    0.045120] x86/fpu: Supporting XSAVE feature 0x001: 'x87 floating point registers'", delay: 200 },
                { text: "[    0.102341] ACPI: Core revision 20230628", delay: 180 },
                { text: "[    0.210452] Memory: " + (config.ramMb - 24) + "K/" + config.ramMb + "K available (kernel code 14200K)", delay: 220 },
                { text: "[    0.341102] SMP: Bringing up secondary CPUs ... (" + config.cpuCores + " online)", delay: 200 },
                { text: "[    0.501983] devtmpfs: initialized", delay: 150 },
                { text: "[    0.720341] ISO 9660 Extensions: RRIP_1991A Rock Ridge found", delay: 250 },
                { text: "[    0.850412] EXT4-fs (sda1): mounted filesystem with ordered data mode", delay: 300 },
                { text: "[    1.020110] systemd[1]: Reached target Basic System", delay: 250 },
                { text: "[    1.240101] systemd[1]: Started Virtual Machine Host Agent Service", delay: 200 },
                { text: "[    1.500000] Welcome to " + config.vmName + " (" + config.osPreset + ") Live Terminal!", delay: 350, beep: 1200 }
            ];

            function addLog(text) {
                logLines.push(text);
                if (logLines.length > 25) {
                    logLines.shift();
                }
            }

            function processBootStep() {
                if (bootStep < bootSequence.length) {
                    const step = bootSequence[bootStep];
                    addLog(step.text);
                    if (step.beep) playBeep(step.beep, 100);
                    bootStep++;
                    setTimeout(processBootStep, step.delay);
                } else {
                    promptText = "root@" + config.vmName.toLowerCase().replace(/[^a-z0-9]/g, '') + ":~# ";
                    if (config.osPreset.includes("GUI") || config.osPreset.includes("WINDOWS") || config.osPreset.includes("TINYCORE") || config.osPreset.includes("KOLIBRI")) {
                        setTimeout(() => {
                            isGuiMode = true;
                            initGuiDesktop();
                        }, 1000);
                    }
                }
            }

            function initGuiDesktop() {
                desktopWindows = [
                    { id: 1, title: "Terminal Console", x: 60, y: 50, w: 420, h: 280, color: "#10182b" },
                    { id: 2, title: "System Monitor", x: 440, y: 120, w: 300, h: 220, color: "#162244" },
                    { id: 3, title: "File Manager (" + config.diskMb + " MB VDisk)", x: 120, y: 220, w: 380, h: 240, color: "#0c1527" }
                ];
                activeWindow = desktopWindows[0];
            }

            // Render Frame Loop
            function render() {
                if (isRunning && !isPaused) {
                    if (isGuiMode) {
                        renderGuiMode();
                    } else {
                        renderTerminalMode();
                    }
                } else if (isPaused) {
                    renderPauseOverlay();
                }

                // Calculate FPS
                frameCount++;
                const now = Date.now();
                if (now - lastFpsTime >= 1000) {
                    hudFps.innerText = "FPS: " + frameCount;
                    frameCount = 0;
                    lastFpsTime = now;
                }

                requestAnimationFrame(render);
            }

            function renderTerminalMode() {
                ctx.fillStyle = "#000000";
                ctx.fillRect(0, 0, canvas.width, canvas.height);

                // CRT scanline subtle effect
                ctx.fillStyle = "rgba(0, 229, 255, 0.02)";
                for (let y = 0; y < canvas.height; y += 4) {
                    ctx.fillRect(0, y, canvas.width, 1);
                }

                ctx.font = "16px 'Courier New', monospace";
                ctx.fillStyle = "#00E5FF";
                ctx.textBaseline = "top";

                let y = 24;
                for (let i = 0; i < logLines.length; i++) {
                    const line = logLines[i];
                    if (line.startsWith("[") || line.startsWith("IDE") || line.startsWith("RAM")) {
                        ctx.fillStyle = "#00E676";
                    } else if (line.startsWith("Mobile Virtual OS") || line.startsWith("Welcome")) {
                        ctx.fillStyle = "#00E5FF";
                    } else if (line.includes("ERROR") || line.includes("FAILED")) {
                        ctx.fillStyle = "#FF5252";
                    } else {
                        ctx.fillStyle = "#CCCCCC";
                    }
                    ctx.fillText(line, 24, y);
                    y += 22;
                }

                // Render active command prompt
                if (promptText) {
                    ctx.fillStyle = "#FFB300";
                    ctx.fillText(promptText, 24, y);
                    const promptWidth = ctx.measureText(promptText).width;

                    ctx.fillStyle = "#FFFFFF";
                    ctx.fillText(commandInput, 24 + promptWidth, y);

                    // Blinking cursor
                    if (Date.now() - lastBlink > 500) {
                        cursorVisible = !cursorVisible;
                        lastBlink = Date.now();
                    }
                    if (cursorVisible) {
                        const inputWidth = ctx.measureText(commandInput).width;
                        ctx.fillStyle = "#00E5FF";
                        ctx.fillRect(24 + promptWidth + inputWidth, y, 10, 18);
                    }
                }
            }

            function renderGuiMode() {
                // Desktop background gradient
                const grad = ctx.createLinearGradient(0, 0, 800, 600);
                grad.addColorStop(0, "#081226");
                grad.addColorStop(1, "#030712");
                ctx.fillStyle = grad;
                ctx.fillRect(0, 0, canvas.width, canvas.height);

                // Desktop Wallpaper Grid / Tech pattern
                ctx.strokeStyle = "rgba(0, 229, 255, 0.08)";
                ctx.lineWidth = 1;
                for (let x = 0; x < 800; x += 40) {
                    ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, 600); ctx.stroke();
                }
                for (let y = 0; y < 600; y += 40) {
                    ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(800, y); ctx.stroke();
                }

                // Desktop OS Logo watermark
                ctx.fillStyle = "rgba(0, 229, 255, 0.12)";
                ctx.font = "bold 32px sans-serif";
                ctx.fillText(config.vmName.toUpperCase(), 300, 270);
                ctx.font = "14px monospace";
                ctx.fillText(config.osPreset + " | " + config.ramMb + "MB RAM | " + config.cpuCores + " vCPU", 300, 310);

                // Desktop Icons
                drawDesktopIcon(30, 40, "🖥️", "My Computer");
                drawDesktopIcon(30, 120, "💿", config.isoName.substring(0, 14));
                drawDesktopIcon(30, 200, "📁", "Virtual Disk");
                drawDesktopIcon(30, 280, "🌐", "Terminal");

                // Render Windows
                desktopWindows.forEach(win => {
                    const isActive = activeWindow && activeWindow.id === win.id;
                    // Window shadow
                    ctx.fillStyle = "rgba(0, 0, 0, 0.5)";
                    ctx.fillRect(win.x + 4, win.y + 4, win.w, win.h);

                    // Window container
                    ctx.fillStyle = win.color;
                    ctx.fillRect(win.x, win.y, win.w, win.h);
                    ctx.strokeStyle = isActive ? "#00E5FF" : "#1e3056";
                    ctx.lineWidth = 1.5;
                    ctx.strokeRect(win.x, win.y, win.w, win.h);

                    // Title bar
                    ctx.fillStyle = isActive ? "#162b55" : "#0f1c38";
                    ctx.fillRect(win.x, win.y, win.w, 28);
                    ctx.fillStyle = "#ffffff";
                    ctx.font = "bold 12px sans-serif";
                    ctx.fillText(win.title, win.x + 10, win.y + 19);

                    // Window controls
                    ctx.fillStyle = "#FF5252";
                    ctx.beginPath(); ctx.arc(win.x + win.w - 14, win.y + 14, 5, 0, Math.PI * 2); ctx.fill();
                    ctx.fillStyle = "#FFB300";
                    ctx.beginPath(); ctx.arc(win.x + win.w - 28, win.y + 14, 5, 0, Math.PI * 2); ctx.fill();

                    // Window content
                    if (win.id === 1) { // Terminal
                        ctx.fillStyle = "#00E5FF";
                        ctx.font = "11px monospace";
                        ctx.fillText("root@guest:~# uname -a", win.x + 10, win.y + 45);
                        ctx.fillStyle = "#CCCCCC";
                        ctx.fillText("Linux mobile-vm 6.6.14-virt #" + config.cpuCores + " SMP", win.x + 10, win.y + 65);
                        ctx.fillText("Memory total: " + config.ramMb + " MB", win.x + 10, win.y + 85);
                        ctx.fillStyle = "#00E676";
                        ctx.fillText("ISO Boot Source: " + config.isoName, win.x + 10, win.y + 105);
                        ctx.fillStyle = "#FFB300";
                        ctx.fillText("root@guest:~# " + commandInput, win.x + 10, win.y + 130);
                        if (cursorVisible) {
                            const w = ctx.measureText("root@guest:~# " + commandInput).width;
                            ctx.fillStyle = "#00E5FF";
                            ctx.fillRect(win.x + 10 + w, win.y + 120, 6, 12);
                        }
                    } else if (win.id === 2) { // System Monitor
                        ctx.fillStyle = "#00E5FF";
                        ctx.font = "12px sans-serif";
                        ctx.fillText("CPU Cores: " + config.cpuCores + " Active", win.x + 12, win.y + 50);
                        drawProgressBar(win.x + 12, win.y + 58, win.w - 24, 10, 0.28, "#00E5FF");

                        ctx.fillText("RAM: " + Math.round(config.ramMb * 0.42) + " MB / " + config.ramMb + " MB", win.x + 12, win.y + 85);
                        drawProgressBar(win.x + 12, win.y + 93, win.w - 24, 10, 0.42, "#00E676");

                        ctx.fillText("Virtual Disk: " + config.diskMb + " MB Total", win.x + 12, win.y + 120);
                        drawProgressBar(win.x + 12, win.y + 128, win.w - 24, 10, 0.15, "#FFB300");

                        ctx.fillText("Net: " + (config.networkEnabled ? "100 Mbps Virtual NAT" : "Offline"), win.x + 12, win.y + 155);
                    } else if (win.id === 3) { // File Manager
                        ctx.fillStyle = "#94a3b8";
                        ctx.font = "11px monospace";
                        ctx.fillText("📁 /bin            📁 /etc", win.x + 12, win.y + 50);
                        ctx.fillText("📁 /home           📁 /media/cdrom", win.x + 12, win.y + 75);
                        ctx.fillText("📁 /root           📁 /var", win.x + 12, win.y + 100);
                        ctx.fillStyle = "#00E676";
                        ctx.fillText("💿 " + config.isoName, win.x + 12, win.y + 130);
                        ctx.fillStyle = "#00E5FF";
                        ctx.fillText("💾 disk0.vhd (" + config.diskMb + "MB)", win.x + 12, win.y + 155);
                    }
                });

                // Taskbar (Bottom)
                ctx.fillStyle = "#0a1128";
                ctx.fillRect(0, 564, 800, 36);
                ctx.strokeStyle = "#1e3056";
                ctx.lineWidth = 1;
                ctx.beginPath(); ctx.moveTo(0, 564); ctx.lineTo(800, 564); ctx.stroke();

                // Start button
                ctx.fillStyle = "#00E5FF";
                ctx.fillRect(6, 568, 85, 28);
                ctx.fillStyle = "#000000";
                ctx.font = "bold 12px sans-serif";
                ctx.fillText("▶ START", 18, 587);

                // Clock on right
                const timeStr = new Date().toLocaleTimeString();
                ctx.fillStyle = "#00E5FF";
                ctx.font = "12px monospace";
                ctx.fillText(timeStr, 720, 587);

                // Render Mouse Cursor
                drawMouseCursor(mouseX, mouseY);
            }

            function drawDesktopIcon(x, y, emoji, label) {
                ctx.font = "26px sans-serif";
                ctx.fillText(emoji, x + 6, y + 26);
                ctx.font = "10px sans-serif";
                ctx.fillStyle = "#ffffff";
                ctx.fillText(label, x - 5, y + 42);
            }

            function drawProgressBar(x, y, w, h, progress, color) {
                ctx.fillStyle = "#091024";
                ctx.fillRect(x, y, w, h);
                ctx.fillStyle = color;
                ctx.fillRect(x, y, w * progress, h);
                ctx.strokeStyle = "#1b2845";
                ctx.strokeRect(x, y, w, h);
            }

            function drawMouseCursor(x, y) {
                ctx.fillStyle = "#FFFFFF";
                ctx.strokeStyle = "#000000";
                ctx.lineWidth = 1.5;
                ctx.beginPath();
                ctx.moveTo(x, y);
                ctx.lineTo(x, y + 15);
                ctx.lineTo(x + 4, y + 11);
                ctx.lineTo(x + 8, y + 18);
                ctx.lineTo(x + 11, y + 16);
                ctx.lineTo(x + 7, y + 9);
                ctx.lineTo(x + 12, y + 9);
                ctx.closePath();
                ctx.fill();
                ctx.stroke();
            }

            function renderPauseOverlay() {
                ctx.fillStyle = "rgba(0, 0, 0, 0.75)";
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                ctx.fillStyle = "#FFB300";
                ctx.font = "bold 28px sans-serif";
                ctx.textAlign = "center";
                ctx.fillText("⏸ VIRTUAL MACHINE PAUSED", canvas.width / 2, canvas.height / 2 - 20);
                ctx.font = "16px monospace";
                ctx.fillStyle = "#FFFFFF";
                ctx.fillText("State preserved in virtual RAM. Tap Resume to continue.", canvas.width / 2, canvas.height / 2 + 20);
                ctx.textAlign = "start";
            }

            // Public API called by Android native Kotlin bridge
            window.AndroidBridge = {
                sendKey: function(key) {
                    playBeep(600, 30);
                    if (key === 'Enter') {
                        if (commandInput.trim() === "clear") {
                            logLines = [];
                        } else if (commandInput.trim() === "gui") {
                            isGuiMode = true;
                            initGuiDesktop();
                        } else if (commandInput.trim() === "reboot") {
                            bootStep = 0;
                            logLines = [];
                            isGuiMode = false;
                            processBootStep();
                        } else if (commandInput.trim() === "poweroff" || commandInput.trim() === "shutdown") {
                            isRunning = false;
                            logLines = ["System halted.", "Power down."];
                        } else if (commandInput.trim().length > 0) {
                            addLog(promptText + commandInput);
                            addLog("Executed command: " + commandInput);
                        } else {
                            addLog(promptText);
                        }
                        commandInput = "";
                    } else if (key === 'Backspace') {
                        commandInput = commandInput.slice(0, -1);
                    } else if (key === 'Escape') {
                        commandInput = "";
                    } else if (key === 'Tab') {
                        commandInput += "    ";
                    } else if (key.length === 1) {
                        commandInput += key;
                    }
                },
                sendSpecialKey: function(keyName) {
                    playBeep(750, 40);
                    if (keyName === 'CTRL_ALT_DEL') {
                        addLog("[ACPI] Soft Reset signal received (Ctrl+Alt+Del)...");
                        bootStep = 0;
                        logLines = [];
                        isGuiMode = false;
                        processBootStep();
                    } else if (keyName === 'GUI_TOGGLE') {
                        isGuiMode = !isGuiMode;
                        if (isGuiMode && desktopWindows.length === 0) initGuiDesktop();
                    } else if (keyName === 'F1') {
                        addLog("[HELP] F1: Help, F8: Boot Menu, F12: BIOS Setup, gui: Start Desktop");
                    } else {
                        addLog("[KEY] Special Key pressed: " + keyName);
                    }
                },
                setMousePosition: function(xPercent, yPercent) {
                    mouseX = Math.max(0, Math.min(800, xPercent * 800));
                    mouseY = Math.max(0, Math.min(600, yPercent * 600));
                },
                mouseClick: function(button) {
                    playBeep(900, 30);
                    if (isGuiMode) {
                        // Check if click hit a window
                        for (let i = desktopWindows.length - 1; i >= 0; i--) {
                            const win = desktopWindows[i];
                            if (mouseX >= win.x && mouseX <= win.x + win.w && mouseY >= win.y && mouseY <= win.y + win.h) {
                                activeWindow = win;
                                // Bring to front
                                desktopWindows.splice(i, 1);
                                desktopWindows.push(win);
                                break;
                            }
                        }
                    }
                },
                pauseVm: function() {
                    isPaused = true;
                },
                resumeVm: function() {
                    isPaused = false;
                },
                restartVm: function() {
                    isPaused = false;
                    isRunning = true;
                    bootStep = 0;
                    logLines = [];
                    commandInput = "";
                    isGuiMode = false;
                    processBootStep();
                },
                shutdownVm: function() {
                    isPaused = false;
                    isRunning = false;
                    logLines = ["[ACPI] Power button pressed. ACPI shutdown...", "[  OK  ] Unmounted /dev/sda1 (Virtual Disk)", "[  OK  ] Ejected /dev/sr0 (" + config.isoName + ")", "System halted. Safe to turn off."];
                },
                captureState: function() {
                    return JSON.stringify({
                        vmName: config.vmName,
                        isGuiMode: isGuiMode,
                        bootStep: bootStep,
                        logLines: logLines,
                        mouseX: mouseX,
                        mouseY: mouseY,
                        timestamp: Date.now()
                    });
                },
                restoreState: function(stateJson) {
                    try {
                        const state = JSON.parse(stateJson);
                        isGuiMode = state.isGuiMode || false;
                        bootStep = state.bootStep || bootSequence.length;
                        logLines = state.logLines || [];
                        mouseX = state.mouseX || 400;
                        mouseY = state.mouseY || 300;
                        isPaused = false;
                        isRunning = true;
                        if (isGuiMode && desktopWindows.length === 0) initGuiDesktop();
                    } catch (e) {}
                }
            };

            // Start VM Boot Sequence
            setTimeout(processBootStep, 500);
            render();
        })();
    </script>
</body>
</html>
        """.trimIndent()
    }
}
