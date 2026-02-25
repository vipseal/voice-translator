package com.hhaigc.translator

import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import javax.imageio.ImageIO

fun main() = application {
    var isVisible by remember { mutableStateOf(true) }
    var bringToFront by remember { mutableStateOf(false) }
    var trayClickX by remember { mutableStateOf(0) }
    val isMac = System.getProperty("os.name").lowercase().contains("mac")
    // Track when window was hidden by focus loss to prevent show→hide→show race
    var lastHideTime by remember { mutableStateOf(0L) }

    val state = rememberWindowState(
        size = DpSize(420.dp, 700.dp),
        position = WindowPosition(Alignment.Center)
    )

    // System tray
    if (SystemTray.isSupported()) {
        LaunchedEffect(Unit) {
            try {
                val tray = SystemTray.getSystemTray()
                val iconStream = Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png")
                val image = if (iconStream != null) {
                    ImageIO.read(iconStream)
                } else {
                    val img = java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB)
                    val g = img.createGraphics()
                    g.color = Color(124, 77, 255)
                    g.fillRoundRect(0, 0, 16, 16, 4, 4)
                    g.color = Color.WHITE
                    g.font = Font("SansSerif", Font.BOLD, 11)
                    g.drawString("V", 3, 13)
                    g.dispose()
                    img
                }
                val traySize = tray.trayIconSize
                val scaledImage = image.getScaledInstance(traySize.width, traySize.height, Image.SCALE_SMOOTH)
                
                // Never set popup on the TrayIcon directly on Mac
                // We'll show it manually via a workaround
                val trayIcon = TrayIcon(scaledImage, "VoiceTranslator")
                trayIcon.isImageAutoSize = false
                
                trayIcon.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (e.button == MouseEvent.BUTTON1) {
                            trayClickX = e.xOnScreen
                            val now = System.currentTimeMillis()
                            // If window was just hidden by focus loss (<300ms ago), treat as intentional hide
                            if (isVisible || (now - lastHideTime < 300)) {
                                isVisible = false
                                lastHideTime = now
                            } else {
                                isVisible = true
                                bringToFront = true
                            }
                        }
                    }
                })
                
                // For right-click menu, use action listener (macOS compatible)
                val popup = PopupMenu()
                val showItem = MenuItem("Show/Hide")
                showItem.addActionListener { 
                    isVisible = !isVisible
                    if (isVisible) bringToFront = true
                }
                val exitItem = MenuItem("Quit")
                exitItem.addActionListener { exitApplication() }
                popup.add(showItem)
                popup.addSeparator()
                popup.add(exitItem)

                if (!isMac) {
                    // On non-Mac, set popup normally (right-click shows menu)
                    trayIcon.popupMenu = popup
                }
                
                // On macOS, use double-click as fallback for action
                trayIcon.addActionListener {
                    isVisible = true
                    bringToFront = true
                }
                
                tray.add(trayIcon)
            } catch (_: Exception) {}
        }
    }

    Window(
        onCloseRequest = {
            if (isMac) {
                isVisible = false
            } else {
                exitApplication()
            }
        },
        visible = isVisible,
        title = "VoiceTranslator",
        icon = painterResource("icon.png"),
        state = state,
        resizable = true,
        alwaysOnTop = true
    ) {
        // Position window below tray icon and bring to front
        LaunchedEffect(bringToFront) {
            if (bringToFront) {
                if (isMac && trayClickX > 0) {
                    // Position below menu bar, centered on tray icon
                    val screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .defaultScreenDevice.defaultConfiguration.bounds
                    val menuBarHeight = Toolkit.getDefaultToolkit().getScreenInsets(
                        GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .defaultScreenDevice.defaultConfiguration
                    ).top
                    val winWidth = window.width
                    val x = (trayClickX - winWidth / 2).coerceIn(0, screenBounds.width - winWidth)
                    val y = if (menuBarHeight > 0) menuBarHeight + 4 else 28
                    window.setLocation(x, y)
                }
                window.toFront()
                window.requestFocus()
                if (isMac) {
                    // macOS blocks toFront() from background — use AppleScript
                    try {
                        ProcessBuilder("osascript", "-e",
                            """tell application "System Events" to set frontmost of first process whose unix id is ${ProcessHandle.current().pid()} to true"""
                        ).start()
                    } catch (_: Exception) {}
                }
                bringToFront = false
            }
        }
        // Auto-hide when clicking outside (like JetBrains Toolbox)
        if (isMac) {
            LaunchedEffect(Unit) {
                window.addWindowFocusListener(object : WindowFocusListener {
                    override fun windowGainedFocus(e: WindowEvent?) {}
                    override fun windowLostFocus(e: WindowEvent?) {
                        isVisible = false
                        lastHideTime = System.currentTimeMillis()
                    }
                })
            }
        }
        App()
    }
}
