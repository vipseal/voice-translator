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
    val isMac = System.getProperty("os.name").lowercase().contains("mac")

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
                    // Fallback: create a simple colored icon
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
                // Scale for tray
                val traySize = tray.trayIconSize
                val scaledImage = image.getScaledInstance(traySize.width, traySize.height, Image.SCALE_SMOOTH)
                
                val popup = PopupMenu()
                val showItem = MenuItem("Show/Hide")
                showItem.addActionListener { isVisible = !isVisible }
                val exitItem = MenuItem("Quit")
                exitItem.addActionListener { exitApplication() }
                popup.add(showItem)
                popup.addSeparator()
                popup.add(exitItem)
                
                // On macOS, left-click should show window directly (not menu)
                // Only set popup for non-Mac, or show it on right-click manually
                val trayIcon = if (isMac) {
                    TrayIcon(scaledImage, "VoiceTranslator")
                } else {
                    TrayIcon(scaledImage, "VoiceTranslator", popup)
                }
                trayIcon.isImageAutoSize = false
                trayIcon.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (e.button == MouseEvent.BUTTON1) {
                            // Left click: toggle window visibility
                            isVisible = !isVisible
                        } else if (isMac && (e.button == MouseEvent.BUTTON3 || e.isPopupTrigger)) {
                            // Right click on Mac: show context menu
                            trayIcon.popupMenu = popup
                        }
                    }
                    override fun mousePressed(e: MouseEvent) {
                        if (isMac && e.isPopupTrigger) {
                            trayIcon.popupMenu = popup
                        }
                    }
                })
                tray.add(trayIcon)
            } catch (_: Exception) {}
        }
    }

    Window(
        onCloseRequest = {
            if (isMac) {
                // On Mac, close hides to tray instead of quitting
                isVisible = false
            } else {
                exitApplication()
            }
        },
        visible = isVisible,
        title = "VoiceTranslator",
        icon = painterResource("icon.png"),
        state = state,
        resizable = true
    ) {
        // No auto-hide — window stays visible until explicitly closed or toggled via tray
        App()
    }
}
